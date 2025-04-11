package burpmcp.tools;

import java.util.Collections;
import java.util.Map;

import com.google.gson.Gson;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.message.HttpRequestResponse;
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
public class SaveRequestTool {

    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    public SaveRequestTool(MontoyaApi api, BurpMCP burpMCP) {
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
                "headers": {
                    "type": "string",
                    "description": "Newline separated HTTP headers. Unless specified otherwise, the host header should be included here. If using HTTP/2, don't use invalid headers according to the HTTP/2 specification."
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
                },
                "httpVersion": {
                    "type": "string",
                    "enum": ["HTTP/1.1", "HTTP/2"],
                    "description": "HTTP protocol version to use for the request"
                },
                "notes": {
                    "type": "string",
                    "description": "Notes for the request"
                },
                "response": {
                    "type": "string",
                    "description": "Response content"
                }
            },
            "required": ["body", "headers", "method", "path", "host", "port", "secure", "httpVersion"]
        }
        """;

        Tool saveRequestTool = new Tool(
                "save-request",
                "Saves an HTTP request to the saved requests list",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(saveRequestTool, this::handleToolCall);
    }

    /**
     * Tool handler function that processes request arguments and sends the HTTP request
     * 
     * @param exchange The server exchange
     * @param args     The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        CallToolResult result;
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "Tool", "save-request", new Gson().toJson(args));
        try {
            HttpRequest httpRequest = HttpUtils.buildHttpRequest(args);
            String responseStr = args.get("response") != null ? args.get("response").toString() : "";
            HttpResponse httpResponse = HttpResponse.httpResponse(responseStr);

            HttpRequestResponse requestResponse = HttpRequestResponse.httpRequestResponse(httpRequest, httpResponse);

            burpMCP.addSavedRequest(requestResponse, args.get("notes").toString());
            
            result = new CallToolResult(Collections.singletonList(
                new TextContent("Request saved successfully")), false);
            
        } catch (Exception e) {
            result = new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: Error adding HTTP request: " + e.getMessage())), true);
        }
        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "Tool", "save-request", result.toString());
        return result;
    }
}