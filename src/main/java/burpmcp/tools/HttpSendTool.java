package burpmcp.tools;

import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.HttpMode;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import burpmcp.BurpMCP;

/**
 * A tool for sending HTTP requests via BurpMCP
 */
public class HttpSendTool {

    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    public HttpSendTool(MontoyaApi api, BurpMCP burpMCP) {
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
                    "description": "Body of the request"
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
                    "enum": ["HTTP_1", "HTTP_2"],
                    "description": "HTTP protocol version to use for the request"
                }
            },
            "required": ["body", "headers", "method", "path", "host", "port", "secure", "httpVersion"]
        }
        """;

        Tool httpSendTool = new Tool(
                "http-send",
                "Sends an HTTP request using Burp's HTTP client",
                schema);

        // Create the tool specification with the handler function
        return new SyncToolSpecification(httpSendTool, this::handleToolCall);
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
        burpMCP.writeToServerLog("To server", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "Tool", "http-send", new Gson().toJson(args));
        try {
            String body = args.get("body").toString();
            String headers = args.get("headers").toString();
            String method = args.get("method").toString();
            String path = args.get("path").toString();
            String host = args.get("host").toString();
            Integer port = ((Number) args.get("port")).intValue();
            Boolean secure = Boolean.valueOf(args.get("secure").toString());
            HttpMode httpMode = getHttpMode(args.get("httpVersion").toString());
            
            // Create HTTP service for the target
            HttpService httpService = HttpService.httpService(host, port, secure);
            
            // Create HTTP request from raw content
            HttpRequest httpRequest;
            
            if (httpMode == HttpMode.HTTP_2) {
                List<HttpHeader> headersList = new ArrayList<>();
                headersList.add(HttpHeader.httpHeader(":scheme", secure ? "https" : "http"));
                headersList.add(HttpHeader.httpHeader(":method", method));
                headersList.add(HttpHeader.httpHeader(":path", path));
                
                String[] headerStrings = headers.split("\n");
                for (String headerString : headerStrings) {
                    String[] parts = headerString.split(":", 2);
                    if (parts.length == 2) {
                        String name = parts[0].toLowerCase();
                        String value = parts[1];
                        // Remove only a single leading space if it exists
                        if (value.startsWith(" ")) {
                            value = value.substring(1);
                        }
                        headersList.add(HttpHeader.httpHeader(name, value));
                    }
                }
                httpRequest = HttpRequest.http2Request(httpService, headersList, ByteArray.byteArray(body));
            } else {
                httpRequest = HttpRequest.httpRequest(httpService, ByteArray.byteArray(method + " " + path + " HTTP/1.1\r\n" + headers + "\r\n\r\n" + body));
            }
            
            // Send the request using the specified HTTP mode
            HttpRequestResponse response = api.http().sendRequest(httpRequest, httpMode);
            
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
                new TextContent("ERROR: Error sending HTTP request: " + e.getMessage())), true);
        }
        burpMCP.writeToServerLog("To client", exchange.getClientInfo().name()+" "+exchange.getClientInfo().version(), "Tool", "http-send", result.toString());
        return result;
    }
    
    /**
     * Converts string HTTP version to HttpMode enum
     * 
     * @param httpVersion String representation of HTTP version
     * @return The corresponding HttpMode
     */
    private HttpMode getHttpMode(String httpVersion) {
        return switch (httpVersion.toUpperCase()) {
            case "HTTP_2" -> HttpMode.HTTP_2;
            case "HTTP_1" -> HttpMode.HTTP_1;
            default -> HttpMode.AUTO;
        };
    }
}