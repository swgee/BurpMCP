package burpmcp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.proxy.Proxy;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.util.ArrayList;

public class BurpMCP implements BurpExtension
{
    private MontoyaApi api;
    private MCPServer mcpServer;

    @Override
    public void initialize(MontoyaApi api)
    {
        this.api = api;
        api.extension().setName("BurpMCP");
        
        // Initialize and start the MCP server
        mcpServer = new MCPServer(api);
        
        api.logging().logToOutput("BurpMCP extension loaded. MCP server started.");
    }
}