package burpmcp;

import burp.api.montoya.http.message.HttpRequestResponse;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestListModel {
    private final List<RequestEntry> requests;
    private int nextId = 1;

    public RequestListModel() {
        this.requests = new ArrayList<>();
    }

    public void addRequest(HttpRequestResponse requestResponse) {
        RequestEntry entry = new RequestEntry(nextId++, ZonedDateTime.now(), requestResponse, "");
        requests.add(entry);
    }

    public int getRowCount() {
        return requests.size();
    }

    public RequestEntry getEntry(int rowIndex) {
        return requests.get(rowIndex);
    }

    public HttpRequestResponse getRequestAt(int rowIndex) {
        return requests.get(rowIndex).getRequestResponse();
    }
    
    public void setNotes(int rowIndex, String notes) {
        if (rowIndex >= 0 && rowIndex < requests.size()) {
            requests.get(rowIndex).setNotes(notes);
        }
    }
    
    public String getNotes(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < requests.size()) {
            return requests.get(rowIndex).getNotes();
        }
        return "";
    }

    public static class RequestEntry {
        private final int id;
        private final ZonedDateTime time;
        private final HttpRequestResponse requestResponse;
        private String notes;

        public RequestEntry(int id, ZonedDateTime time, HttpRequestResponse requestResponse, String notes) {
            this.id = id;
            this.time = time;
            this.requestResponse = requestResponse;
            this.notes = notes;
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
        
        public String getNotes() {
            return notes;
        }
        
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}