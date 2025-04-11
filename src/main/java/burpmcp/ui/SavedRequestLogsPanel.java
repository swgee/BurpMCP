package burpmcp.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burpmcp.models.SavedRequestListModel;
import burpmcp.models.SavedRequestTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class SavedRequestLogsPanel extends JPanel {
    private final JTable requestTable;
    private final SavedRequestListModel savedRequestListModel;
    private final SavedRequestDetailPanel detailPanel;

    public SavedRequestLogsPanel(MontoyaApi api, SavedRequestListModel savedRequestListModel) {
        super(new BorderLayout());
        this.savedRequestListModel = savedRequestListModel;
        
        // Create the table with expanded columns including Status code and Response Length
        String[] columnNames = {"ID", "Time", "Host", "Method", "Path", "Query", "Status", "Resp Len", "Notes"};
        SavedRequestTableModel tableModel = new SavedRequestTableModel(savedRequestListModel, columnNames);
        requestTable = new JTable(tableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setAutoCreateRowSorter(true);
        
        // Connect the table model to the list model
        savedRequestListModel.setTableModel(tableModel);
        
        // Set column widths
        requestTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        requestTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        requestTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        requestTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(4).setPreferredWidth(220);
        requestTable.getColumnModel().getColumn(5).setPreferredWidth(220);
        requestTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status code
        requestTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Response Length
        requestTable.getColumnModel().getColumn(8).setPreferredWidth(250); // Notes column
        
        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Create a scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        
        // Create the detail panel
        detailPanel = new SavedRequestDetailPanel(api);
        
        // Add selection listener to show request details
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = requestTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Convert view index to model index
                    int modelRow = requestTable.convertRowIndexToModel(selectedRow);
                    HttpRequestResponse selectedRequest = savedRequestListModel.getRequestAt(modelRow);
                    detailPanel.setRequest(selectedRequest, modelRow, savedRequestListModel);
                }
            }
        });
        
        // Create a split pane to divide the table and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.4); // Give 40% to the table
        
        // Create a panel for the clear button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton clearButton = new JButton("Clear Saved Requests");
        clearButton.addActionListener(e -> {
            savedRequestListModel.clear();
            requestTable.updateUI();
        });
        buttonPanel.add(clearButton);
        
        // Add the button panel and split pane
        add(buttonPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    public JTable getRequestTable() {
        return requestTable;
    }
} 