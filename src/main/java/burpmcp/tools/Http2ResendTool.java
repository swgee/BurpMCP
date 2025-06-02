package burpmcp.tools;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

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
 * A tool for resending HTTP/2 requests with partial replacements via BurpMCP
 */
public class Http2ResendTool {

    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    private final SavedRequestListModel savedRequestListModel;

    public Http2ResendTool(MontoyaApi api, BurpMCP burpMCP, SavedRequestListModel savedRequestListModel) {
        this.api = api;
        this.burpMCP = burpMCP;
        this.savedRequestListModel = savedRequestListModel;
    }

    /**
     * Creates a Tool specification for resending HTTP/2 requests with partial replacements
     * 
     * @return A SyncToolSpecification for the HTTP/2 resend tool
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
                "body": {
                    "type": "object",
                    "properties": {
                        "replacements": {
                            "type": "array",
                            "description": "Array of regex replacements to apply to the HTTP body",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "regex": {
                                        "type": "string",
                                        "description": "Pattern to match within the HTTP body"
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
                "authority": {
                    "type": "string",
                    "description": "New authority value to replace the original"
                },
                "headers": {
                    "type": "array",
                    "description": "Array of regex replacements to apply to the headers string (headers will be in this format before replacements are applied: 'header-name: header-value\\nheader-name2: header-value2\\n')",
                    "items": {
                        "type": "object",
                        "properties": {
                            "regex": {
                                "type": "string",
                                "description": "Pattern to match within the headers"
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
                "method": {
                    "type": "string",
                    "description": "New HTTP method to replace the original"
                },
                "path": {
                    "type": "array",
                    "description": "Array of regex replacements to apply to the path",
                    "items": {
                        "type": "object",
                        "properties": {
                            "regex": {
                                "type": "string",
                                "description": "Pattern to match within the path"
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

        Tool http2ResendTool = new Tool(
                "http2-resend",
                "Resends a saved HTTP/2 request with partial replacements using regex patterns for body/headers/path and full replacement for authority/method/host/port/secure",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(http2ResendTool, this::handleToolCall);
    }

    /**
     * Tool handler function that processes request arguments and resends the HTTP request
     * 
     * @param exchange The server exchange
     * @param args     The tool arguments
     * @return The tool execution result
     */
    private CallToolResult handleToolCall(McpSyncServerExchange exchange, Map<String, Object> args) {
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "http2-resend", new GsonBuilder().disableHtmlEscaping().create().toJson(args));
        
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
            HttpRequest originalRequest = originalRequestResponse.request();
            
            // Extract original HTTP/2 components
            String originalBody = originalRequest.bodyToString();
            String originalHost = originalRequest.httpService().host();
            int originalPort = originalRequest.httpService().port();
            boolean originalSecure = originalRequest.httpService().secure();
            
            // Extract method, path, and authority from headers
            String originalMethod = extractHttp2PseudoHeader(originalRequest, ":method");
            String originalPath = extractHttp2PseudoHeader(originalRequest, ":path");
            String originalAuthority = extractHttp2PseudoHeader(originalRequest, ":authority");
            
            // Extract regular headers as a combined string (excluding pseudo-headers and host)
            String originalHeadersString = extractHttp2Headers(originalRequest);
            
            // Track changes for debugging
            StringBuilder changeLog = new StringBuilder();
            changeLog.append("\n=== PROPERTY CHANGES ===\n");
                        
            // Track the initial length to check if any changes were made
            int initialChangeLogLength = changeLog.length();
                        
            // Apply replacements and track changes
            String newBody = applyReplacement(originalBody, args, "body", changeLog);
            String newHost = applyFullReplacement(originalHost, args, "host", changeLog);
            int newPort = applyFullReplacementInt(originalPort, args, "port", changeLog);
            boolean newSecure = applyFullReplacementBoolean(originalSecure, args, "secure", changeLog);
            String newMethod = applyFullReplacement(originalMethod, args, "method", changeLog);
            String newPath = applyReplacement(originalPath, args, "path", changeLog);
            String newAuthority = applyFullReplacement(originalAuthority, args, "authority", changeLog);
            List<Map<String, Object>> newHeaders = applyHeaderReplacement(originalHeadersString, args, "headers", changeLog);
            
