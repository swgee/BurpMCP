package burpmcp;

import burp.api.montoya.MontoyaApi;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;

import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import reactor.netty.http.server.HttpServer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MCPServer {
    private final MontoyaApi api;
    private final Logger logger = LoggerFactory.getLogger(MCPServer.class);
    private McpSyncServer syncServer;
    private WebFluxSseServerTransportProvider transportProvider;
    private boolean isRunning = false;
    private final String serverUrl = "http://localhost:8181"; // Server base URL
    private final String messagePath = "/mcp/message";
    private final String ssePath = "/mcp/sse";
    private WebServer webServer;
    private reactor.netty.DisposableServer reactorServer;
    
    public MCPServer(MontoyaApi api) {
        this.api = api;
    }
    
    public void start() {
        if (isRunning) {
            return;
        }
        
        try {
            // Create a WebFlux-based SSE transport provider
            this.transportProvider = new WebFluxSseServerTransportProvider(
                new ObjectMapper(), 
                messagePath, 
                ssePath
            );
            
            // Create the MCP server with the WebFlux SSE transport
            this.syncServer = McpServer.sync(transportProvider)
                .serverInfo("burp-mcp-server", "1.0.0")
                .capabilities(ServerCapabilities.builder()
                    .resources(true, true)     // Enable resource support
                    .tools(true)               // Enable tool support
                    .prompts(true)             // Enable prompt support
                    .logging()                 // Enable logging support
                    .build())
                .build();
            
            // Get the router function from the transport provider
            RouterFunction<?> routerFunction = transportProvider.getRouterFunction();
            
            // Create a handler adapter for the router function
            org.springframework.web.server.WebHandler webHandler = 
                RouterFunctions.toWebHandler(routerFunction, HandlerStrategies.builder().build());
            
            // Create an HTTP handler
            org.springframework.http.server.reactive.HttpHandler httpHandler = 
                WebHttpHandlerBuilder.webHandler(webHandler).build();
            
            // Create and start the Netty server directly
            org.springframework.http.server.reactive.ReactorHttpHandlerAdapter adapter = 
                new org.springframework.http.server.reactive.ReactorHttpHandlerAdapter(httpHandler);
            
            // Create and start the Reactor Netty HTTP server directly
            this.reactorServer = reactor.netty.http.server.HttpServer.create()
                .host("localhost")
                .port(8181)
                .handle(adapter)
                .bindNow();
            
            // Send a log message to clients
            this.syncServer.loggingNotification(LoggingMessageNotification.builder()
                .level(LoggingLevel.INFO)
                .logger("burp-mcp-server")
                .data("MCP Server started with WebFlux SSE transport")
                .build());
            
            // Log server initialization with URLs
            String sseUrl = serverUrl + ssePath;
            String messageUrl = serverUrl + messagePath;
            api.logging().logToOutput("Burp MCP Server started:");
            api.logging().logToOutput("- SSE endpoint: " + sseUrl);
            api.logging().logToOutput("- Message endpoint: " + messageUrl);
            api.logging().logToOutput("To connect manually, clients should connect to: " + sseUrl);
            
            isRunning = true;
            
        } catch (Exception e) {
            api.logging().logToError("Failed to start MCP Server: " + e.getMessage(), e);
            logger.error("Failed to start MCP Server", e);
        }
    }
    
    public void stop() {
        if (!isRunning || syncServer == null) {
            return;
        }
        
        try {
            // Close the server gracefully
            syncServer.closeGracefully();
            
            // Stop the Reactor Netty server
            if (reactorServer != null) {
                reactorServer.disposeNow();
                reactorServer = null;
            }
            
            syncServer = null;
            transportProvider = null;
            isRunning = false;
            
            api.logging().logToOutput("Burp MCP Server stopped");
            
        } catch (Exception e) {
            api.logging().logToError("Error stopping MCP Server: " + e.getMessage(), e);
            logger.error("Error stopping MCP Server", e);
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public String getServerUrl() {
        return serverUrl;
    }
    
    public String getSSEEndpoint() {
        return ssePath;
    }
    
    public String getMessageEndpoint() {
        return messagePath;
    }
    
    /**
     * Get the router function for the WebFlux application.
     * This would be used if integrating with a Spring WebFlux application.
     */
    public Object getRouterFunction() {
        if (transportProvider != null) {
            // Changed from specific type to Object to avoid type mismatch
            return transportProvider.getRouterFunction();
        }
        return null;
    }
}