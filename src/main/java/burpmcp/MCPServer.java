package burpmcp;

import burp.api.montoya.MontoyaApi;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import reactor.netty.http.server.HttpServer;

import com.fasterxml.jackson.databind.ObjectMapper;

import burpmcp.tools.Http1SendTool;
import burpmcp.tools.Http2SendTool;
import burpmcp.tools.GetSavedRequestTool;
import burpmcp.tools.UpdateNoteTool;
import burpmcp.tools.SaveHttp1RequestTool;
import burpmcp.tools.SaveHttp2RequestTool;
import burpmcp.models.SavedRequestListModel;
import burpmcp.tools.GenerateCollaboratorPayloadTool;
import burpmcp.tools.RetrieveCollaboratorInteractionsTool;
public class MCPServer {
    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    private final Logger logger = LoggerFactory.getLogger(MCPServer.class);
    private McpSyncServer syncServer;
    private WebFluxSseServerTransportProvider transportProvider;
    private boolean isRunning = false;
    private String serverHost = "localhost";
    private int serverPort = 8181;
    private final String messagePath = "/mcp/message";
    private final String ssePath = "/mcp/sse";
    private reactor.netty.DisposableServer reactorServer;
    private SavedRequestListModel savedRequestListModel;
    private Http1SendTool http1SendTool;
    
    public MCPServer(MontoyaApi api, BurpMCP burpMCP) {
        this.api = api;
        this.burpMCP = burpMCP;
    }
    
    public void setSavedRequestListModel(SavedRequestListModel savedRequestListModel) {
        this.savedRequestListModel = savedRequestListModel;
    }
    
    public void setServerConfig(String host, int port) {
        if (!isRunning) {
            this.serverHost = host;
            this.serverPort = port;
        }
    }
    
    public String getServerUrl() {
        return "http://" + serverHost + ":" + serverPort;
    }
    