            // Extract updateContentLength from body object, default to true
            boolean updateContentLength = true;
            if (args.containsKey("body") && args.get("body") instanceof Map) {
                Map<String, Object> bodyObj = (Map<String, Object>) args.get("body");
                if (bodyObj.containsKey("updateContentLength") && bodyObj.get("updateContentLength") instanceof Boolean) {
                    updateContentLength = (Boolean) bodyObj.get("updateContentLength");
                }
            }
            
            // Check if no replacements were made
            if (changeLog.length() == initialChangeLogLength) {
                changeLog.append("No replacements made (Is your regex correct?)\n");
            }
            
            // Build the modified request arguments
            Map<String, Object> requestArgs = new HashMap<>();
            requestArgs.put("body", newBody);
            requestArgs.put("authority", newAuthority);
            requestArgs.put("headers", newHeaders);
            requestArgs.put("method", newMethod);
            requestArgs.put("path", newPath);
            requestArgs.put("host", newHost);
            requestArgs.put("port", newPort);
            requestArgs.put("secure", newSecure);
                        
            // Build and send the HTTP request using existing utility
            HttpRequest httpRequest = HttpUtils.buildHttp2Request(requestArgs, updateContentLength);
            
            // Send the request using the specified HTTP mode
            HttpRequestResponse response = api.http().sendRequest(httpRequest, HttpMode.HTTP_2);

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
                new TextContent("ERROR: Error resending HTTP/2 request: " + e.getMessage())), true);
        }
        
        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "http2-resend", result.toString());
        return result;
    }
    
    /**
     * Extract HTTP/2 pseudo-header value from the request
     */
    private String extractHttp2PseudoHeader(HttpRequest request, String headerName) {
        // First try to get it from the headers collection
        String headerValue = request.headers().stream()
                .filter(header -> header.name().equals(headerName))
                .map(header -> header.value())
                .findFirst()
                .orElse("");
        
        // If not found, use fallback methods based on the pseudo-header type. This is needed with requests sent from Repeater.
        if (headerValue.isEmpty()) {
            switch (headerName) {
                case ":method":
                    headerValue = request.method();
                    break;
                case ":path":
                    headerValue = request.path();
                    break;
                case ":authority":
                    // Try to get from Host header first, then fall back to httpService host
                    headerValue = request.headers().stream()
                            .filter(header -> header.name().equalsIgnoreCase("host"))
                            .map(header -> header.value())
                            .findFirst()
                            .orElse(request.httpService().host());
                    break;
                default:
                    break;
            }
        }
        
        return headerValue;
    }
    
    /**
     * Extract regular HTTP/2 headers (excluding pseudo-headers and host) as a combined string
     * Headers are separated by newlines for flexible regex replacement
     */
    private String extractHttp2Headers(HttpRequest request) {
        StringBuilder headersBuilder = new StringBuilder();
        request.headers().stream()
                .filter(header -> !header.name().startsWith(":"))
                .filter(header -> !header.name().equalsIgnoreCase("host")) // Exclude host header as it's handled by :authority
                .forEach(header -> {
                    if (headersBuilder.length() > 0) {
                        headersBuilder.append("\n");
                    }
                    headersBuilder.append(header.name().toLowerCase()).append(": ").append(header.value());
                });
        return headersBuilder.toString();
    }

    /**
     * Apply regex replacement to a string value based on provided arguments and track changes
     * 
     * @param originalValue The original value to potentially modify
     * @param args The tool arguments containing replacement specifications
     * @param propertyName The name of the property being modified
     * @param changeLog The StringBuilder to append change information
     * @return The modified value or original value if no replacement specified
     */
    private String applyReplacement(String originalValue, Map<String, Object> args, String propertyName, StringBuilder changeLog) {
        if (!args.containsKey(propertyName)) {
            return originalValue;
        }
        
        Object propertyObj = args.get(propertyName);
        List<Object> replacements;
        
        // Handle the different structure for body vs other properties
        if ("body".equals(propertyName)) {
            if (!(propertyObj instanceof Map)) {
                return originalValue;
            }
            Map<String, Object> bodyObj = (Map<String, Object>) propertyObj;
            if (!bodyObj.containsKey("replacements") || !(bodyObj.get("replacements") instanceof List)) {
                return originalValue;
            }
            replacements = (List<Object>) bodyObj.get("replacements");
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
                    Pattern pattern;
                    if ("headers".equals(propertyName)) {
                        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    } else {
                        pattern = Pattern.compile(regex);
                    }
                    
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
                    Pattern pattern;
                    if ("headers".equals(propertyName)) {
                        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    } else {
                        pattern = Pattern.compile(regex);
                    }
                    
                    Matcher matcher = pattern.matcher(modifiedValue);
                    if (matcher.find()) {
                        String matchedText = matcher.group();
                        changeLog.append(propertyName).append(": ").append(matchedText).append(" => ").append(replacementValue).append("\n");
                    }
                    
                    // Apply the replacement
                    if ("headers".equals(propertyName)) {
                        modifiedValue = modifiedValue.replaceFirst("(?i)" + regex, replacementValue);
                    } else {
                        modifiedValue = modifiedValue.replaceFirst(regex, replacementValue);
                    }
                }
            } catch (Exception e) {
                // If regex fails, return original value
                return originalValue;
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
        changeLog.append(propertyName).append(": ").append(originalValue).append(" => ").append(modifiedValue).append("\n");
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
        changeLog.append(propertyName).append(": ").append(originalValue).append(" => ").append(modifiedValue).append("\n");
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
        changeLog.append(propertyName).append(": ").append(originalValue).append(" => ").append(modifiedValue).append("\n");
        return modifiedValue;
    }

    /**
     * Apply header replacements to the combined headers string and return as a list of header objects
     * 
     * @param originalHeadersString The original headers as a combined string
     * @param args The tool arguments containing replacement specifications
     * @param propertyName The name of the property being modified
     * @param changeLog The StringBuilder to append change information
     * @return The modified headers list or original headers list if no replacement specified
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> applyHeaderReplacement(String originalHeadersString, Map<String, Object> args, String propertyName, StringBuilder changeLog) {
        // Apply regex replacements to the combined headers string
        String modifiedHeadersString = applyReplacement(originalHeadersString, args, propertyName, changeLog);
        
        // Convert the modified headers string back to a list of header objects
        List<Map<String, Object>> headers = new ArrayList<>();
        
        if (!modifiedHeadersString.trim().isEmpty()) {
            String[] headerLines = modifiedHeadersString.split("\n");
            
            for (String headerLine : headerLines) {
                headerLine = headerLine.trim();
                if (headerLine.isEmpty()) {
                    continue;
                }
                
                int colonIndex = headerLine.indexOf(": ");
                if (colonIndex == -1) {
                    continue; // Skip malformed headers
                }
                
                String headerName = headerLine.substring(0, colonIndex).toLowerCase();
                String headerValue = headerLine.substring(colonIndex + 2);
                
                // Handle cookie header splitting to prevent kettling
                if ("cookie".equals(headerName)) {
                    String[] cookieParts = headerValue.split("; ");
                    for (String cookiePart : cookieParts) {
                        if (!cookiePart.trim().isEmpty()) {
                            Map<String, Object> cookieHeader = new HashMap<>();
                            cookieHeader.put("name", "cookie");
                            cookieHeader.put("value", cookiePart.trim());
                            headers.add(cookieHeader);
                        }
                    }
                } else {
                    Map<String, Object> headerObj = new HashMap<>();
                    headerObj.put("name", headerName);
                    headerObj.put("value", headerValue);
                    headers.add(headerObj);
                }
            }
        }
        
        return headers;
    }
} 