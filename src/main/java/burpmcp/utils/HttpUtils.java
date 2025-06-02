package burpmcp.utils;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtils {
    @SuppressWarnings("unchecked")
    public static HttpRequest buildHttp2Request(Map<String, Object> args, boolean ResendUpdateContentLength) {
        String body = args.get("body").toString();
        List<Map<String, Object>> headersList = (List<Map<String, Object>>) args.get("headers");
        String method = args.get("method").toString();
        String path = args.get("path").toString();
        String host = args.get("host").toString();
        String authority = args.get("authority").toString();
        Integer port = ((Number) args.get("port")).intValue();
        Boolean secure = Boolean.valueOf(args.get("secure").toString());
        
        // Create HTTP service for the target
        HttpService httpService = HttpService.httpService(host, port, secure);

        // Create HTTP request from raw content
        List<HttpHeader> httpHeadersList = new ArrayList<>();
        httpHeadersList.add(HttpHeader.httpHeader(":scheme", secure ? "https" : "http"));
        httpHeadersList.add(HttpHeader.httpHeader(":method", method));
        httpHeadersList.add(HttpHeader.httpHeader(":path", path));
        httpHeadersList.add(HttpHeader.httpHeader(":authority", authority));
        
        // Process headers as array format
        for (Map<String, Object> headerObj : headersList) {
            String name = headerObj.get("name").toString().toLowerCase();
            String value = headerObj.get("value").toString();
            httpHeadersList.add(HttpHeader.httpHeader(name, value));
        }
        if (ResendUpdateContentLength) {
            return HttpRequest.http2Request(httpService, httpHeadersList, ByteArray.byteArray(body)).withBody(ByteArray.byteArray(body));
        } else {
            return HttpRequest.http2Request(httpService, httpHeadersList, ByteArray.byteArray(body));
        }
    }

    public static HttpRequest buildHttp1Request(Map<String, Object> args, boolean crlfReplace, boolean ResendUpdateContentLength) {
        String data = args.get("data").toString();
        String host = args.get("host").toString();
        Integer port = ((Number) args.get("port")).intValue();
        Boolean secure = Boolean.valueOf(args.get("secure").toString());

        // Create HTTP service for the target
        HttpService httpService = HttpService.httpService(host, port, secure);

        String body = "";
        if (crlfReplace) {
            // replace newlines with CRLF
            data = data.replaceAll("(?<!\\r)\\n", "\r\n");

            // Determine if the request has a body (double CRLF) and retrieve it
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
        } else {
            // Ensure at least two newlines at the end of the request - otherwise it won't even send the request
            boolean hasBody = data.contains("\r\n\r\n") || data.contains("\n\n");
            if (!hasBody) {
                if (data.endsWith("\n")) {
                    data += "\r\n";
                } else {
                    data += "\r\n\r\n";
                }
            }
        }

         // withBody updates the Content-Length header to the correct length after the CRLF replacement
         if (crlfReplace || ResendUpdateContentLength) {
            return HttpRequest.httpRequest(httpService, ByteArray.byteArray(data)).withBody(ByteArray.byteArray(body));
         } else {
            return HttpRequest.httpRequest(httpService, ByteArray.byteArray(data));
         }
    }
} 