package burpmcp.tools;

import java.util.Collections;
import java.util.Map;

import burpmcp.BurpMCP;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import burp.api.montoya.collaborator.CollaboratorPayload;

/**
 * A tool that generates a new collaborator payload.
 */
public class GenerateCollaboratorPayloadTool {
    private final BurpMCP burpMCP;

    public GenerateCollaboratorPayloadTool(BurpMCP burpMCP) {
        this.burpMCP = burpMCP;
    }

    /**
     * Creates a Tool specification for generating a new collaborator payload
     * 
     * @return A SyncToolSpecification for generating a new collaborator payload
     */
    public SyncToolSpecification createToolSpecification() {
        // Define the JSON schema for the tool input
        String schema = """
        {
            "type": "object",
            "properties": {},
            "required": []
        }
        """;

        Tool generateCollaboratorPayloadTool = new Tool(
                "generate-collaborator-payload",
                "Generates a new collaborator payload for out-of-band applicationsecurity testing",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(generateCollaboratorPayloadTool, this::handleToolCall);
    }

    /**
     * Tool handler function that generates a new collaborator payload
     * 
     * @param exchange The server exchange
     * @param args The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(),
                "generate-collaborator-payload", args.toString());
        
        CallToolResult result;
        try {
            CollaboratorPayload payload = burpMCP.collaboratorClient.generatePayload();

            result = new CallToolResult(Collections.singletonList(
                new TextContent(payload.toString())), false);

        } catch (Exception e) {
            result = new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: " + e.getMessage())), true);
        }

        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(),
                "generate-collaborator-payload", result.toString());

        return result;
    }
} 