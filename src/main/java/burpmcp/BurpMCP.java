package burpmcp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BurpMCP implements BurpExtension {
    private MontoyaApi api;
    private RequestListModel requestListModel;
    private ServerLogListModel serverLogListModel;
    private JPanel extensionPanel;
    private JTable requestTable;
    private JTable serverLogTable;
    private RequestDetailPanel detailPanel;
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
        mcpServer = new MCPServer(api);
        
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
        
        api.logging().logToOutput("BurpMCP extension loaded successfully.");
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
                            requestTable.updateUI();
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
                            requestTable.updateUI();
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
        JPanel resourcesPanel = createResourcesPanel();
        tabbedPane.addTab("Resources", resourcesPanel);
        
        // Create Server Logs tab
        JPanel serverLogsPanel = createServerLogsPanel();
        tabbedPane.addTab("Server Logs", serverLogsPanel);
        
        // Add the toggle button at the top
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        toggleButton = new JToggleButton("MCP Server: Disabled");
        toggleButton.addActionListener(e -> {
            if (toggleButton.isSelected()) {
                mcpServer.start();
                toggleButton.setText("MCP Server: Enabled");
                // Add server info to the logs
                serverLogListModel.addLog(
                    "INFO", 
                    "-", 
                    "BurpMCP", 
                    "-", 
                    "-", 
                    "SERVER_START", 
                    "MCP Server started at " + mcpServer.getServerUrl() + 
                    "\nSSE Endpoint: " + mcpServer.getSSEEndpoint() + 
                    "\nMessage Endpoint: " + mcpServer.getMessageEndpoint()
                );
                serverLogTable.updateUI();
            } else {
                mcpServer.stop();
                toggleButton.setText("MCP Server: Disabled");
                // Add server stop info to the logs
                serverLogListModel.addLog(
                    "INFO", 
                    "-", 
                    "BurpMCP", 
                    "-", 
                    "-", 
                    "SERVER_STOP", 
                    "MCP Server stopped"
                );
                serverLogTable.updateUI();
            }
        });
        controlPanel.add(toggleButton);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createResourcesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create the table with expanded columns
        String[] columnNames = {"ID", "Time", "Host", "Method", "Path", "Query", "Notes"};
        RequestTableModel tableModel = new RequestTableModel(requestListModel, columnNames);
        requestTable = new JTable(tableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        requestTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        requestTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        requestTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        requestTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        requestTable.getColumnModel().getColumn(5).setPreferredWidth(250);
        requestTable.getColumnModel().getColumn(6).setPreferredWidth(300); // Notes column wider
        
        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Create a scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        
        // Create the detail panel
        detailPanel = new RequestDetailPanel(api);
        
        // Add selection listener to show request details
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = requestTable.getSelectedRow();
                if (selectedRow >= 0) {
                    HttpRequestResponse selectedRequest = requestListModel.getRequestAt(selectedRow);
                    detailPanel.setRequest(selectedRequest, selectedRow, requestListModel);
                }
            }
        });
        
        // Create a split pane to divide the table and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.4); // Give 40% to the table
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createServerLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create the server logs table
        String[] columnNames = {"Time", "Direction", "Session ID", "Client", "Method", "Request ID", "Type"};
        ServerLogTableModel tableModel = new ServerLogTableModel(serverLogListModel, columnNames);
        serverLogTable = new JTable(tableModel);
        serverLogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        serverLogTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        serverLogTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        serverLogTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        serverLogTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        serverLogTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        serverLogTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        serverLogTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < serverLogTable.getColumnCount(); i++) {
            serverLogTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Create a scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(serverLogTable);
        
        // Create the detail panel for message data
        ServerLogDetailPanel detailPanel = new ServerLogDetailPanel();
        
        // Add selection listener to show message details
        serverLogTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = serverLogTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String messageData = serverLogListModel.getEntry(selectedRow).getMessageData();
                    detailPanel.setMessageData(messageData);
                }
            }
        });
        
        // Create a split pane to divide the table and message data
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.5); // 50-50 split
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
}