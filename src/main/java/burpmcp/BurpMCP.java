package burpmcp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.time.ZonedDateTime;

import burpmcp.ui.SentRequestLogsPanel;
import burpmcp.ui.SavedRequestLogsPanel;
import burpmcp.ui.ServerLogsPanel;
import burpmcp.models.SavedRequestListModel;
import burpmcp.models.SentRequestListModel;
import burpmcp.models.ServerLogListModel;

public class BurpMCP implements BurpExtension {
    private MontoyaApi api;
    private SavedRequestListModel savedRequestListModel;
    private ServerLogListModel serverLogListModel;
    private SentRequestListModel sentRequestListModel;
    private JPanel extensionPanel;
    private SavedRequestLogsPanel savedRequestLogsPanel;
    private ServerLogsPanel serverLogsPanel;
    private SentRequestLogsPanel requestLogsPanel;
    private JToggleButton mcpServerToggleButton;
    private JToggleButton crlfToggleButton;
    private boolean mcpServerEnabled;
    public boolean crlfReplace;
    private MCPServer mcpServer;
    private JTextField hostField;
    private JTextField portField;
    private String serverHost;
    private Integer serverPort;
    private BurpMCPPersistence persistence;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("BurpMCP");
        persistence = new BurpMCPPersistence(api);
        
        // Initialize our models
        savedRequestListModel = new SavedRequestListModel();
        serverLogListModel = new ServerLogListModel();
        sentRequestListModel = new SentRequestListModel();
        
        // Restore state from persistence
        persistence.restoreState(savedRequestListModel, sentRequestListModel, serverLogListModel);
        
        // Initialize MCP server
        mcpServer = new MCPServer(api, this);
        mcpServer.setSavedRequestListModel(savedRequestListModel);
        
        // Register unloading handler to stop server
        api.extension().registerUnloadingHandler(() -> {
            if (mcpServer.isRunning()) {
                mcpServer.stop();
            }
            // Save the current state before unloading
            persistence.saveState(savedRequestListModel, sentRequestListModel, serverLogListModel);
        });
        
        // Register context menu item
        api.userInterface().registerContextMenuItemsProvider(createContextMenuItemsProvider());
        
        // Create the extension UI tab
        extensionPanel = createExtensionPanel();
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
                            savedRequestListModel.addRequest(requestResponse, ZonedDateTime.now(), "");
                            savedRequestLogsPanel.getRequestTable().updateUI();
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
                            savedRequestListModel.addRequest(requestResponse, ZonedDateTime.now(), "");
                            savedRequestLogsPanel.getRequestTable().updateUI();
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
        
        // Create Saved Requests tab
        savedRequestLogsPanel = new SavedRequestLogsPanel(api, savedRequestListModel);
        tabbedPane.addTab("Saved Requests", savedRequestLogsPanel);
        
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
        serverHost = "localhost";
        serverPort = 8181;
        crlfReplace = true;
        mcpServerEnabled = false;
        // Try to restore saved configuration
        Object[] savedConfig = persistence.restoreServerConfig();
        if (savedConfig != null) {
            serverHost = (String) savedConfig[0];
            serverPort = (Integer) savedConfig[1];
            crlfReplace = (Boolean) savedConfig[2];
            mcpServerEnabled = (Boolean) savedConfig[3];
        }
        
        hostField = new JTextField(serverHost, 15);
        configPanel.add(hostField);
        configPanel.add(new JLabel("Port:"));
        portField = new JTextField(String.valueOf(serverPort), 5);
        configPanel.add(portField);
        
        // Create toggle button
        mcpServerToggleButton = new JToggleButton("MCP Server: Disabled");
        mcpServerToggleButton.addActionListener(e -> {
            if (mcpServerToggleButton.isSelected()) {
                try {
                    // Update server configuration
                    serverHost = hostField.getText().trim();
                    serverPort = Integer.parseInt(portField.getText().trim());
                    mcpServerEnabled = true;
                    mcpServer.setServerConfig(serverHost, serverPort);
                    
                    // Save server configuration
                    persistence.saveServerConfig(serverHost, serverPort, crlfReplace, mcpServerEnabled);
                    
                    // Start server
                    mcpServer.start();
                    mcpServerToggleButton.setText("MCP Server: Enabled");
                    serverLogsPanel.getServerLogTable().updateUI();
                    
                    // Disable configuration fields
                    hostField.setEnabled(false);
                    portField.setEnabled(false);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(extensionPanel, 
                        "Invalid port number. Please enter a valid integer.", 
                        "Configuration Error", 
                        JOptionPane.ERROR_MESSAGE);
                    mcpServerToggleButton.setSelected(false);
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(extensionPanel, 
                        ex.getMessage(), 
                        "Server Error", 
                        JOptionPane.ERROR_MESSAGE);
                    mcpServerToggleButton.setSelected(false);
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage();
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "An unknown error occurred while starting the server.";
                    }
                    JOptionPane.showMessageDialog(extensionPanel, 
                        errorMessage, 
                        "Server Error", 
                        JOptionPane.ERROR_MESSAGE);
                    mcpServerToggleButton.setSelected(false);
                }
            } else {
                try {
                    mcpServer.stop();
                    mcpServerToggleButton.setText("MCP Server: Disabled");
                    serverLogsPanel.getServerLogTable().updateUI();
                    
                    // Enable configuration fields
                    hostField.setEnabled(true);
                    portField.setEnabled(true);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(extensionPanel, 
                        "Error stopping server: " + ex.getMessage(), 
                        "Server Error", 
                        JOptionPane.ERROR_MESSAGE);
                    mcpServerToggleButton.setSelected(true); // Keep server running if stop failed
                }
            }
        });
        
        // Create CRLF toggle button with tooltip
        crlfToggleButton = new JToggleButton("Replace LF with CRLF");
        crlfToggleButton.setToolTipText("Replace all LF \"\\n\" with CRLF \"\\r\\n\" in HTTP/1.1 requests.\nSome LLMs are not capable of sending CRLFs in their MCP\nmessages, which are required for HTTP/1.1.");
        crlfToggleButton.setSelected(crlfReplace);  
        
        // Add action listener to save state when toggled
        crlfToggleButton.addActionListener(e -> {
            crlfReplace = crlfToggleButton.isSelected();
            persistence.saveServerConfig(serverHost, serverPort, crlfReplace, mcpServerEnabled);
        });
        
        // Add components to control panel
        controlPanel.add(configPanel);
        controlPanel.add(mcpServerToggleButton);
        controlPanel.add(crlfToggleButton);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);

        // If the server was previously enabled, restart it
        if (mcpServerEnabled) {
            mcpServerToggleButton.doClick();
        }
        
        return panel;
    }

    public void writeToServerLog(String direction, String client, String capability, String specification, String messageData) {
        serverLogListModel.addLog(ZonedDateTime.now(), direction, client, capability, specification, messageData);
        serverLogsPanel.getServerLogTable().updateUI();
    }
    
    public void addSentRequest(HttpRequestResponse requestResponse) {
        sentRequestListModel.addRequest(requestResponse, ZonedDateTime.now());
        requestLogsPanel.getSentRequestTable().updateUI();
    }

    public void addSavedRequest(HttpRequestResponse requestResponse, String notes) {
        savedRequestListModel.addRequest(requestResponse, ZonedDateTime.now(), notes);
        savedRequestLogsPanel.getRequestTable().updateUI();
    }
}