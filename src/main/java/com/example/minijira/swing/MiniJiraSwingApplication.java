package com.example.minijira.swing;

import com.example.minijira.swing.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class MiniJiraSwingApplication {

    private MiniJiraSwingApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LoginFrame().setVisible(true);
        });
    }
}
