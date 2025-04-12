package burpmcp.models;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ServerLogListModel {
    private final List<ServerLogEntry> logs;
    private int nextId = 1;
    private AbstractTableModel tableModel;

    public ServerLogListModel() {
        this.logs = new ArrayList<>();
    }

    public void setTableModel(AbstractTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public void addLog(ZonedDateTime time, String direction, String client, String capability, String specification, String messageData) {
        ServerLogEntry entry = new ServerLogEntry(nextId++, time, direction, client, capability, specification, messageData);
        logs.add(entry);
        if (tableModel != null) {
            tableModel.fireTableRowsInserted(logs.size() - 1, logs.size() - 1);
        }
    }

    public int getRowCount() {
        return logs.size();
    }

    public ServerLogEntry getEntry(int rowIndex) {
        return logs.get(rowIndex);
    }

    public void clear() {
        logs.clear();
        nextId = 1;
        if (tableModel != null) {
            tableModel.fireTableDataChanged();
        }
    }

    public static class ServerLogEntry {
        private final int id;
        private final ZonedDateTime time;
        private final String direction;
        private final String client;
        private final String capability;
        private final String specification;
        private final String messageData;

        public ServerLogEntry(int id, ZonedDateTime time, String direction, String client, String capability, 
                             String specification, String messageData) {
            this.id = id;
            this.time = time;
            this.direction = direction;
            this.client = client;
            this.capability = capability;
            this.specification = specification;
            this.messageData = messageData;
        }

        public int getId() {
            return id;
        }

        public ZonedDateTime getTime() {
            return time;
        }

        public String getDirection() {
            return direction;
        }

        public String getClient() {
            return client;
        }

        public String getCapability() {
            return capability;
        }

        public String getSpecification() {
            return specification;
        }

        public String getMessageData() {
            return messageData;
        }
    }
}