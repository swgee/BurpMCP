package burpmcp.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ServerLogDetailPanel extends JPanel {
    private final JTextArea messageDataArea;

    public ServerLogDetailPanel() {
        setLayout(new BorderLayout());
        
        // Create panel with title
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Message Data", TitledBorder.LEFT, TitledBorder.TOP));
        
        // Create text area for message data
        messageDataArea = new JTextArea();
        messageDataArea.setEditable(false);
        messageDataArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        messageDataArea.setLineWrap(true);
        messageDataArea.setWrapStyleWord(true);
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(messageDataArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setMessageData(String messageData) {
        messageDataArea.setText(messageData);
        messageDataArea.setCaretPosition(0); // Scroll to top
    }
}