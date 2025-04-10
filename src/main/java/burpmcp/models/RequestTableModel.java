package burpmcp.model;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;

public class RequestTableModel extends AbstractTableModel {
    private final RequestListModel requestListModel;
    private final String[] columnNames;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RequestTableModel(RequestListModel requestListModel, String[] columnNames) {
        this.requestListModel = requestListModel;
        this.columnNames = columnNames;
    }

    @Override
    public int getRowCount() {
        return requestListModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RequestListModel.RequestEntry entry = requestListModel.getEntry(rowIndex);
        switch (columnIndex) {
            case 0:
                return entry.getId();
            case 1:
                return DATE_TIME_FORMATTER.format(entry.getTime());
            case 2:
                return entry.getRequestResponse().httpService().host();
            case 3:
                try {
                    return entry.getRequestResponse().request().method();
                } catch (Exception e) {
                    return "";
                }
            case 4:
                try {
                    return entry.getRequestResponse().request().pathWithoutQuery();
                } catch (Exception e) {
                    return "";
                }
            case 5:
                try {
                    String path = entry.getRequestResponse().request().path();
                    int queryIndex = path.indexOf('?');
                    return queryIndex >= 0 ? path.substring(queryIndex + 1) : "";
                } catch (Exception e) {
                    return "";
                }
            case 6:
                return entry.getNotes();
            default:
                return null;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Make Notes column editable
        return columnIndex == 6;
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 6) {
            requestListModel.setNotes(rowIndex, (String) value);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}