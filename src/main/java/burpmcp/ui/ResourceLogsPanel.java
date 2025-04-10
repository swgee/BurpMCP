package burpmcp.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burpmcp.models.RequestListModel;
import burpmcp.models.RequestTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ResourceLogsPanel extends JPanel {
    private final RequestListModel requestListModel;
    private final JTable requestTable;
    private final RequestDetailPanel detailPanel;

    public ResourceLogsPanel(MontoyaApi api, RequestListModel requestListModel) {
        super(new BorderLayout());
        this.requestListModel = requestListModel;
        
        // Create the table with expanded columns including Status code and Response Length
        String[] columnNames = {"ID", "Time", "Host", "Method", "Path", "Query", "Status", "Resp Len", "Notes"};
        RequestTableModel tableModel = new RequestTableModel(requestListModel, columnNames);
        requestTable = new JTable(tableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
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
        
        add(splitPane, BorderLayout.CENTER);
    }

    public JTable getRequestTable() {
        return requestTable;
    }
} 