    public void start() {
        if (isRunning) {
            throw new IllegalStateException("MCP Server is already running");
        }
        
        try {
            // Create a WebFlux-based SSE transport provider with a new ObjectMapper each time
            this.transportProvider = new WebFluxSseServerTransportProvider(
                new ObjectMapper(), 
                messagePath, 
                ssePath
            );
            
            // Create HTTP/1.1 Send tool
            http1SendTool = new Http1SendTool(api, burpMCP);
            SyncToolSpecification http1SendToolSpec = http1SendTool.createToolSpecification();

            // Create HTTP/2 Send tool
            Http2SendTool http2SendTool = new Http2SendTool(api, burpMCP);
            SyncToolSpecification http2SendToolSpec = http2SendTool.createToolSpecification();

            // Create Retrieve Saved Request tool
            GetSavedRequestTool retrieveSavedRequestTool = new GetSavedRequestTool(burpMCP, savedRequestListModel);
            SyncToolSpecification retrieveSavedRequestToolSpec = retrieveSavedRequestTool.createToolSpecification();

            // Create Update Note tool
            UpdateNoteTool updateNoteTool = new UpdateNoteTool(burpMCP, savedRequestListModel);
            SyncToolSpecification updateNoteToolSpec = updateNoteTool.createToolSpecification();
            
            // Create Save HTTP/1.1 Request tool
            SaveHttp1RequestTool saveHttp1RequestTool = new SaveHttp1RequestTool(api, burpMCP);
            SyncToolSpecification saveHttp1RequestToolSpec = saveHttp1RequestTool.createToolSpecification();

            // Create Save HTTP/2 Request tool
            SaveHttp2RequestTool saveHttp2RequestTool = new SaveHttp2RequestTool(api, burpMCP);
            SyncToolSpecification saveHttp2RequestToolSpec = saveHttp2RequestTool.createToolSpecification();

            // Create Generate Collaborator Payload tool
            GenerateCollaboratorPayloadTool generateCollaboratorPayloadTool = new GenerateCollaboratorPayloadTool(burpMCP);
            SyncToolSpecification generateCollaboratorPayloadToolSpec = generateCollaboratorPayloadTool.createToolSpecification();

            // Create List Collaborator Interactions tool
            RetrieveCollaboratorInteractionsTool retrieveCollaboratorInteractionsTool = new RetrieveCollaboratorInteractionsTool(burpMCP);
            SyncToolSpecification retrieveCollaboratorInteractionsToolSpec = retrieveCollaboratorInteractionsTool.createToolSpecification();

            // Create request saved request specification
            // Ensure savedRequestListModel is set before starting the server
            if (savedRequestListModel == null) {
                throw new IllegalStateException("SavedRequestListModel must be set before starting the server");
            }
            
            // Create the MCP server with the WebFlux SSE transport and tools
            this.syncServer = McpServer.sync(transportProvider)
                .serverInfo("burp-mcp-server", "1.0.0")
                .capabilities(ServerCapabilities.builder()
                    .tools(true)               // Enable tool support
                    .logging()                 // Enable logging support
                    .build())
                .tool(http1SendToolSpec.tool(), http1SendToolSpec.call()) // Add HTTP/1.1 send tool
                .tool(http2SendToolSpec.tool(), http2SendToolSpec.call()) // Add HTTP/2 send tool
                .tool(retrieveSavedRequestToolSpec.tool(), retrieveSavedRequestToolSpec.call()) // Add retrieve saved request tool
                .tool(updateNoteToolSpec.tool(), updateNoteToolSpec.call()) // Add update note tool
                .tool(saveHttp1RequestToolSpec.tool(), saveHttp1RequestToolSpec.call()) // Add save HTTP/1.1 request tool
                .tool(saveHttp2RequestToolSpec.tool(), saveHttp2RequestToolSpec.call()) // Add save HTTP/2 request tool
                .tool(generateCollaboratorPayloadToolSpec.tool(), generateCollaboratorPayloadToolSpec.call()) // Add generate collaborator payload tool
                .tool(retrieveCollaboratorInteractionsToolSpec.tool(), retrieveCollaboratorInteractionsToolSpec.call()) // Add list collaborator interactions tool
                .build();
            
            // Get the router function from the transport provider
            RouterFunction<?> routerFunction = transportProvider.getRouterFunction();
            
            // Create a handler adapter for the router function
            org.springframework.web.server.WebHandler webHandler = 
                RouterFunctions.toWebHandler(routerFunction, HandlerStrategies.builder().build());
            
            // Create an HTTP handler
            org.springframework.http.server.reactive.HttpHandler httpHandler = 
                WebHttpHandlerBuilder.webHandler(webHandler).build();
            
            // Create the adapter
            ReactorHttpHandlerAdapter adapter = 
                new ReactorHttpHandlerAdapter(httpHandler);
            
            // Configure a new HttpServer with socket options
            HttpServer httpServer = HttpServer.create()
                .host(serverHost)
                .port(serverPort)
                .wiretap(true);  // Enables wiretap for debugging
            
            // Bind and store the server instance
            this.reactorServer = httpServer
                .handle(adapter)
                .bindNow();
            
            // Send a log message to clients
            this.syncServer.loggingNotification(LoggingMessageNotification.builder()
                .level(LoggingLevel.INFO)
                .logger("burp-mcp-server")
                .data("MCP Server started with WebFlux SSE transport")
                .build());
            
            // Log server initialization with URLs
            String sseUrl = getServerUrl() + ssePath;
            String messageUrl = getServerUrl() + messagePath;
            api.logging().logToOutput("Burp MCP Server started:");
            api.logging().logToOutput("- SSE endpoint: " + sseUrl);
            api.logging().logToOutput("- Message endpoint: " + messageUrl);
            
            isRunning = true;
            
        } catch (Exception e) {
            // Log the error for debugging
            api.logging().logToError("Failed to start MCP Server: " + e.getMessage(), e);
            logger.error("Failed to start MCP Server", e);
            cleanup();  // Ensure cleanup on errors
            throw new RuntimeException("Failed to start MCP Server: " + e.getMessage(), e);
        }
    }
    
    public void stop() {
        if (!isRunning) {
            api.logging().logToOutput("MCP Server is not running");
            return;
        }
        
        try {
            cleanup();
            api.logging().logToOutput("Burp MCP Server stopped");
        } catch (Exception e) {
            api.logging().logToError("Error stopping MCP Server: " + e.getMessage(), e);
            logger.error("Error stopping MCP Server", e);
        }
    }
    
    private void cleanup() {
        try {
            // Close the MCP server first
            if (syncServer != null) {
                syncServer.close(); // Use immediate close to ensure quick shutdown
                syncServer = null;
            }
            
            // Dispose the Reactor server
            if (reactorServer != null) {
                try {
                    reactorServer.disposeNow();
                } catch (Exception e) {
                    logger.error("Error disposing reactor server", e);
                }
                reactorServer = null;
            }
            
            // Release reference to transport provider
            transportProvider = null;
            
            // Force garbage collection to release resources
            System.gc();
            
            isRunning = false;
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    public String getSSEEndpoint() {
        return ssePath;
    }
    
    public String getMessageEndpoint() {
        return messagePath;
    }

    public Http1SendTool getHttp1SendTool() {
        return http1SendTool;
    }
}