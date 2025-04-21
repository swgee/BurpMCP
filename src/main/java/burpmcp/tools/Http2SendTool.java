package burpmcp.tools;

import java.util.Collections;
import java.util.Map;

import com.google.gson.Gson;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.HttpMode;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import burpmcp.BurpMCP;
import burpmcp.utils.HttpUtils;

/**
 * A tool for sending HTTP requests via BurpMCP
 */
public class Http2SendTool {

    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    public Http2SendTool(MontoyaApi api, BurpMCP burpMCP) {
        this.api = api;
        this.burpMCP = burpMCP;
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
                "body": {
                    "type": "string",
                    "description": "HTTP body part of the request"
                },
                "authority": {
                    "type": "string",
                    "description": "Authority part of the request - the Host header value of HTTP/2 requests"
                },
                "headers": {
                    "type": "string",
                    "description": "Newline separated additional HTTP headers. Don't use invalid headers according to the HTTP/2 specification (Connection, Keep-Alive, Proxy-Connection, Transfer-Encoding, Upgrade)."
                },
                "method": {
                    "type": "string",
                    "description": "HTTP method to use for the request"
                },
                "path": {
                    "type": "string",
                    "description": "Path of the request"
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
                }
            },
            "required": ["body", "headers", "method", "path", "host", "port", "secure"]
        }
        """;

        Tool http2SendTool = new Tool(
                "http2-send",
                "Sends an HTTP/2 request using Burp's HTTP client",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(http2SendTool, this::handleToolCall);
    }

    /**
     * Tool handler function that processes request arguments and sends the HTTP request
     * 
     * @param exchange The server exchange
     * @param args     The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "Tool", "http2-send", new Gson().toJson(args));

        // Validate required parameters
        String[] requiredParams = {"body", "authority", "headers", "method", "path", "host", "port", "secure"};
        for (String param : requiredParams) {
            if (!args.containsKey(param)) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: Missing required parameter: " + param)), true);
            }
        }

        CallToolResult result;
        try {
            HttpRequest httpRequest = HttpUtils.buildHttp2Request(args);
            
            // Send the request using the specified HTTP mode
            HttpRequestResponse response = api.http().sendRequest(httpRequest, HttpMode.HTTP_2);

            // Log the sent request to the Request Logs tab
            burpMCP.addSentRequest(response);
            
            // Process the response
            if (!response.hasResponse()) {
                result = new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: No response received. The request may have timed out or failed.")), true);
            }
            
            // Format and return the response
            String responseContent = response.response().toString();
            result = new CallToolResult(Collections.singletonList(
                new TextContent(responseContent)), false);
            
        } catch (Exception e) {
            result = new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: Error sending HTTP/2 request: " + e.getMessage())), true);
        }
        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "Tool", "http2-send", result.toString());
        return result;
    }
}