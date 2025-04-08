package burpmcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.HttpMode;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * A tool for sending HTTP requests via BurpMCP
 */
public class HttpSendTool {

    private final MontoyaApi api;

    public HttpSendTool(MontoyaApi api) {
        this.api = api;
    }

    /**
     * Creates a Tool specification for sending HTTP requests
     * 
     * @return A SyncToolSpecification for the HTTP send tool
     */
    public SyncToolSpecification createToolSpecification() {
        // Define the JSON schema for the tool input
        String schema = """
        {
            "type": "object",
            "properties": {
                "request": {
                    "type": "string",
                    "description": "Raw HTTP request content"
                },
                "host": {
                    "type": "string",
                    "description": "Target hostname or IP address"
                },
                "port": {
                    "type": "integer",
                    "description": "Target port number"
                },
                "secure": {
                    "type": "boolean",
                    "description": "Whether to use HTTPS (true) or HTTP (false)"
                },
                "httpVersion": {
                    "type": "string",
                    "enum": ["HTTP_1", "HTTP_2"],
                    "description": "HTTP protocol version to use"
                }
            },
            "required": ["request", "host", "port", "secure"]
        }
        """;

        Tool httpSendTool = new Tool(
                "http-send",
                "Sends an HTTP request using Burp's HTTP client",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(httpSendTool, this::handleToolCall);
    }

    /**
     * Tool handler function that processes request arguments and sends the HTTP request
     * 
     * @param exchange The server exchange
     * @param args     The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        try {
            // Validate and extract required arguments
            if (!validateArguments(args)) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: Missing or invalid required parameters: request, host, port, secure")), true);
            }

            String requestContent = args.get("request").toString();
            String host = args.get("host").toString();
            Integer port = ((Number) args.get("port")).intValue();
            Boolean secure = Boolean.valueOf(args.get("secure").toString());
            
            // Default to HTTP_1 if not specified
            String httpVersionStr = args.containsKey("httpVersion") ? args.get("httpVersion").toString() : "HTTP_1";
            HttpMode httpMode = getHttpMode(httpVersionStr);
            
            // Create HTTP service for the target
            HttpService httpService = HttpService.httpService(host, port, secure);
            
            // Create HTTP request from raw content
            HttpRequest httpRequest = HttpRequest.httpRequest(httpService, ByteArray.byteArray(requestContent));
            
            // Send the request using the specified HTTP mode
            HttpRequestResponse response = api.http().sendRequest(httpRequest, httpMode);
            
            // Process the response
            if (!response.hasResponse()) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: No response received. The request may have timed out or failed.")), true);
            }
            
            // Format and return the response
            String responseContent = response.response().toString();
            return new CallToolResult(Collections.singletonList(
                new TextContent(responseContent)), false);
            
        } catch (Exception e) {
            return new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: Error sending HTTP request: " + e.getMessage())), true);
        }
    }
    
    /**
     * Validates that all required arguments are present and of the correct type
     * 
     * @param args The arguments to validate
     * @return Whether validation passed
     */
    private boolean validateArguments(Map<String, Object> args) {
        if (!args.containsKey("request") || !(args.get("request") instanceof String)) {
            return false;
        }
        
        if (!args.containsKey("host") || !(args.get("host") instanceof String)) {
            return false;
        }
        
        if (!args.containsKey("port") || !(args.get("port") instanceof Number)) {
            return false;
        }
        
        if (!args.containsKey("secure") || !(args.get("secure").toString().equalsIgnoreCase("true") || 
                                            args.get("secure").toString().equalsIgnoreCase("false"))) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Converts string HTTP version to HttpMode enum
     * 
     * @param httpVersion String representation of HTTP version
     * @return The corresponding HttpMode
     */
    private HttpMode getHttpMode(String httpVersion) {
        return switch (httpVersion.toUpperCase()) {
            case "HTTP_2" -> HttpMode.HTTP_2;
            default -> HttpMode.HTTP_1;
        };
    }
}