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
public class SaveHttp1RequestTool {

    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    public SaveHttp1RequestTool(MontoyaApi api, BurpMCP burpMCP) {
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
                    "description": "Entire request content"
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
                "notes": {
                    "type": "string",
                    "description": "Notes for the request"
                },
                "response": {
                    "type": "string",
                    "description": "Response content"
                }
            },
            "required": ["data", "host", "port", "secure"]
        }
        """;

        Tool saveRequestTool = new Tool(
                "save-http1-request",
                "Saves an HTTP/1.1 request to the saved requests list",
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
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "Tool", "save-http1-request", new Gson().toJson(args));

        // Validate required parameters
        String[] requiredParams = {"data", "host", "port", "secure"};
        for (String param : requiredParams) {
            if (!args.containsKey(param)) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: Missing required parameter: " + param)), true);
            }
        }

        CallToolResult result;
        try {
            HttpRequest httpRequest = HttpUtils.buildHttp1Request(args, burpMCP.crlfReplace);
            String responseStr = args.get("response") != null ? args.get("response").toString() : "";
            HttpResponse httpResponse = HttpResponse.httpResponse(responseStr);

            HttpRequestResponse requestResponse = HttpRequestResponse.httpRequestResponse(httpRequest, httpResponse);

            burpMCP.addSavedRequest(requestResponse, args.get("notes").toString());
            
            result = new CallToolResult(Collections.singletonList(
                new TextContent("Request saved successfully")), false);
            
        } catch (Exception e) {
            result = new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: Error adding HTTP/1.1 request: " + e.getMessage())), true);
        }
        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "Tool", "save-http1-request", result.toString());
        return result;
    }
}