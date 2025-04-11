package burpmcp.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.*;

public class SentRequestDetailPanel extends JPanel {
    private final MontoyaApi api;
    private final HttpRequestEditor requestEditor;
    private final HttpResponseEditor responseEditor;

    public SentRequestDetailPanel(MontoyaApi api) {
        this.api = api;
        setLayout(new BorderLayout());
        
        // Create request and response editors
        requestEditor = api.userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY);
        responseEditor = api.userInterface().createHttpResponseEditor(EditorOptions.READ_ONLY);
        
        // Create a split pane for request and response
        JSplitPane editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                                    requestEditor.uiComponent(), 
                                                    responseEditor.uiComponent());
        editorSplitPane.setResizeWeight(0.5); // 50-50 split
        
        // Add the split pane to this panel
        add(editorSplitPane, BorderLayout.CENTER);
    }

    public void setRequest(HttpRequestResponse requestResponse) {
        if (requestResponse != null) {
            // Set request
            requestEditor.setRequest(requestResponse.request());
            
            // Set response if it exists
            if (requestResponse.hasResponse()) {
                responseEditor.setResponse(requestResponse.response());
            } else {
                // Clear the response editor if no response
                responseEditor.setResponse(null);
            }
        }
    }
}