package burpmcp.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burpmcp.models.SentRequestListModel;
import burpmcp.models.SentRequestTableModel;
import burpmcp.BurpMCPPersistence;
import burpmcp.BurpMCP;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class SentRequestLogsPanel extends JPanel {
    private final SentRequestListModel sentRequestListModel;
    private final JTable sentRequestTable;
    private final JPanel detailPanel;
    private final JSplitPane requestResponseSplitPane;
    private final MontoyaApi api;
    private final BurpMCP burpMCP;
    private final TableRowSorter<SentRequestTableModel> tableSorter;
    private final BurpMCPPersistence persistence;
    private final JButton saveRequestButton;
    private static final String TABLE_KEY = "sentRequests";

    public SentRequestLogsPanel(MontoyaApi api, SentRequestListModel sentRequestListModel, BurpMCP burpMCP) {
        super(new BorderLayout());
        this.requestResponseSplitPane = new JSplitPane();
        this.api = api;
        this.burpMCP = burpMCP;
        this.sentRequestListModel = sentRequestListModel;
        this.persistence = new BurpMCPPersistence(api);
        
        // Create the table with columns
        String[] columnNames = {"ID", "Time", "Host", "Port", "Secure", "Method", "Path", "Query", "Status", "Resp Len"};
        SentRequestTableModel tableModel = new SentRequestTableModel(sentRequestListModel, columnNames);
        sentRequestTable = new JTable(tableModel);
        sentRequestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create and set the table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        sentRequestTable.setRowSorter(tableSorter);
        
        // Connect the table model to the list model
        sentRequestListModel.setTableModel(tableModel);
        
        // Set column widths
        sentRequestTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        sentRequestTable.getColumnModel().getColumn(1).setPreferredWidth(180); // Time
        sentRequestTable.getColumnModel().getColumn(2).setPreferredWidth(180); // Host
        sentRequestTable.getColumnModel().getColumn(3).setPreferredWidth(60);  // Port
        sentRequestTable.getColumnModel().getColumn(4).setPreferredWidth(70);  // Secure
        sentRequestTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Method
        sentRequestTable.getColumnModel().getColumn(6).setPreferredWidth(220); // Path
        sentRequestTable.getColumnModel().getColumn(7).setPreferredWidth(220); // Query
        sentRequestTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Status code
        sentRequestTable.getColumnModel().getColumn(9).setPreferredWidth(100); // Response Length
        
        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < sentRequestTable.getColumnCount(); i++) {
            sentRequestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Add listener to save sorting state when it changes
        tableSorter.addRowSorterListener(e -> {
            persistence.saveTableSortingState(TABLE_KEY, tableSorter);
        });
        
        // Create a scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(sentRequestTable);
        
        // Create the Save Request button (initially disabled)
        saveRequestButton = new JButton("Save Request");
        saveRequestButton.setEnabled(false);
        saveRequestButton.addActionListener(e -> {
            int selectedRow = sentRequestTable.getSelectedRow();
            if (selectedRow >= 0) {
                // Convert view index to model index
                int modelRow = sentRequestTable.convertRowIndexToModel(selectedRow);
                HttpRequestResponse selectedRequest = sentRequestListModel.getRequestAt(modelRow);
                
                // Save the request to saved requests with a note
                String notes = "Saved from Request Logs";
                burpMCP.addSavedRequest(selectedRequest, notes);
            }
        });
        
        // Create request and response editors for the detail panel
        detailPanel = new SentRequestDetailPanel(api);
        
        // Add selection listener to show request details
        sentRequestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = sentRequestTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Convert view index to model index
                    int modelRow = sentRequestTable.convertRowIndexToModel(selectedRow);
                    HttpRequestResponse selectedRequest = sentRequestListModel.getRequestAt(modelRow);
                    ((SentRequestDetailPanel) detailPanel).setRequest(selectedRequest);
                    
                    // Enable the save request button when a row is selected
                    saveRequestButton.setEnabled(true);
                } else {
                    // Disable the save request button when no row is selected
                    saveRequestButton.setEnabled(false);
                }
            }
        });
        
        // Create a split pane to divide the table and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.4); // Give 40% to the table
        
        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        
        JButton clearButton = new JButton("Clear Sent Requests");
        clearButton.addActionListener(e -> {
            sentRequestListModel.clear();
            sentRequestTable.updateUI();
            saveRequestButton.setEnabled(false); // Disable save button when clearing
        });
        
        buttonPanel.add(saveRequestButton);
        buttonPanel.add(clearButton);
        
        // Add the button panel and split pane
        add(buttonPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Restores the table sorting state from persistence
     */
    public void restoreTableSortingState() {
        persistence.restoreTableSortingState(TABLE_KEY, tableSorter);
    }
    
    /**
     * Gets the table sorter for external access
     */
    public TableRowSorter<SentRequestTableModel> getTableSorter() {
        return tableSorter;
    }

    public JTable getSentRequestTable() {
        return sentRequestTable;
    }
}