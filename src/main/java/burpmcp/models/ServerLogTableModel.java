package burpmcp.model;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;

public class ServerLogTableModel extends AbstractTableModel {
    private final ServerLogListModel serverLogListModel;
    private final String[] columnNames;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ServerLogTableModel(ServerLogListModel serverLogListModel, String[] columnNames) {
        this.serverLogListModel = serverLogListModel;
        this.columnNames = columnNames;
    }

    @Override
    public int getRowCount() {
        return serverLogListModel.getRowCount();
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
        ServerLogListModel.ServerLogEntry entry = serverLogListModel.getEntry(rowIndex);
        switch (columnIndex) {
            case 0:
                return DATE_TIME_FORMATTER.format(entry.getTime());
            case 1:
                return entry.getDirection();
            case 2:
                return entry.getClient();
            case 3:
                return entry.getCapability();
            case 4:
                return entry.getSpecification();
            case 5:
                return entry.getError();
            default:
                return null;
        }
    }
}