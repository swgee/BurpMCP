package burpmcp.tools;

import java.util.Collections;
import java.util.Map;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burpmcp.BurpMCP;
import burpmcp.models.SavedRequestListModel;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * A tool that retrieves stored request/response content from BurpMCP's saved request list.
 * This tool functions as an alternative to the RequestResponseSavedRequest for clients
 * that don't support saved requests.
 */
public class GetSavedRequestTool {
    private final BurpMCP burpMCP;
    private final SavedRequestListModel savedRequestListModel;

    public GetSavedRequestTool(BurpMCP burpMCP, SavedRequestListModel savedRequestListModel) {
        this.burpMCP = burpMCP;
        this.savedRequestListModel = savedRequestListModel;
    }

    /**
     * Creates a Tool specification for retrieving request/response content by ID
     * 
     * @return A SyncToolSpecification for accessing request/response content
     */
    public SyncToolSpecification createToolSpecification() {
        // Define the JSON schema for the tool input
        String schema = """
        {
            "type": "object",
            "properties": {
                "id": {
                    "type": "integer",
                    "description": "ID of the request/response pair to retrieve"
                }
            },
            "required": ["id"]
        }
        """;

        Tool retrieveSavedRequestTool = new Tool(
                "get-saved-request",
                "Retrieves a stored request/response pair and any notes from the saved request list by ID",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(retrieveSavedRequestTool, this::handleToolCall);
    }

    /**
     * Tool handler function that retrieves request/response content by ID
     * 
     * @param exchange The server exchange
     * @param args The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(), 
                "get-saved-request", args.toString());
        
        CallToolResult result;
        try {
            // Extract the request ID from arguments
            int requestId;
            try {
                if (args.get("id") instanceof Integer) {
                    requestId = (Integer) args.get("id");
                } else if (args.get("id") instanceof Number) {
                    requestId = ((Number) args.get("id")).intValue();
                } else {
                    requestId = Integer.parseInt(args.get("id").toString());
                }
            } catch (NumberFormatException | NullPointerException e) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: Invalid saved request ID. Please provide a valid integer ID.")), true);
            }
            
            // Find the request with matching ID
            SavedRequestListModel.RequestEntry entry = null;
            for (int i = 0; i < savedRequestListModel.getRowCount(); i++) {
                SavedRequestListModel.RequestEntry currentEntry = savedRequestListModel.getEntry(i);
                if (currentEntry.getId() == requestId) {
                    entry = currentEntry;
                    break;
                }
            }
            
            if (entry == null) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: Request ID not found: " + requestId)), true);
            }
            
            HttpRequestResponse requestResponse = entry.getRequestResponse();
            String requestHost = requestResponse.httpService().host();
            String requestPort = String.valueOf(requestResponse.httpService().port());
            String requestProtocol = requestResponse.httpService().secure() ? "HTTPS" : "HTTP";
            String requestContent = requestResponse.request().toString();
            
            String responseContent = requestResponse.hasResponse() ? 
                    requestResponse.response().toString() : "No response available";
            
            String notes = entry.getNotes();
            
            // Combine the data into a single response
            StringBuilder combinedContent = new StringBuilder();
            combinedContent.append("=== REQUEST DATA ===\n");
            combinedContent.append("Host: " + requestHost + "\n");
            combinedContent.append("Port: " + requestPort + "\n");
            combinedContent.append("Protocol: " + requestProtocol + "\n");
            combinedContent.append(requestContent);
            combinedContent.append("\n\n=== RESPONSE DATA ===\n");
            combinedContent.append(responseContent);
            combinedContent.append("\n\n=== NOTES ===\n");
            combinedContent.append(notes);
            
            // Create the result
            result = new CallToolResult(Collections.singletonList(new TextContent(combinedContent.toString())), false);
            
        } catch (Exception e) {
            result = new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: " + e.getMessage())), true);
        }

        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(), 
                "get-saved-request", result.toString());

        return result;
    }
}