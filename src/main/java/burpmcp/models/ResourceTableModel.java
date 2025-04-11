package burpmcp.models;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;

public class ResourceTableModel extends AbstractTableModel {
    private final ResourceListModel resourceListModel;
    private final String[] columnNames;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResourceTableModel(ResourceListModel resourceListModel, String[] columnNames) {
        this.resourceListModel = resourceListModel;
        this.columnNames = columnNames;
    }

    @Override
    public int getRowCount() {
        return resourceListModel.getRowCount();
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
        ResourceListModel.RequestEntry entry = resourceListModel.getEntry(rowIndex);
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
            resourceListModel.setNotes(rowIndex, (String) value);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}