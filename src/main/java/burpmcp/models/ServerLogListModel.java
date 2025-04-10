package burpmcp.models;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServerLogListModel {
    private final List<ServerLogEntry> logs;
    private int nextId = 1;

    public ServerLogListModel() {
        this.logs = new ArrayList<>();
    }

    public void addLog(String direction, String client, String capability, String specification, String messageData) {
        ServerLogEntry entry = new ServerLogEntry(nextId++, ZonedDateTime.now(), direction, client, capability, specification, messageData);
        logs.add(entry);
    }

    public int getRowCount() {
        return logs.size();
    }

    public ServerLogEntry getEntry(int rowIndex) {
        return logs.get(rowIndex);
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