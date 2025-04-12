package burpmcp.models;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;

public class SavedRequestTableModel extends AbstractTableModel {
    private final SavedRequestListModel savedRequestListModel;
    private final String[] columnNames;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SavedRequestTableModel(SavedRequestListModel savedRequestListModel, String[] columnNames) {
        this.savedRequestListModel = savedRequestListModel;
        this.columnNames = columnNames;
    }

    @Override
    public int getRowCount() {
        return savedRequestListModel.getRowCount();
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
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) { // ID column
            return Integer.class;
        } else if (columnIndex == 6 || columnIndex == 7) { // Status and Response Length columns
            return Integer.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SavedRequestListModel.RequestEntry entry = savedRequestListModel.getEntry(rowIndex);
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
                try {
                    return entry.getRequestResponse().hasResponse() ? 
                           entry.getRequestResponse().response().statusCode() : "";
                } catch (Exception e) {
                    return "";
                }
            case 7:
                try {
                    return entry.getRequestResponse().hasResponse() ? 
                           entry.getRequestResponse().response().body().length() : "";
                } catch (Exception e) {
                    return "";
                }
            case 8:
                return entry.getNotes();
            default:
                return null;
        }
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 8) {
            savedRequestListModel.setNotes(rowIndex, (String) value);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}