package com.example.minijira.swing;

import com.example.minijira.swing.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class MiniJiraSwingApplication {

    private MiniJiraSwingApplication() {
    }

    public static void main(String[] args) {
        // Start the Swing UI on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            try {
                // Use the operating system look and feel to keep the UI familiar.
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LoginFrame().setVisible(true);
        });
    }
}
