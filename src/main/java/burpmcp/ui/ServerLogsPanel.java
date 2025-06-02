package burpmcp.ui;

import burp.api.montoya.MontoyaApi;
import burpmcp.models.ServerLogListModel;
import burpmcp.models.ServerLogTableModel;
import burpmcp.BurpMCPPersistence;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class ServerLogsPanel extends JPanel {
    private final ServerLogListModel serverLogListModel;
    private final JTable serverLogTable;
    private final TableRowSorter<ServerLogTableModel> tableSorter;
    private final BurpMCPPersistence persistence;
    private static final String TABLE_KEY = "serverLogs";

    public ServerLogsPanel(MontoyaApi api, ServerLogListModel serverLogListModel) {
        super(new BorderLayout());
        this.serverLogListModel = serverLogListModel;
        this.persistence = new BurpMCPPersistence(api);
        
        // Create the server logs table with updated column names
        String[] columnNames = {"ID", "Time", "Direction", "Client", "Tool", "Size"};
        ServerLogTableModel tableModel = new ServerLogTableModel(serverLogListModel, columnNames);
        serverLogTable = new JTable(tableModel);
        serverLogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create and set the table sorter
        tableSorter = new TableRowSorter<>(tableModel);
        serverLogTable.setRowSorter(tableSorter);
        
        // Connect the table model to the list model
        serverLogListModel.setTableModel(tableModel);
        
        // Set column widths
        serverLogTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID column
        serverLogTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Time
        serverLogTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Direction
        serverLogTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Client
        serverLogTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Tool
        serverLogTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Size
        
        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < serverLogTable.getColumnCount(); i++) {
            serverLogTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Add listener to save sorting state when it changes
        tableSorter.addRowSorterListener(e -> {
            persistence.saveTableSortingState(TABLE_KEY, tableSorter);
        });
        
        // Create a scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(serverLogTable);
        
        // Create the detail panel for message data
        ServerLogDetailPanel detailPanel = new ServerLogDetailPanel();
        
        // Add selection listener to show message details
        serverLogTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = serverLogTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Convert view index to model index
                    int modelRow = serverLogTable.convertRowIndexToModel(selectedRow);
                    String messageData = serverLogListModel.getEntry(modelRow).getMessageData();
                    detailPanel.setMessageData(messageData);
                }
            }
        });
        
        // Create a split pane to divide the table and message data
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.5); // 50-50 split
        
        // Create a panel for the clear button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton clearButton = new JButton("Clear Server Logs");
        clearButton.addActionListener(e -> {
            serverLogListModel.clear();
            serverLogTable.updateUI();
        });
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
    public TableRowSorter<ServerLogTableModel> getTableSorter() {
        return tableSorter;
    }

    public JTable getServerLogTable() {
        return serverLogTable;
    }
} 