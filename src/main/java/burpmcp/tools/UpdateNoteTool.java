package burpmcp.tools;

import java.util.Collections;
import java.util.Map;

import burpmcp.BurpMCP;
import burpmcp.models.SavedRequestListModel;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * A tool that updates the note for a saved request in BurpMCP's saved request list.
 */
public class UpdateNoteTool {
    private final BurpMCP burpMCP;
    private final SavedRequestListModel savedRequestListModel;

    public UpdateNoteTool(BurpMCP burpMCP, SavedRequestListModel savedRequestListModel) {
        this.burpMCP = burpMCP;
        this.savedRequestListModel = savedRequestListModel;
    }

    /**
     * Creates a Tool specification for updating a saved request's note
     * 
     * @return A SyncToolSpecification for updating notes
     */
    public SyncToolSpecification createToolSpecification() {
        // Define the JSON schema for the tool input
        String schema = """
        {
            "type": "object",
            "properties": {
                "id": {
                    "type": "integer",
                    "description": "ID of the saved request to update"
                },
                "content": {
                    "type": "string",
                    "description": "New note content"
                }
            },
            "required": ["id", "content"]
        }
        """;

        Tool updateNoteTool = new Tool(
                "update-note",
                "Updates the note for a saved request by ID",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(updateNoteTool, this::handleToolCall);
    }

    /**
     * Tool handler function that updates a saved request's note
     * 
     * @param exchange The server exchange
     * @param args The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(), 
                "update-note", args.toString());
        
        CallToolResult result;
        try {
            // Extract the request ID and content from arguments
            int requestId;
            String content;
            try {
                if (args.get("id") instanceof Integer) {
                    requestId = (Integer) args.get("id");
                } else if (args.get("id") instanceof Number) {
                    requestId = ((Number) args.get("id")).intValue();
                } else {
                    requestId = Integer.parseInt(args.get("id").toString());
                }
                content = args.get("content").toString();
            } catch (NumberFormatException | NullPointerException e) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: Invalid arguments. Please provide a valid integer ID and string content.")), true);
            }
            
            // Find the request with matching ID
            boolean found = false;
            for (int i = 0; i < savedRequestListModel.getRowCount(); i++) {
                SavedRequestListModel.RequestEntry currentEntry = savedRequestListModel.getEntry(i);
                if (currentEntry.getId() == requestId) {
                    savedRequestListModel.setNotes(i, content);
                    // Refresh the UI
                    if (savedRequestListModel.getTableModel() != null) {
                        savedRequestListModel.getTableModel().fireTableDataChanged();
                    }
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                return new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: Request ID not found: " + requestId)), true);
            }
            
            // Create success result
            result = new CallToolResult(Collections.singletonList(
                new TextContent("Successfully updated note for request ID: " + requestId)), false);
            
        } catch (Exception e) {
            result = new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: " + e.getMessage())), true);
        }

        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(), 
                "update-note", result.toString());

        return result;
    }
} 