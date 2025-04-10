package burpmcp.ui;

import burpmcp.models.ServerLogListModel;
import burpmcp.models.ServerLogTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ServerLogsPanel extends JPanel {
    private final ServerLogListModel serverLogListModel;
    private final JTable serverLogTable;

    public ServerLogsPanel(ServerLogListModel serverLogListModel) {
        super(new BorderLayout());
        this.serverLogListModel = serverLogListModel;
        
        // Create the server logs table with updated column names
        String[] columnNames = {"Time", "Direction", "Client", "Capability", "Specification"};
        ServerLogTableModel tableModel = new ServerLogTableModel(serverLogListModel, columnNames);
        serverLogTable = new JTable(tableModel);
        serverLogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        serverLogTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        serverLogTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        serverLogTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        serverLogTable.getColumnModel().getColumn(3).setPreferredWidth(150); 
        serverLogTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        
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
        
        add(splitPane, BorderLayout.CENTER);
    }

    public JTable getServerLogTable() {
        return serverLogTable;
    }
} 