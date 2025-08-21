"""
MCP Bridge Server

Bridges between STDIO transport (client side) and SSE transport (server side).
This allows clients like Claude Desktop that only support STDIO to connect to remote SSE servers.
"""

import asyncio
import logging
import sys
import typer
from typing import Any, Optional
from pydantic import BaseModel
from urllib.parse import urlparse
from mcp import (
    ClientSession, 
    ServerSession, 
    types
)
from mcp.server.models import InitializationOptions
from mcp.client.sse import sse_client
from mcp.server.stdio import stdio_server
from mcp.shared.session import RequestResponder

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stderr)]
)

logger = logging.getLogger("mcp-bridge")

app = typer.Typer()

class OutboundNotification(BaseModel):
    """Minimal model with method/params for BaseSession.send_notification."""
    method: str
    params: Optional[Any] = None

class MCPBridge:
    """
    Bidirectional bridge between STDIO client and SSE server
    """
    def __init__(self, server_url: str):
        """
        Initialize the bridge.
        
        Args:
            server_url: The URL of the SSE server to connect to
        """
        self.server_url = server_url
        self._client_session: Optional[ClientSession] = None
        self._server_session: Optional[ServerSession] = None
        
        # Validate the URL structure
        parsed_url = urlparse(server_url)
        if not all([parsed_url.scheme, parsed_url.netloc]):
            raise ValueError(f"Invalid server URL: {server_url}")
            
        logger.info(f"Initializing bridge to server: {server_url}")

    @staticmethod
    def _to_plain_params(params: Any) -> Any:
        """Convert params to plain JSON-serializable structure if needed."""
        try:
            if params is None:
                return None
            if hasattr(params, "model_dump"):
                return params.model_dump(by_alias=True, exclude_none=True)
            return params
        except Exception:
            return params

    @staticmethod
    def _extract_method_and_params(notification_obj: Any) -> tuple[Optional[str], Any]:
        """Best-effort extraction of method and params from various notification shapes."""
        obj = getattr(notification_obj, "root", notification_obj)

        # Direct attributes
        method = getattr(obj, "method", None)
        params = getattr(obj, "params", None)
        if method is not None:
            return method, MCPBridge._to_plain_params(params)

        # Pydantic model
        if hasattr(obj, "model_dump"):
            try:
                dumped = obj.model_dump(by_alias=True, exclude_none=True)
                method = dumped.get("method")
                params = dumped.get("params")
                return method, MCPBridge._to_plain_params(params)
            except Exception:
                pass

        # Dict-like
        if isinstance(obj, dict):
            method = obj.get("method")
            params = obj.get("params")
            return method, MCPBridge._to_plain_params(params)

        return None, None

    async def handle_client_request(
        self, 
        responder: RequestResponder[types.ClientRequest, types.ServerResult]
    ) -> None:
        """Handle requests from the client by forwarding them to the server."""
        if not self._client_session:
            raise RuntimeError("Client session not initialized")

        request = responder.request.root
        logger.debug(f"Forwarding client request: {request.method}")

        try:
            # Handle based on request type
            if isinstance(request, types.InitializeRequest):
                # Special handling for initialization
                with responder:
                    capabilities = types.ServerCapabilities()
                    
                    # Copy capabilities from the remote server if we have them
                    if hasattr(self, '_remote_capabilities'):
                        capabilities = self._remote_capabilities
                        
                    result = types.InitializeResult(
                        protocolVersion=types.LATEST_PROTOCOL_VERSION,
                        serverInfo=types.Implementation(
                            name="mcp-bridge",
                            version="1.0.0"
                        ),
                        capabilities=capabilities
                    )
                    await responder.respond(types.ServerResult(result))
            else:
                # Forward all other requests to the SSE server
                result = await self._forward_request(request)
                with responder:
                    await responder.respond(result)
        except Exception as e:
            logger.error(f"Error handling client request: {e}", exc_info=True)
            with responder:
                await responder.respond(
                    types.ErrorData(code=0, message=f"Bridge error: {str(e)}")
                )

    async def handle_client_notification(self, notification: types.ClientNotification) -> None:
        """Handle notifications from the client by forwarding them to the server."""
        if not self._client_session:
            raise RuntimeError("Client session not initialized")

        try:
            # Extract the notification data and forward it properly
            if hasattr(notification, 'root') and notification.root:
                method, params = self._extract_method_and_params(notification)
                try:
                    if method is not None:
                        outbound = OutboundNotification(method=method, params=params)
                        await self._client_session.send_notification(outbound)
                        logger.info(f"Forwarded client notification: {method}")
                    else:
                        logger.warning("Client notification missing method; skipping forward")
                except Exception as send_err:
                    logger.error(f"Failed forwarding client notification: {send_err}", exc_info=True)
            else:
                logger.warning("Received notification without proper root data")
        except Exception as e:
            logger.error(f"Error handling client notification: {e}", exc_info=True)

    async def handle_server_request(
        self, 
        responder: RequestResponder[types.ServerRequest, types.ClientResult]
    ) -> None:
        """Handle requests from the server by forwarding them to the client."""
        if not self._server_session:
            raise RuntimeError("Server session not initialized")

        request = responder.request.root
        logger.debug(f"Received server request: {request.method}")

        try:
            # Forward the request to the STDIO client
            if isinstance(request, types.CreateMessageRequest):
                # Handle sampling requests
                logger.info("Received sampling request from server")
                with responder:
                    await responder.respond(
                        types.ErrorData(code=0, message="Sampling not supported by bridge")
                    )
            elif isinstance(request, types.ListRootsRequest):
                # Handle roots requests
                logger.info("Received roots request from server")
                with responder:
                    await responder.respond(
                        types.ListRootsResult(roots=[])
                    )
            else:
                # Generic fallback
                logger.warning(f"Unsupported server request type: {type(request).__name__}")
                with responder:
                    await responder.respond(
                        types.ErrorData(
                            code=types.METHOD_NOT_FOUND,
                            message=f"Unsupported request method: {request.method}"
                        )
                    )
        except Exception as e:
            logger.error(f"Error handling server request: {e}", exc_info=True)
            with responder:
                await responder.respond(
                    types.ErrorData(code=0, message=f"Bridge error: {str(e)}")
                )

    async def handle_server_notification(self, notification: types.ServerNotification) -> None:
        """Handle notifications from the server by forwarding them to the client."""
        if not self._server_session:
            raise RuntimeError("Server session not initialized")

        try:
            # Extract the notification data and forward it properly
            if hasattr(notification, 'root') and notification.root:
                method, params = self._extract_method_and_params(notification)
                try:
                    if method is not None:
                        outbound = OutboundNotification(method=method, params=params)
                        await self._server_session.send_notification(outbound)
                        logger.info(f"Forwarded server notification: {method}")
                    else:
                        logger.warning("Server notification missing method; skipping forward")
                except Exception as send_err:
                    logger.error(f"Failed forwarding server notification: {send_err}", exc_info=True)
            else:
                logger.warning("Received notification without proper root data")
        except Exception as e:
            logger.error(f"Error handling server notification: {e}", exc_info=True)

    async def _forward_request(self, request: Any) -> types.ServerResult:
        """Forward a request to the SSE server and return the result."""
        if not self._client_session:
            raise RuntimeError("Client session not initialized")

        # Map request type to appropriate method on client session
        method_name = f"{request.method.replace('/', '_')}"
        client_method = getattr(self._client_session, method_name, None)
        
        if not client_method:
            # Handle standard request methods
            if request.method == "ping":
                return types.ServerResult(types.EmptyResult())
            elif request.method == "prompts/list":
                result = await self._client_session.list_prompts()
                return types.ServerResult(result)
            elif request.method == "prompts/get":
                result = await self._client_session.get_prompt(
                    request.params.name, request.params.arguments
                )
                return types.ServerResult(result)
            elif request.method == "resources/list":
                result = await self._client_session.list_resources()
                return types.ServerResult(result)
            elif request.method == "resources/templates/list":
                result = await self._client_session.list_resource_templates()
                return types.ServerResult(result)
            elif request.method == "resources/read":
                result = await self._client_session.read_resource(request.params.uri)
                return types.ServerResult(result)
            elif request.method == "tools/list":
                result = await self._client_session.list_tools()
                return types.ServerResult(result)
            elif request.method == "tools/call":
                result = await self._client_session.call_tool(
                    request.params.name, request.params.arguments
                )
                return types.ServerResult(result)
            
            logger.warning(f"Unsupported request method: {request.method}")
            raise ValueError(f"Unsupported request method: {request.method}")
        
        # Call the method with appropriate arguments based on the request parameters
        if hasattr(request, 'params') and request.params:
            # Extract params from the request
            kwargs = {}
            for field_name in request.params.model_fields.keys():
                if hasattr(request.params, field_name):
                    value = getattr(request.params, field_name)
                    if value is not None:
                        kwargs[field_name] = value
            
            result = await client_method(**kwargs)
        else:
            result = await client_method()
        
        return types.ServerResult(result)

    async def handle_client_message(
        self,
        msg: RequestResponder[types.ClientRequest, types.ServerResult] 
        | types.ClientNotification 
        | Exception
    ) -> None:
        """Handle incoming messages from the client."""
        if isinstance(msg, Exception):
            logger.error(f"Error from client: {msg}")
            return
        
        if isinstance(msg, RequestResponder):
            await self.handle_client_request(msg)
        elif isinstance(msg, types.ClientNotification):
            await self.handle_client_notification(msg)

    async def handle_server_message(
        self,
        msg: RequestResponder[types.ServerRequest, types.ClientResult] 
        | types.ServerNotification 
        | Exception
    ) -> None:
        """Handle incoming messages from the server."""
        if isinstance(msg, Exception):
            logger.error(f"Error from server: {msg}")
            return
        
        if isinstance(msg, RequestResponder):
            await self.handle_server_request(msg)
        elif isinstance(msg, types.ServerNotification):
            await self.handle_server_notification(msg)

    async def start(self) -> None:
        """Start the bridge server."""
        logger.info(f"Starting MCP bridge to {self.server_url}")

        # Connect to the SSE server
        async with sse_client(self.server_url) as (sse_read, sse_write):
            logger.info("Connected to SSE server")
            
            # Create the client session
            async with ClientSession(
                sse_read, 
                sse_write,
                message_handler=self.handle_server_message
            ) as client_session:
                self._client_session = client_session
                
                # Initialize the client connection to get capabilities
                result = await client_session.initialize()
                self._remote_capabilities = result.capabilities
                
                logger.info(f"Connected to remote server: {result.serverInfo.name} {result.serverInfo.version}")
                
                # Start the STDIO server to handle client connections
                async with stdio_server() as (stdio_read, stdio_write):
                    logger.info("STDIO server started")
                    
                    # Create the server session
                    server_options = InitializationOptions(
                        server_name="mcp-bridge",
                        server_version="1.0.0",
                        capabilities=self._remote_capabilities,
                        instructions=f"Bridge to {self.server_url}"
                    )
                    
                    async with ServerSession(
                        stdio_read, 
                        stdio_write, 
                        server_options
                    ) as server_session:
                        self._server_session = server_session
                        
                        logger.info("Waiting for client to connect...")
                        
                        # Process incoming messages from the client
                        async for message in server_session.incoming_messages:
                            await self.handle_client_message(message)

@app.command()
def start(
    server_url: str = typer.Argument(..., help="URL of the SSE server to connect to"),
    debug: bool = typer.Option(False, "--debug", "-d", help="Enable debug logging")
):
    """Start the MCP bridge server."""
    if debug:
        logging.getLogger().setLevel(logging.DEBUG)
        
    bridge = MCPBridge(server_url)
    asyncio.run(bridge.start())

if __name__ == "__main__":
    app()