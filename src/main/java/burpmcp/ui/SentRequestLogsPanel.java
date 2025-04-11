package burpmcp.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burpmcp.models.SentRequestListModel;
import burpmcp.models.SentRequestTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class SentRequestLogsPanel extends JPanel {
    private final SentRequestListModel sentRequestListModel;
    private final JTable sentRequestTable;
    private final JPanel detailPanel;
    private final JSplitPane requestResponseSplitPane;
    private final MontoyaApi api;

    public SentRequestLogsPanel(MontoyaApi api, SentRequestListModel sentRequestListModel) {
        super(new BorderLayout());
        this.requestResponseSplitPane = new JSplitPane();
        this.api = api;
        this.sentRequestListModel = sentRequestListModel;
        
        // Create the table with columns
        String[] columnNames = {"ID", "Time", "Host", "Method", "Path", "Query", "Status", "Resp Len"};
        SentRequestTableModel tableModel = new SentRequestTableModel(sentRequestListModel, columnNames);
        sentRequestTable = new JTable(tableModel);
        sentRequestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sentRequestTable.setAutoCreateRowSorter(true);
        
        // Connect the table model to the list model
        sentRequestListModel.setTableModel(tableModel);
        
        // Set column widths
        sentRequestTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        sentRequestTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        sentRequestTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        sentRequestTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        sentRequestTable.getColumnModel().getColumn(4).setPreferredWidth(220);
        sentRequestTable.getColumnModel().getColumn(5).setPreferredWidth(220);
        sentRequestTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status code
        sentRequestTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Response Length
        
        // Center all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < sentRequestTable.getColumnCount(); i++) {
            sentRequestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Create a scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(sentRequestTable);
        
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
                }
            }
        });
        
        // Create a split pane to divide the table and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailPanel);
        splitPane.setResizeWeight(0.4); // Give 40% to the table
        
        // Create a panel for the clear button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton clearButton = new JButton("Clear Sent Requests");
        clearButton.addActionListener(e -> {
            sentRequestListModel.clear();
            sentRequestTable.updateUI();
        });
        buttonPanel.add(clearButton);
        
        // Add the button panel and split pane
        add(buttonPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    public JTable getSentRequestTable() {
        return sentRequestTable;
    }
}