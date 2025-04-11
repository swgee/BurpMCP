package burpmcp.models;

import burp.api.montoya.http.message.HttpRequestResponse;
import javax.swing.table.AbstractTableModel;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SentRequestListModel {
    private final List<SentRequestEntry> requests;
    private int nextId = 1;
    private AbstractTableModel tableModel;

    public SentRequestListModel() {
        this.requests = new ArrayList<>();
    }

    public void setTableModel(AbstractTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public void addRequest(HttpRequestResponse requestResponse, ZonedDateTime time) {
        SentRequestEntry entry = new SentRequestEntry(nextId++, time, requestResponse);
        requests.add(entry);
        if (tableModel != null) {
            tableModel.fireTableRowsInserted(requests.size() - 1, requests.size() - 1);
        }
    }

    public int getRowCount() {
        return requests.size();
    }

    public SentRequestEntry getEntry(int rowIndex) {
        return requests.get(rowIndex);
    }

    public HttpRequestResponse getRequestAt(int rowIndex) {
        return requests.get(rowIndex).getRequestResponse();
    }

    public void clear() {
        requests.clear();
        nextId = 1;
        if (tableModel != null) {
            tableModel.fireTableDataChanged();
        }
    }

    public static class SentRequestEntry {
        private final int id;
        private final ZonedDateTime time;
        private final HttpRequestResponse requestResponse;

        public SentRequestEntry(int id, ZonedDateTime time, HttpRequestResponse requestResponse) {
            this.id = id;
            this.time = time;
            this.requestResponse = requestResponse;
        }

        public int getId() {
            return id;
        }

        public ZonedDateTime getTime() {
            return time;
        }

        public HttpRequestResponse getRequestResponse() {
            return requestResponse;
        }
    }
}