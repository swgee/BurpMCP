package burpmcp.tools;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import burpmcp.BurpMCP;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import burp.api.montoya.collaborator.HttpDetails;
import burp.api.montoya.collaborator.DnsDetails;
import burp.api.montoya.collaborator.SmtpDetails;
import burp.api.montoya.collaborator.Interaction;
import burp.api.montoya.http.HttpProtocol;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.collaborator.DnsQueryType;
import burp.api.montoya.collaborator.SmtpProtocol;
import java.util.HashMap;
/**
 * A tool that retrieves collaborator interactions.
 */
public class RetrieveCollaboratorInteractionsTool {
    private final BurpMCP burpMCP;
    private final Gson gson;

    public RetrieveCollaboratorInteractionsTool(BurpMCP burpMCP) {
        this.burpMCP = burpMCP;
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    /**
     * Creates a Tool specification for retrieving collaborator interactions
     * 
     * @return A SyncToolSpecification for retrieving collaborator interactions
     */
    public SyncToolSpecification createToolSpecification() {
        // Define the JSON schema for the tool input
        String schema = """
        {
            "type": "object",
            "properties": {
                "payload": {
                    "type": "string",
                    "description": "The specific payload to filter interactions by."
                },
                "count": {
                    "type": "integer",
                    "description": "The number of latest interactions to retrieve. Default is 10 to limit token usage."
                },
                "includeRetrieved": {
                    "type": "boolean",
                    "description": "Whether to include previously retrieved/read interactions in the response."
                }
            },
            "required": ["includeRetrieved"]
        }
        """;

        Tool retrieveCollaboratorInteractionsTool = new Tool(
                "retrieve-collaborator-interactions",
                "Retrieves collaborator interaction details",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(retrieveCollaboratorInteractionsTool, this::handleToolCall);
    }

    /**
     * Tool handler function that lists collaborator interactions
     * 
     * @param exchange The server exchange
     * @param args The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(), 
                "Tool", "retrieve-collaborator-interactions", args.toString());
        
        CallToolResult result;
        try {
            String payload = args.get("payload") != null ? args.get("payload").toString() : null;
            int count = args.get("count") != null ? Integer.parseInt(args.get("count").toString()) : 10;
            boolean includeRetrieved = Boolean.parseBoolean(args.get("includeRetrieved").toString());

            // Get new interactions from collaborator client
            List<Interaction> newInteractions = burpMCP.collaboratorClient.getAllInteractions();

            // Convert to simple string array since Interactions are not supported by Persistence interface
            /*
             * Format of string array:
             * [id, timestamp, type, clientIp, httpProtocol, requestResponse, query, queryType, conversation, smtpProtocol]
             */
            List<String[]> newInteractionsArray = newInteractions.stream()
                .map(interaction -> new String[] {
                    interaction.id().toString(),
                    interaction.timeStamp().toString(),
                    interaction.type().toString(),
                    interaction.clientIp().toString(),
                    interaction.httpDetails().map(HttpDetails::protocol).map(HttpProtocol::toString).orElse(null),
                    interaction.httpDetails().map(HttpDetails::requestResponse).map(HttpRequestResponse::toString).orElse(null),
                    interaction.dnsDetails().map(DnsDetails::query).map(ByteArray::toString).orElse(null),
                    interaction.dnsDetails().map(DnsDetails::queryType).map(DnsQueryType::toString).orElse(null),
                    interaction.smtpDetails().map(SmtpDetails::conversation).orElse(null),
                    interaction.smtpDetails().map(SmtpDetails::protocol).map(SmtpProtocol::toString).orElse(null)
                })
                .collect(Collectors.toList());
            
            burpMCP.saveRetrievedInteractions(newInteractionsArray);

            // Filter interactions by payload if specified
            List<String[]> listToFilter = includeRetrieved ? burpMCP.retrievedInteractions : newInteractionsArray;
            List<String[]> filteredInteractions = new ArrayList<>();
            if (payload != null) {
                for (String[] interactionArray : listToFilter) {
                    if (interactionArray[0].contains(payload)) {
                        filteredInteractions.add(interactionArray);
                    }
                }
            } else {
                filteredInteractions = listToFilter;
            }
            
            // Get the last 'count' interactions
            count = Math.min(count, filteredInteractions.size()); // Ensure count doesn't exceed list size
            int startIndex = Math.max(0, filteredInteractions.size() - count);
            filteredInteractions = filteredInteractions.subList(startIndex, filteredInteractions.size());
            
            // Convert to JSON
            List<Map<String, String>> interactionData = new ArrayList<>();
            for (String[] interactionArray : filteredInteractions) {
                Map<String, String> interactionMap = new HashMap<>();
                interactionMap.put("id", interactionArray[0]);
                interactionMap.put("timestamp", interactionArray[1]);
                interactionMap.put("type", interactionArray[2]);
                interactionMap.put("clientIp", interactionArray[3]);
                if (interactionArray[2].equals("HTTP")) {
                    interactionMap.put("httpProtocol", interactionArray[4]);
                    interactionMap.put("requestResponse", interactionArray[5]);
                }
                if (interactionArray[2].equals("DNS")) {
                    interactionMap.put("query", interactionArray[6]);
                    interactionMap.put("queryType", interactionArray[7]);
                }
                if (interactionArray[2].equals("SMTP")) {
                    interactionMap.put("conversation", interactionArray[8]);
                    interactionMap.put("smtpProtocol", interactionArray[9]);
                }
                interactionData.add(interactionMap);
            }
            String jsonOutput = gson.toJson(interactionData);
            
            result = new CallToolResult(Collections.singletonList(
                new TextContent(jsonOutput)), false);
            
        } catch (Exception e) {
            result = new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: " + e.getMessage())), true);
        }

        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(), 
        "Tool", "retrieve-collaborator-interactions", result.toString());    
        
        return result;
    }
} 