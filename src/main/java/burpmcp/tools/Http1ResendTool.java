package burpmcp.tools;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
import burpmcp.models.SavedRequestListModel;
import burpmcp.utils.HttpUtils;
import com.google.gson.GsonBuilder;

/**
 * A tool for resending HTTP/1.1 requests with partial replacements via BurpMCP
 */
public class Http1ResendTool {

    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    private final SavedRequestListModel savedRequestListModel;

    public Http1ResendTool(MontoyaApi api, BurpMCP burpMCP, SavedRequestListModel savedRequestListModel) {
        this.api = api;
        this.burpMCP = burpMCP;
        this.savedRequestListModel = savedRequestListModel;
    }

    /**
     * Creates a Tool specification for resending HTTP/1.1 requests with partial replacements
     * 
     * @return A SyncToolSpecification for the HTTP/1.1 resend tool
     */
    public SyncToolSpecification createToolSpecification() {
        // Define the JSON schema for the tool input
        String schema = """
        {
            "type": "object",
            "properties": {
                "id": {
                    "type": "integer",
                    "description": "ID of the saved request to resend"
                },
                "data": {
                    "type": "object",
                    "properties": {
                        "replacements": {
                            "type": "array",
                            "description": "Array of regex replacements to apply to the request data",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "regex": {
                                        "type": "string",
                                        "description": "Pattern to match within the request data"
                                    },
                                    "replacement": {
                                        "type": "string",
                                        "description": "Pattern to replace matches with"
                                    },
                                    "global": {
                                        "type": "boolean",
                                        "description": "Whether to replace all matches (true) or just the first match (false). Default is false.",
                                        "default": false
                                    }
                                },
                                "required": ["regex", "replacement"]
                            }
                        },
                        "updateContentLength": {
                            "type": "boolean",
                            "description": "Whether to automatically update the Content-Length header. Default is true.",
                            "default": true
                        }
                    }
                },
                "host": {
                    "type": "string",
                    "description": "New hostname or IP address to replace the original"
                },
                "port": {
                    "type": "integer",
                    "description": "New port number to replace the original"
                },
                "secure": {
                    "type": "boolean",
                    "description": "New secure flag (HTTPS/HTTP) to replace the original"
                }
            },
            "required": ["id"]
        }
        """;

        Tool http1ResendTool = new Tool(
                "http1-resend",
                "Resends a saved HTTP/1.1 request with partial replacements using regex patterns for data and full replacement for host/port/secure",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(http1ResendTool, this::handleToolCall);
    }

    /**
     * Tool handler function that processes request arguments and resends the HTTP request
     * 
     * @param exchange The server exchange
     * @param args     The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "http1-resend", new GsonBuilder().disableHtmlEscaping().create().toJson(args));
        
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
            
            // Find the saved request with matching ID
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
            
            // Get the original request
            HttpRequestResponse originalRequestResponse = entry.getRequestResponse();
            String originalData = originalRequestResponse.request().toString();
            String originalHost = originalRequestResponse.request().httpService().host();
            int originalPort = originalRequestResponse.request().httpService().port();
            boolean originalSecure = originalRequestResponse.request().httpService().secure();
            
            // Track changes for debugging
            StringBuilder changeLog = new StringBuilder();
            changeLog.append("=== PROPERTY CHANGES ===\n");
            
            // Track the initial length to check if any changes were made
            int initialChangeLogLength = changeLog.length();
            
            // Apply replacements and track changes
            String newData = applyReplacement(originalData, args, "data", changeLog);
            String newHost = applyFullReplacement(originalHost, args, "host", changeLog);
            int newPort = applyFullReplacementInt(originalPort, args, "port", changeLog);
            boolean newSecure = applyFullReplacementBoolean(originalSecure, args, "secure", changeLog);
            
            // Extract updateContentLength from data object, default to true
            boolean updateContentLength = true;
            if (args.containsKey("data") && args.get("data") instanceof Map) {
                Map<String, Object> dataObj = (Map<String, Object>) args.get("data");
                if (dataObj.containsKey("updateContentLength") && dataObj.get("updateContentLength") instanceof Boolean) {
                    updateContentLength = (Boolean) dataObj.get("updateContentLength");
                }
            }
            
            // Check if no replacements were made
            if (changeLog.length() == initialChangeLogLength) {
                changeLog.append("No replacements made (Is your regex correct?)\n");
            }
            
            // Build the modified request arguments
            Map<String, Object> requestArgs = new HashMap<>();
            requestArgs.put("data", newData);
            requestArgs.put("host", newHost);
            requestArgs.put("port", newPort);
            requestArgs.put("secure", newSecure);
            
            // Build and send the HTTP request using existing utility
            HttpRequest httpRequest = HttpUtils.buildHttp1Request(requestArgs, burpMCP.crlfReplace, updateContentLength);
            
            // Send the request using the specified HTTP mode
            HttpRequestResponse response = api.http().sendRequest(httpRequest, HttpMode.HTTP_1);

            // Log the sent request to the Request Logs tab
            burpMCP.addSentRequest(response);
            
            // Process the response
            if (!response.hasResponse()) {
                result = new CallToolResult(Collections.singletonList(
                    new TextContent("ERROR: No response received. The request may have timed out or failed.")), true);
            } else {
                // Format and return the response with change information
                String responseContent = response.response().toString();
                String combinedResponse = changeLog.toString() + "\n=== HTTP RESPONSE ===\n" + responseContent;
                result = new CallToolResult(Collections.singletonList(
                    new TextContent(combinedResponse)), false);
            }
            
        } catch (Exception e) {
            result = new CallToolResult(Collections.singletonList(
                new TextContent("ERROR: Error resending HTTP/1.1 request: " + e.getMessage())), true);
        }
        
        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "http1-resend", result.toString());
        return result;
    }
    
    /**
     * Apply multiple regex replacements to a string value based on provided arguments and track changes
     * 
     * @param originalValue The original value to potentially modify
     * @param args The tool arguments containing replacement specifications
     * @param propertyName The name of the property being modified
     * @param changeLog The StringBuilder to append change information
     * @return The modified value or original value if no replacement specified
     */
    @SuppressWarnings("unchecked")
    private String applyReplacement(String originalValue, Map<String, Object> args, String propertyName, StringBuilder changeLog) {
        if (!args.containsKey(propertyName)) {
            return originalValue;
        }
        
        Object propertyObj = args.get(propertyName);
        List<Object> replacements;
        
        // Handle the different structure for data vs other properties
        if ("data".equals(propertyName)) {
            if (!(propertyObj instanceof Map)) {
                return originalValue;
            }
            Map<String, Object> dataObj = (Map<String, Object>) propertyObj;
            if (!dataObj.containsKey("replacements") || !(dataObj.get("replacements") instanceof List)) {
                return originalValue;
            }
            replacements = (List<Object>) dataObj.get("replacements");
        } else {
            if (!(propertyObj instanceof List)) {
                return originalValue;
            }
            replacements = (List<Object>) propertyObj;
        }
        
        if (replacements.isEmpty()) {
            return originalValue;
        }
        
        String modifiedValue = originalValue;
        for (Object replacementObj : replacements) {
            if (!(replacementObj instanceof Map)) {
                continue;
            }
            
            Map<String, Object> replacement = (Map<String, Object>) replacementObj;
            if (!replacement.containsKey("regex") || !replacement.containsKey("replacement")) {
                continue;
            }
            
            String regex = replacement.get("regex").toString();
            String replacementValue = replacement.get("replacement").toString();
            
            // Get the global flag, default to false
            boolean global = false;
            if (replacement.containsKey("global") && replacement.get("global") instanceof Boolean) {
                global = (Boolean) replacement.get("global");
            }
            
            try {
                if (global) {
                    // For global replacement, use manual matching to log individual matches
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(modifiedValue);
                    StringBuffer result = new StringBuffer();
                    while (matcher.find()) {
                        String matchedText = matcher.group();
                        changeLog.append(propertyName).append(": ").append(matchedText).append(" => ").append(replacementValue).append("\n");
                        matcher.appendReplacement(result, replacementValue);
                    }
                    matcher.appendTail(result);
                    modifiedValue = result.toString();
                } else {
                    // For non-global replacement, find the first match and log it
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(modifiedValue);
                    if (matcher.find()) {
                        String matchedText = matcher.group();
                        changeLog.append(propertyName).append(": ").append(matchedText).append(" => ").append(replacementValue).append("\n");
                    }
                    
                    // Apply the replacement
                    modifiedValue = modifiedValue.replaceFirst(regex, replacementValue);
                }
            } catch (Exception e) {
                // If regex fails, continue with the original value for this replacement
                changeLog.append(propertyName).append(": Invalid regex pattern: ").append(regex).append("\n");
            }
        }
        
        return modifiedValue;
    }
    
    /**
     * Apply full string replacement based on provided arguments and track changes
     */
    private String applyFullReplacement(String originalValue, Map<String, Object> args, String propertyName, StringBuilder changeLog) {
        if (!args.containsKey(propertyName)) {
            return originalValue;
        }
        
        Object propertyObj = args.get(propertyName);
        if (propertyObj == null) {
            return originalValue;
        }
        
        String modifiedValue = propertyObj.toString();
        changeLog.append(propertyName).append(" ").append(originalValue).append(" => ").append(modifiedValue).append("\n");
        return modifiedValue;
    }
    
    /**
     * Apply full integer replacement based on provided arguments and track changes
     */
    private int applyFullReplacementInt(int originalValue, Map<String, Object> args, String propertyName, StringBuilder changeLog) {
        if (!args.containsKey(propertyName)) {
            return originalValue;
        }
        
        Object propertyObj = args.get(propertyName);
        if (!(propertyObj instanceof Number)) {
            return originalValue;
        }
        
        Number propertyValue = (Number) propertyObj;
        int modifiedValue = propertyValue.intValue();
        changeLog.append(propertyName).append(" ").append(originalValue).append(" => ").append(modifiedValue).append("\n");
        return modifiedValue;
    }
    
    /**
     * Apply full boolean replacement based on provided arguments and track changes
     */
    private boolean applyFullReplacementBoolean(boolean originalValue, Map<String, Object> args, String propertyName, StringBuilder changeLog) {
        if (!args.containsKey(propertyName)) {
            return originalValue;
        }
        
        Object propertyObj = args.get(propertyName);
        if (!(propertyObj instanceof Boolean)) {
            return originalValue;
        }
        
        Boolean propertyValue = (Boolean) propertyObj;
        boolean modifiedValue = propertyValue;
        changeLog.append(propertyName).append(" ").append(originalValue).append(" => ").append(modifiedValue).append("\n");
        return modifiedValue;
    }
} 