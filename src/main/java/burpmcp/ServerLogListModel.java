package burpmcp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServerLogListModel {
    private final List<ServerLogEntry> logs;
    private int nextId = 1;

    public ServerLogListModel() {
        this.logs = new ArrayList<>();
    }

    public void addLog(String direction, String sessionId, String client, String method, String requestId, String type, String messageData) {
        ServerLogEntry entry = new ServerLogEntry(nextId++, ZonedDateTime.now(), direction, sessionId, client, method, requestId, type, messageData);
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
        private final String sessionId;
        private final String client;
        private final String method;
        private final String requestId;
        private final String type;
        private final String messageData;

        public ServerLogEntry(int id, ZonedDateTime time, String direction, String sessionId, String client, String method, String requestId, String type, String messageData) {
            this.id = id;
            this.time = time;
            this.direction = direction;
            this.sessionId = sessionId;
            this.client = client;
            this.method = method;
            this.requestId = requestId;
            this.type = type;
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

        public String getSessionId() {
            return sessionId;
        }

        public String getClient() {
            return client;
        }

        public String getMethod() {
            return method;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getType() {
            return type;
        }

        public String getMessageData() {
            return messageData;
        }
    }
}