package burpmcp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.collaborator.CollaboratorClient;
import burp.api.montoya.collaborator.SecretKey;
import burp.api.montoya.collaborator.Interaction;
import burpmcp.models.SavedRequestListModel;
import burpmcp.models.SentRequestListModel;
import burpmcp.models.ServerLogListModel;

import javax.swing.table.TableRowSorter;
import javax.swing.SortOrder;
import javax.swing.RowSorter;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Handles persistence of BurpMCP's data within the Burp project
 */
public class BurpMCPPersistence {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    
    private final MontoyaApi api;
    private final PersistedObject persistedData;
    
    public BurpMCPPersistence(MontoyaApi api) {
        this.api = api;
        this.persistedData = api.persistence().extensionData();
    }
    
    /**
     * Persists all data models
     */
    public void saveState(SavedRequestListModel savedRequestsModel, SentRequestListModel sentRequestsModel, ServerLogListModel serverLogsModel) {
        // Save saved request logs
        saveSavedRequests(savedRequestsModel);
        
        // Save request logs
        saveRequestLogs(sentRequestsModel);
        
        // Save server logs
        saveServerLogs(serverLogsModel);
    }
    
    /**
     * Restores all data models from persistence
     */
    public void restoreState(SavedRequestListModel savedRequestsModel, SentRequestListModel sentRequestsModel, ServerLogListModel serverLogsModel) {
        // Restore saved requests
        restoreSavedRequests(savedRequestsModel);
        
        // Restore request logs
        restoreRequestLogs(sentRequestsModel);
        
        // Restore server logs
        restoreServerLogs(serverLogsModel);
    }
    
    /**
     * Saves saved request logs
     */
    private void saveSavedRequests(SavedRequestListModel savedRequestsModel) {
        // Get or create the saved requests persisted object
        PersistedObject savedRequestsObj = persistedData.getChildObject("savedRequests");
        if (savedRequestsObj == null) {
            savedRequestsObj = PersistedObject.persistedObject();
            persistedData.setChildObject("savedRequests", savedRequestsObj);
        }
        
        // Create lists for the saved requests data
        PersistedList<HttpRequestResponse> requestResponses = PersistedList.persistedHttpRequestResponseList();
        PersistedList<String> notes = PersistedList.persistedStringList();
        PersistedList<String> times = PersistedList.persistedStringList();
        
        // Populate the lists
        for (int i = 0; i < savedRequestsModel.getRowCount(); i++) {
            SavedRequestListModel.RequestEntry entry = savedRequestsModel.getEntry(i);
            requestResponses.add(entry.getRequestResponse());
            notes.add(entry.getNotes());
            times.add(entry.getTime().format(DATE_TIME_FORMATTER));
        }
        
        // Save the lists to persistence
        savedRequestsObj.setHttpRequestResponseList("requestResponses", requestResponses);
        savedRequestsObj.setStringList("notes", notes);
        savedRequestsObj.setStringList("times", times);
    }
    
    /**
     * Restores saved request logs
     */
    private void restoreSavedRequests(SavedRequestListModel savedRequestsModel) {
        PersistedObject savedRequestsObj = persistedData.getChildObject("savedRequests");
        if (savedRequestsObj == null) return;
        
        // Retrieve the lists
        PersistedList<HttpRequestResponse> requestResponses = savedRequestsObj.getHttpRequestResponseList("requestResponses");
        PersistedList<String> notes = savedRequestsObj.getStringList("notes");
        PersistedList<String> times = savedRequestsObj.getStringList("times");
        
        if (requestResponses == null || notes == null || times == null ||
            requestResponses.size() != notes.size() || notes.size() != times.size()) {
            return;
        }
        
        // Loop through and add each request to the model
        for (int i = 0; i < requestResponses.size(); i++) {
            // Add the request to the model
            savedRequestsModel.addRequest(requestResponses.get(i), ZonedDateTime.parse(times.get(i), DATE_TIME_FORMATTER), notes.get(i));
            
            // Set the notes for the entry (last entry added)
            int lastIndex = savedRequestsModel.getRowCount() - 1;
            savedRequestsModel.setNotes(lastIndex, notes.get(i));
        }
    }
    
    /**
     * Saves request logs
     */
    private void saveRequestLogs(SentRequestListModel sentRequestsModel) {
        // Get or create the sent requests persisted object
        PersistedObject sentRequestsObj = persistedData.getChildObject("sentRequests");
        if (sentRequestsObj == null) {
            sentRequestsObj = PersistedObject.persistedObject();
            persistedData.setChildObject("sentRequests", sentRequestsObj);
        }
        
        // Create lists for the sent requests data
        PersistedList<HttpRequestResponse> requestResponses = PersistedList.persistedHttpRequestResponseList();
        PersistedList<String> times = PersistedList.persistedStringList();
        
        // Populate the lists
        for (int i = 0; i < sentRequestsModel.getRowCount(); i++) {
            SentRequestListModel.SentRequestEntry entry = sentRequestsModel.getEntry(i);
            requestResponses.add(entry.getRequestResponse());
            times.add(entry.getTime().format(DATE_TIME_FORMATTER));
        }
        
        // Save the lists to persistence
        sentRequestsObj.setHttpRequestResponseList("requestResponses", requestResponses);
        sentRequestsObj.setStringList("times", times);
    }
    
