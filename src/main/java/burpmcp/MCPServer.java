package burpmcp;

import burp.api.montoya.MontoyaApi;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;

import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MCPServer {
    private final MontoyaApi api;
    private final Logger logger = LoggerFactory.getLogger(MCPServer.class);
    
    public MCPServer(MontoyaApi api) {
        this.api = api;
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(new ObjectMapper());
        McpSyncServer syncServer = McpServer.sync(transportProvider)
            .serverInfo("burp-mcp-server", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .resources(true, true)     // Enable resource support
                .tools(true)               // Enable tool support
                .prompts(true)             // Enable prompt support
                .logging()                 // Enable logging support
                .build())
            .build();

        // Send a log message to clients
        syncServer.loggingNotification(LoggingMessageNotification.builder()
            .level(LoggingLevel.INFO)
            .logger("custom-logger")
            .data("Custom log message")
            .build());

        // Register resource for getting proxy history
        syncServer.addResource(new SyncResourceSpecification(
            new Resource(
                "burp://proxy/history", 
                "Proxy History", 
                "application/json", 
                "List of all proxy history in Burp Suite", 
                null
            ),
            (exchange, request) -> {
                try {
                    List<Map<String, Object>> tabs = getRepeaterTabsInfo.get();
                    String tabsJson = new ObjectMapper().writeValueAsString(tabs);
                    
                    return new McpSchema.ReadResourceResult(List.of(
                        new McpSchema.TextResourceContents(
                            "burp://repeater/tabs",
                            "application/json",
                            tabsJson
                        )
                    ));
                } catch (Exception e) {
                    logger.error("Error getting repeater tabs resource", e);
                    throw new RuntimeException("Failed to get repeater tabs: " + e.getMessage());
                }
            }
        ));
    }
} 