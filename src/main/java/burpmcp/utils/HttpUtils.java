package burpmcp.utils;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtils {
    public static HttpRequest buildHttp2Request(Map<String, Object> args) {
        String body = args.get("body").toString();
        String headers = args.get("headers").toString();
        String method = args.get("method").toString();
        String path = args.get("path").toString();
        String host = args.get("host").toString();
        String authority = args.get("authority").toString();
        Integer port = ((Number) args.get("port")).intValue();
        Boolean secure = Boolean.valueOf(args.get("secure").toString());
        
        // Create HTTP service for the target
        HttpService httpService = HttpService.httpService(host, port, secure);

        // Create HTTP request from raw content
        List<HttpHeader> headersList = new ArrayList<>();
        headersList.add(HttpHeader.httpHeader(":scheme", secure ? "https" : "http"));
        headersList.add(HttpHeader.httpHeader(":method", method));
        headersList.add(HttpHeader.httpHeader(":path", path));
        headersList.add(HttpHeader.httpHeader(":authority", authority));
        
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
        return HttpRequest.http2Request(httpService, headersList, ByteArray.byteArray(body));
    }

    public static HttpRequest buildHttp1Request(Map<String, Object> args, boolean crlfReplace) {
        String data = args.get("data").toString();
        String host = args.get("host").toString();
        Integer port = ((Number) args.get("port")).intValue();
        Boolean secure = Boolean.valueOf(args.get("secure").toString());

        // Create HTTP service for the target
        HttpService httpService = HttpService.httpService(host, port, secure);

        if (crlfReplace) {
            // replace newlines with CRLF
            data = data.replaceAll("(?<!\\r)\\n", "\r\n");

            // Determine if the request has a body (double CRLF) and retrieve it
            String body = "";
            boolean hasBody = data.contains("\r\n\r\n");
            if (hasBody) {
                // Everything after the first double CRLF
                body = data.split("\r\n\r\n", 2)[1];
            } else {
                // Ensure at least two CRLFs at the end of the request
                if (data.endsWith("\r\n")) {
                    data += "\r\n";
                } else {
                    data += "\r\n\r\n";
                }
            }
            // withBody updates the Content-Length header to the correct length after the CRLF replacement
            return HttpRequest.httpRequest(httpService, ByteArray.byteArray(data)).withBody(ByteArray.byteArray(body));
        } else {
            // Ensure at least two newlines at the end of the request
            boolean hasBody = data.contains("\r\n\r\n") || data.contains("\n\n");
            if (!hasBody) {
                if (data.endsWith("\n")) {
                    data += "\r\n";
                } else {
                    data += "\r\n\r\n";
                }
            }
            return HttpRequest.httpRequest(httpService, ByteArray.byteArray(data));
        }
    }
} 