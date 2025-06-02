package burpmcp.tools;

import java.util.Collections;
import java.util.Map;

import com.google.gson.GsonBuilder;

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
public class Http1SendTool {

    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    public Http1SendTool(MontoyaApi api, BurpMCP burpMCP) {
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
                "data": {
                    "type": "string",
                    "description": "Request content. For reference, HTTP/1.1 requests require CRLF line endings and two CRLFs between headers and body (even if the body is empty)."
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
            "required": ["data", "host", "port", "secure"]
        }
        """;

        Tool http1SendTool = new Tool(
                "http1-send",
                "Sends an HTTP/1.1 request using Burp's HTTP client",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(http1SendTool, this::handleToolCall);
    }

    /**
     * Tool handler function that processes request arguments and sends the HTTP request
     * 
     * @param exchange The server exchange
     * @param args     The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        // Validate required parameters
        String[] requiredParams = {"data", "host", "port", "secure"};
        for (String param : requiredParams) {
            if (!args.containsKey(param)) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: Missing required parameter: " + param)), true);
            }
        }

        CallToolResult result;
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "http1-send", new GsonBuilder().disableHtmlEscaping().create().toJson(args));
        try {
            HttpRequest httpRequest = HttpUtils.buildHttp1Request(args, burpMCP.crlfReplace, false);
            
            // Send the request using the specified HTTP mode
            HttpRequestResponse response = api.http().sendRequest(httpRequest, HttpMode.HTTP_1);

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
                new TextContent("ERROR: Error sending HTTP/1.1 request: " + e.getMessage())), true);
        }
        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "http1-send", result.toString());
        return result;
    }
}