    /**
     * Restores request logs
     */
    private void restoreRequestLogs(SentRequestListModel sentRequestsModel) {
        PersistedObject sentRequestsObj = persistedData.getChildObject("sentRequests");
        if (sentRequestsObj == null) return;
        
        // Retrieve the lists
        PersistedList<HttpRequestResponse> requestResponses = sentRequestsObj.getHttpRequestResponseList("requestResponses");
        PersistedList<String> times = sentRequestsObj.getStringList("times");
        
        if (requestResponses == null || times == null || requestResponses.size() != times.size()) {
            return;
        }
        
        // Loop through and add each request to the model
        for (int i = 0; i < requestResponses.size(); i++) {
            // Add the request to the model
            sentRequestsModel.addRequest(requestResponses.get(i), ZonedDateTime.parse(times.get(i), DATE_TIME_FORMATTER));
        }
    }
    
    /**
     * Saves server logs
     */
    private void saveServerLogs(ServerLogListModel serverLogsModel) {
        // Get or create the server logs persisted object
        PersistedObject serverLogsObj = persistedData.getChildObject("serverLogs");
        if (serverLogsObj == null) {
            serverLogsObj = PersistedObject.persistedObject();
            persistedData.setChildObject("serverLogs", serverLogsObj);
        }
        
        // Create lists for the server logs data
        PersistedList<String> directions = PersistedList.persistedStringList();
        PersistedList<String> clients = PersistedList.persistedStringList();
        PersistedList<String> tools = PersistedList.persistedStringList();
        PersistedList<String> messageDatas = PersistedList.persistedStringList();
        PersistedList<String> times = PersistedList.persistedStringList();
        
        // Populate the lists
        for (int i = 0; i < serverLogsModel.getRowCount(); i++) {
            ServerLogListModel.ServerLogEntry entry = serverLogsModel.getEntry(i);
            directions.add(entry.getDirection());
            clients.add(entry.getClient());
            tools.add(entry.getTool());
            messageDatas.add(entry.getMessageData());
            times.add(entry.getTime().format(DATE_TIME_FORMATTER));
        }
        
        // Save the lists to persistence
        serverLogsObj.setStringList("directions", directions);
        serverLogsObj.setStringList("clients", clients);
        serverLogsObj.setStringList("tools", tools);
        serverLogsObj.setStringList("messageDatas", messageDatas);
        serverLogsObj.setStringList("times", times);
    }
    
    /**
     * Restores server logs
     */
    private void restoreServerLogs(ServerLogListModel serverLogsModel) {
        PersistedObject serverLogsObj = persistedData.getChildObject("serverLogs");
        if (serverLogsObj == null) return;
        
        // Retrieve the lists
        PersistedList<String> directions = serverLogsObj.getStringList("directions");
        PersistedList<String> clients = serverLogsObj.getStringList("clients");
        PersistedList<String> tools = serverLogsObj.getStringList("tools"); // For backward compatibility
        PersistedList<String> messageDatas = serverLogsObj.getStringList("messageDatas");
        PersistedList<String> times = serverLogsObj.getStringList("times");
        
        // Verify all required lists exist and have the same size
        if (directions == null || clients == null || tools == null || 
            messageDatas == null || times == null ||
            directions.size() != clients.size() || clients.size() != tools.size() || 
            tools.size() != messageDatas.size() || messageDatas.size() != times.size()) {
            return;
        }
        
        // Loop through and add each log to the model
        for (int i = 0; i < directions.size(); i++) {
            serverLogsModel.addLog(
                ZonedDateTime.parse(times.get(i), DATE_TIME_FORMATTER),
                directions.get(i),
                clients.get(i),
                tools.get(i),
                messageDatas.get(i)
            );
        }
    }


    /**
     * Saves server configuration (host and port)
     */
    public void saveServerConfig(String host, int port, boolean crlfReplace, boolean mcpServerEnabled) {
        // Get or create the config persisted object
        PersistedObject configObj = persistedData.getChildObject("config");
        if (configObj == null) {
            configObj = PersistedObject.persistedObject();
            persistedData.setChildObject("config", configObj);
        }
        
        // Save host and port
        configObj.setString("host", host);
        configObj.setInteger("port", port);
        configObj.setBoolean("crlfReplace", crlfReplace);
        configObj.setBoolean("mcpServerEnabled", mcpServerEnabled);
    }
    
