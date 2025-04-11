package burpmcp.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import burpmcp.models.ResourceListModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ResourceDetailPanel extends JPanel {
    private final MontoyaApi api;
    private final HttpRequestEditor requestEditor;
    private final HttpResponseEditor responseEditor;
    private final JTextArea notesTextArea;
    private int currentRowIndex = -1;
    private ResourceListModel resourceListModel;

    public ResourceDetailPanel(MontoyaApi api) {
        this.api = api;
        setLayout(new BorderLayout());
        
        // Create a panel for the notes section
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Notes", TitledBorder.LEFT, TitledBorder.TOP));
        
        notesTextArea = new JTextArea();
        notesTextArea.setLineWrap(true);
        notesTextArea.setWrapStyleWord(true);
        
        // Add a focus listener to save notes when focus is lost
        notesTextArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                saveNotes();
            }
        });
        
        JScrollPane notesScrollPane = new JScrollPane(notesTextArea);
        notesPanel.add(notesScrollPane, BorderLayout.CENTER);
        notesPanel.setPreferredSize(new Dimension(200, 0)); // Set preferred width
        
        // Create request and response editors
        requestEditor = api.userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY);
        responseEditor = api.userInterface().createHttpResponseEditor(EditorOptions.READ_ONLY);
        
        // Create a split pane for request and response
        JSplitPane editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                                    requestEditor.uiComponent(), 
                                                    responseEditor.uiComponent());
        editorSplitPane.setResizeWeight(0.5); // 50-50 split
        
        // Create a split pane to divide notes and request/response view
        JSplitPane detailSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, notesPanel, editorSplitPane);
        detailSplitPane.setResizeWeight(0.2); // Give 20% to notes panel
        
        // Add the split pane to this panel
        add(detailSplitPane, BorderLayout.CENTER);
    }

    public void setRequest(HttpRequestResponse requestResponse, int rowIndex, ResourceListModel model) {
        if (requestResponse != null) {
            this.currentRowIndex = rowIndex;
            this.resourceListModel = model;
            
            // Set request
            requestEditor.setRequest(requestResponse.request());
            
            // Set response if it exists
            if (requestResponse.hasResponse()) {
                responseEditor.setResponse(requestResponse.response());
            } else {
                // Clear the response editor if no response
                responseEditor.setResponse(null);
            }
            
            notesTextArea.setText(model.getNotes(rowIndex));
        }
    }
    
    private void saveNotes() {
        if (currentRowIndex >= 0 && resourceListModel != null) {
            resourceListModel.setNotes(currentRowIndex, notesTextArea.getText());
        }
    }
}