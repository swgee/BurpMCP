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

import burpmcp.ui.ResourceDetailPanel;
import burpmcp.ui.SentRequestLogsPanel;
import burpmcp.ui.ResourceLogsPanel;
import burpmcp.ui.ServerLogDetailPanel;
import burpmcp.ui.ServerLogsPanel;
import burpmcp.models.ResourceListModel;
import burpmcp.models.SentRequestListModel;
import burpmcp.models.ServerLogListModel;
import burpmcp.tools.HttpSendTool;

public class BurpMCP implements BurpExtension {
    private MontoyaApi api;
    private ResourceListModel resourceListModel;
    private ServerLogListModel serverLogListModel;
    private SentRequestListModel sentRequestListModel;
    private JPanel extensionPanel;
    private ResourceLogsPanel resourceLogsPanel;
    private ServerLogsPanel serverLogsPanel;
    private SentRequestLogsPanel requestLogsPanel;
    private JToggleButton toggleButton;
    private MCPServer mcpServer;
    private JTextField hostField;
    private JTextField portField;
    private BurpMCPPersistence persistence;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("BurpMCP");
        persistence = new BurpMCPPersistence(api);
        
        // Initialize our models
        resourceListModel = new ResourceListModel();
        serverLogListModel = new ServerLogListModel();
        sentRequestListModel = new SentRequestListModel();
        
        // Restore state from persistence
        persistence.restoreState(resourceListModel, sentRequestListModel, serverLogListModel);
        
        // Initialize MCP server
        mcpServer = new MCPServer(api, this);
        mcpServer.setResourceListModel(resourceListModel);
        
        // Register unloading handler to stop server
        api.extension().registerUnloadingHandler(() -> {
            if (mcpServer.isRunning()) {
                mcpServer.stop();
            }
            // Save the current state before unloading
            persistence.saveState(resourceListModel, sentRequestListModel, serverLogListModel);
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
                            resourceListModel.addRequest(requestResponse);
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
                            resourceListModel.addRequest(requestResponse);
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
        resourceLogsPanel = new ResourceLogsPanel(api, resourceListModel);
        tabbedPane.addTab("Resources", resourceLogsPanel);
        
        // Create Request Logs tab
        requestLogsPanel = new SentRequestLogsPanel(api, sentRequestListModel);
        tabbedPane.addTab("Request Logs", requestLogsPanel);
        
        // Create Server Logs tab
        serverLogsPanel = new ServerLogsPanel(serverLogListModel);
        tabbedPane.addTab("Server Logs", serverLogsPanel);
        
        // Add the control panel at the top
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // Create server configuration panel
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        configPanel.add(new JLabel("Host:"));
        
        // Initialize with default values that will be replaced if persisted values are found
        String defaultHost = "localhost";
        String defaultPort = "8181";
        
        // Try to restore saved configuration
        Object[] savedConfig = persistence.restoreServerConfig();
        if (savedConfig != null) {
            defaultHost = (String) savedConfig[0];
            defaultPort = String.valueOf(savedConfig[1]);
        }
        
        hostField = new JTextField(defaultHost, 15);
        configPanel.add(hostField);
        configPanel.add(new JLabel("Port:"));
        portField = new JTextField(defaultPort, 5);
        configPanel.add(portField);
        
        // Create toggle button
        toggleButton = new JToggleButton("MCP Server: Disabled");
        toggleButton.addActionListener(e -> {
            if (toggleButton.isSelected()) {
                try {
                    // Update server configuration
                    String host = hostField.getText().trim();
                    int port = Integer.parseInt(portField.getText().trim());
                    mcpServer.setServerConfig(host, port);
                    
                    // Save server configuration
                    persistence.saveServerConfig(host, port);
                    
                    // Start server
                    mcpServer.start();
                    toggleButton.setText("MCP Server: Enabled");
                    serverLogsPanel.getServerLogTable().updateUI();
                    
                    // Disable configuration fields
                    hostField.setEnabled(false);
                    portField.setEnabled(false);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(extensionPanel, 
                        "Invalid port number. Please enter a valid integer.", 
                        "Configuration Error", 
                        JOptionPane.ERROR_MESSAGE);
                    toggleButton.setSelected(false);
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(extensionPanel, 
                        ex.getMessage(), 
                        "Server Error", 
                        JOptionPane.ERROR_MESSAGE);
                    toggleButton.setSelected(false);
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage();
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "An unknown error occurred while starting the server.";
                    }
                    JOptionPane.showMessageDialog(extensionPanel, 
                        errorMessage, 
                        "Server Error", 
                        JOptionPane.ERROR_MESSAGE);
                    toggleButton.setSelected(false);
                }
            } else {
                try {
                    mcpServer.stop();
                    toggleButton.setText("MCP Server: Disabled");
                    serverLogsPanel.getServerLogTable().updateUI();
                    
                    // Enable configuration fields
                    hostField.setEnabled(true);
                    portField.setEnabled(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(extensionPanel, 
                        "Error stopping server: " + ex.getMessage(), 
                        "Server Error", 
                        JOptionPane.ERROR_MESSAGE);
                    toggleButton.setSelected(true); // Keep server running if stop failed
                }
            }
        });
        
        // Add components to control panel
        controlPanel.add(configPanel);
        controlPanel.add(toggleButton);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }

    public void writeToServerLog(String direction, String client, String capability, String specification, String messageData) {
        serverLogListModel.addLog(direction, client, capability, specification, messageData);
        serverLogsPanel.getServerLogTable().updateUI();
    }
    
    public void addSentRequest(HttpRequestResponse requestResponse) {
        sentRequestListModel.addRequest(requestResponse);
        requestLogsPanel.getSentRequestTable().updateUI();
    }
}