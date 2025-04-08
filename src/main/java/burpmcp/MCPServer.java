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
    private reactor.netty.DisposableServer reactorServer;
    
    public MCPServer(MontoyaApi api) {
        this.api = api;
    }
    
    public void start() {
        if (isRunning) {
            api.logging().logToOutput("MCP Server is already running");
            return;
        }
        
        try {
            // Create a WebFlux-based SSE transport provider with a new ObjectMapper each time
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
            
            // Create the adapter
            ReactorHttpHandlerAdapter adapter = 
                new ReactorHttpHandlerAdapter(httpHandler);
            
            // Configure a new HttpServer with socket options
            HttpServer httpServer = HttpServer.create()
                .host("localhost")
                .port(8181)
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
            String sseUrl = serverUrl + ssePath;
            String messageUrl = serverUrl + messagePath;
            api.logging().logToOutput("Burp MCP Server started:");
            api.logging().logToOutput("- SSE endpoint: " + sseUrl);
            api.logging().logToOutput("- Message endpoint: " + messageUrl);
            
            isRunning = true;
            
        } catch (Exception e) {
            api.logging().logToError("Failed to start MCP Server: " + e.getMessage(), e);
            logger.error("Failed to start MCP Server", e);
            cleanup();  // Ensure cleanup on errors
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
    
    public String getServerUrl() {
        return serverUrl;
    }
    
    public String getSSEEndpoint() {
        return ssePath;
    }
    
    public String getMessageEndpoint() {
        return messagePath;
    }
}