    /**
     * Restores server configuration (host and port)
     * 
     * @return Object array containing [host, port, crlfReplace] or null if not saved
     */
    public Object[] restoreServerConfig() {
        PersistedObject configObj = persistedData.getChildObject("config");
        if (configObj == null) {
            return null;
        }
        
        String host = configObj.getString("host");
        Integer port = configObj.getInteger("port");
        Boolean crlfReplace = configObj.getBoolean("crlfReplace");
        Boolean mcpServerEnabled = configObj.getBoolean("mcpServerEnabled");

        return new Object[] { host, port, crlfReplace != null ? crlfReplace : false, mcpServerEnabled != null ? mcpServerEnabled : false };
    }

    public CollaboratorClient restoreCollaboratorClient() {
        String restoredSecretKey = persistedData.getString("collaboratorClientSecretKey");
        if (restoredSecretKey == null) {
            CollaboratorClient newCollaboratorClient = api.collaborator().createClient();
            SecretKey newSecretKey = newCollaboratorClient.getSecretKey();
            persistedData.setString("collaboratorClientSecretKey", newSecretKey.toString());
            return newCollaboratorClient;
        }
        return api.collaborator().restoreClient(SecretKey.secretKey(restoredSecretKey));
    }

    public void saveRetrievedInteractions(List<String[]> retrievedInteractions) {
        PersistedObject retrievedInteractionsObj = persistedData.getChildObject("retrievedInteractions");
        if (retrievedInteractionsObj == null) {
            retrievedInteractionsObj = PersistedObject.persistedObject();
            persistedData.setChildObject("retrievedInteractions", retrievedInteractionsObj);
        }
        Integer index = 0;
        for (String[] interaction : retrievedInteractions) {
            PersistedList<String> interactionList = PersistedList.persistedStringList();
            for (String field : interaction) {
                interactionList.add(field);
            }
            retrievedInteractionsObj.setStringList("retrievedInteraction"+index.toString(), interactionList);
            index++;
        }
    }

    public List<String[]> restoreRetrievedInteractions() {
        PersistedObject retrievedInteractionsObj = persistedData.getChildObject("retrievedInteractions");
        if (retrievedInteractionsObj == null) {
            return new ArrayList<>();
        }
        Integer index = 0;
        List<String[]> retrievedInteractions = new ArrayList<>();
        while (retrievedInteractionsObj.getStringList("retrievedInteraction"+index.toString()) != null) {
            PersistedList<String> retrievedInteractionsList = retrievedInteractionsObj.getStringList("retrievedInteraction"+index.toString());
            retrievedInteractions.add(retrievedInteractionsList.toArray(new String[0]));
            index++;
        }
        return retrievedInteractions;
    }

    /**
     * Saves table sorting state for a specific table
     */
    public void saveTableSortingState(String tableKey, TableRowSorter<?> sorter) {
        if (sorter == null) return;
        
        PersistedObject sortingObj = persistedData.getChildObject("tableSorting");
        if (sortingObj == null) {
            sortingObj = PersistedObject.persistedObject();
            persistedData.setChildObject("tableSorting", sortingObj);
        }
        
        List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();
        if (sortKeys != null && !sortKeys.isEmpty()) {
            RowSorter.SortKey primarySortKey = sortKeys.get(0);
            sortingObj.setInteger(tableKey + "_sortColumn", primarySortKey.getColumn());
            sortingObj.setString(tableKey + "_sortOrder", primarySortKey.getSortOrder().toString());
        } else {
            // Clear sorting state if no sort keys
            sortingObj.setInteger(tableKey + "_sortColumn", -1);
            sortingObj.setString(tableKey + "_sortOrder", "UNSORTED");
        }
    }
    
    /**
     * Restores table sorting state for a specific table
     */
    public void restoreTableSortingState(String tableKey, TableRowSorter<?> sorter) {
        if (sorter == null) return;
        
        PersistedObject sortingObj = persistedData.getChildObject("tableSorting");
        if (sortingObj == null) return;
        
        Integer sortColumn = sortingObj.getInteger(tableKey + "_sortColumn");
        String sortOrderStr = sortingObj.getString(tableKey + "_sortOrder");
        
        if (sortColumn != null && sortColumn >= 0 && sortOrderStr != null && !sortOrderStr.equals("UNSORTED")) {
            try {
                SortOrder sortOrder = SortOrder.valueOf(sortOrderStr);
                List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                sortKeys.add(new RowSorter.SortKey(sortColumn, sortOrder));
                sorter.setSortKeys(sortKeys);
            } catch (Exception e) {
                // Ignore invalid sort order values
            }
        }
    }
}