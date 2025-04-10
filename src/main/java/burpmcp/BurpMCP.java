package burpmcp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

import burpmcp.ui.RequestDetailPanel;
import burpmcp.ui.ResourceLogsPanel;
import burpmcp.ui.ServerLogDetailPanel;
import burpmcp.ui.ServerLogsPanel;
import burpmcp.models.RequestListModel;
import burpmcp.models.ServerLogListModel;
import burpmcp.tools.HttpSendTool;

public class BurpMCP implements BurpExtension {
    private MontoyaApi api;
    private RequestListModel requestListModel;
    private ServerLogListModel serverLogListModel;
    private JPanel extensionPanel;
    private ResourceLogsPanel resourceLogsPanel;
    private ServerLogsPanel serverLogsPanel;
    private JToggleButton toggleButton;
    private MCPServer mcpServer;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("BurpMCP");
        
        // Initialize our models
        requestListModel = new RequestListModel();
        serverLogListModel = new ServerLogListModel();
        
        // Initialize MCP server
        mcpServer = new MCPServer(api, this);
        
        // Register unloading handler to stop server
        api.extension().registerUnloadingHandler(() -> {
            if (mcpServer.isRunning()) {
                mcpServer.stop();
            }
        });
        
        // Register context menu item
        api.userInterface().registerContextMenuItemsProvider(createContextMenuItemsProvider());
        
        // Create the extension UI tab
        extensionPanel = createExtensionPanel();
        toggleButton.doClick();
        api.userInterface().registerSuiteTab("BurpMCP", extensionPanel);
        
        api.logging().logToOutput("BurpMCP loaded successfully.");
    }
    
    private ContextMenuItemsProvider createContextMenuItemsProvider() {
        return new ContextMenuItemsProvider() {
            @Override
            public List<Component> provideMenuItems(ContextMenuEvent event) {
                // First check if we have any selected request responses
                if (!event.selectedRequestResponses().isEmpty()) {
                    JMenuItem sendToExtensionItem = new JMenuItem("Send to BurpMCP");
                    sendToExtensionItem.addActionListener(e -> {
                        for (HttpRequestResponse requestResponse : event.selectedRequestResponses()) {
                            requestListModel.addRequest(requestResponse);
                            resourceLogsPanel.getRequestTable().updateUI();
                        }
                    });
                    
                    return Collections.singletonList(sendToExtensionItem);
                }
                
                // Check for message editor context
                if (event.messageEditorRequestResponse().isPresent()) {
                    JMenuItem sendToExtensionItem = new JMenuItem("Send to BurpMCP");
                    sendToExtensionItem.addActionListener(e -> {
                        event.messageEditorRequestResponse().ifPresent(editor -> {
                            HttpRequestResponse requestResponse = editor.requestResponse();
                            requestListModel.addRequest(requestResponse);
                            resourceLogsPanel.getRequestTable().updateUI();
                        });
                    });
                    
                    return Collections.singletonList(sendToExtensionItem);
                }
                
                return Collections.emptyList();
            }
        };
    }
    
    private JPanel createExtensionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Create Resources tab
        resourceLogsPanel = new ResourceLogsPanel(api, requestListModel);
        tabbedPane.addTab("Resources", resourceLogsPanel);
        
        // Create Server Logs tab
        serverLogsPanel = new ServerLogsPanel(serverLogListModel);
        tabbedPane.addTab("Server Logs", serverLogsPanel);
        
        // Add the toggle button at the top
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        toggleButton = new JToggleButton("MCP Server: Disabled");
        toggleButton.addActionListener(e -> {
            if (toggleButton.isSelected()) {
                mcpServer.start();
                toggleButton.setText("MCP Server: Enabled");
                serverLogsPanel.getServerLogTable().updateUI();
            } else {
                mcpServer.stop();
                toggleButton.setText("MCP Server: Disabled");
                serverLogsPanel.getServerLogTable().updateUI();
            }
        });
        controlPanel.add(toggleButton);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }

    public void writeToServerLog(String direction, String client, String capability, String specification, String messageData) {
        serverLogListModel.addLog(direction, client, capability, specification, messageData);
        serverLogsPanel.getServerLogTable().updateUI();
    }
}