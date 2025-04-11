package burpmcp.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burpmcp.BurpMCP;
import burpmcp.models.ResourceListModel;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

/**
 * A resource specification for accessing stored requests in BurpMCP
 */
public class RequestResponseResource {
    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    private final ResourceListModel resourceListModel;

    public RequestResponseResource(MontoyaApi api, BurpMCP burpMCP, ResourceListModel resourceListModel) {
        this.api = api;
        this.burpMCP = burpMCP;
        this.resourceListModel = resourceListModel;
    }

    /**
     * Creates a resource specification for accessing stored requests
     * 
     * @return A SyncResourceSpecification for request resources
     */
    public SyncResourceSpecification createResourceSpecification() {
        // Define the resource
        Resource resource = new Resource(
                "burp://resource/{id}",
                "Request/response pairs",
                "Retrieves a stored request/response pair and any notes from the resource list by ID",
                "text/plain",
                null);

        // Create the resource specification with the handler function
        return new SyncResourceSpecification(resource, this::handleResourceRead);
    }

    /**
     * Resource handler function that processes request arguments and returns the request data
     * 
     * @param exchange The server exchange
     * @param request The resource read request
     * @return The resource read result
     */
    private ReadResourceResult handleResourceRead(McpSyncServerExchange exchange, ReadResourceRequest request) {
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(), 
                "Resource", "burp://request-response", request.uri());
        
        try {
            // Parse the resource URI to extract the request ID
            String uri = request.uri();
            if (!uri.startsWith("burp://request-response/")) {
                return new ReadResourceResult(Collections.singletonList(
                    new TextResourceContents(uri, "text/plain", "Invalid resource URI format")));
            }
            
            // Extract the request ID from the URI
            String idStr = uri.substring("burp://request-response/".length());
            int requestId;
            try {
                requestId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                return new ReadResourceResult(Collections.singletonList(
                    new TextResourceContents(uri, "text/plain", "Invalid resource ID: " + idStr)));
            }
            
            // Find the request with matching ID
            ResourceListModel.RequestEntry entry = null;
            for (int i = 0; i < resourceListModel.getRowCount(); i++) {
                ResourceListModel.RequestEntry currentEntry = resourceListModel.getEntry(i);
                if (currentEntry.getId() == requestId) {
                    entry = currentEntry;
                    break;
                }
            }
            
            if (entry == null) {
                return new ReadResourceResult(Collections.singletonList(
                    new TextResourceContents(uri, "text/plain", "Request ID not found: " + requestId)));
            }
            
            // Get the request, response, and notes data
            HttpRequestResponse requestResponse = entry.getRequestResponse();
            String requestContent = requestResponse.request().toString();
            
            String responseContent = requestResponse.hasResponse() ? 
                    requestResponse.response().toString() : "No response available";
            
            String notes = entry.getNotes();
            
            // Combine the data into a single response
            StringBuilder combinedContent = new StringBuilder();
            combinedContent.append("=== REQUEST ===\n");
            combinedContent.append(requestContent);
            combinedContent.append("\n\n=== RESPONSE ===\n");
            combinedContent.append(responseContent);
            combinedContent.append("\n\n=== NOTES ===\n");
            combinedContent.append(notes);
            
            List<ResourceContents> contents = new ArrayList<>();
            contents.add(new TextResourceContents(uri, "text/plain", combinedContent.toString()));
            
            ReadResourceResult result = new ReadResourceResult(contents);
            
            burpMCP.writeToServerLog("To client", exchange.getClientInfo().name() + " " + exchange.getClientInfo().version(), 
                    "Resource", "burp://request-response", "Returning data for ID: " + requestId);
            
            return result;
            
        } catch (Exception e) {
            return new ReadResourceResult(Collections.singletonList(
                new TextResourceContents(request.uri(), "text/plain", "Error: " + e.getMessage())));
        }
    }
}