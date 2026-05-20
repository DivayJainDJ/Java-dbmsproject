package com.example.minijira.swing.ui;

import com.example.minijira.swing.model.Role;
import com.example.minijira.swing.model.User;
import com.example.minijira.swing.service.AuthService;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();

    private final JTextField loginEmailField = new JTextField("admin@minijira.com");
    private final JPasswordField loginPasswordField = new JPasswordField("Password@123");
    private final JTextField registerNameField = new JTextField();
    private final JTextField registerEmailField = new JTextField();
    private final JPasswordField registerPasswordField = new JPasswordField();
    private final JComboBox<Role> registerRoleBox = new JComboBox<>(Role.values());

    public LoginFrame() {
        setTitle("Mini Jira Tracker - Swing Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(540, 420);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(540, 420));
        setLayout(new BorderLayout(12, 12));

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", buildLoginPanel());
        tabs.addTab("Register", buildRegisterPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 126, 34)),
            BorderFactory.createEmptyBorder(16, 18, 12, 18)
        ));

        JLabel title = new JLabel("Mini Jira Tracker");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Swing + JDBC + MySQL");
        subtitle.setForeground(new Color(95, 106, 106));
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = createFormPanel();
        panel.add(new JLabel("Email"));
        panel.add(loginEmailField);
        panel.add(new JLabel("Password"));
        panel.add(loginPasswordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(event -> handleLogin());
        panel.add(new JLabel());
        panel.add(loginButton);

        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        demoPanel.setBorder(BorderFactory.createTitledBorder("Demo Logins"));
        JButton adminDemoButton = new JButton("Admin Demo");
        JButton developerDemoButton = new JButton("Developer Demo");
        JButton viewerDemoButton = new JButton("Viewer Demo");
        adminDemoButton.addActionListener(event -> loginDemo("admin@minijira.com", "Password@123"));
        developerDemoButton.addActionListener(event -> loginDemo("dev@minijira.com", "Password@123"));
        viewerDemoButton.addActionListener(event -> loginDemo("viewer@minijira.com", "Password@123"));
        demoPanel.add(adminDemoButton);
        demoPanel.add(developerDemoButton);
        demoPanel.add(viewerDemoButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(demoPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = createFormPanel();
        panel.add(new JLabel("Name"));
        panel.add(registerNameField);
        panel.add(new JLabel("Email"));
        panel.add(registerEmailField);
        panel.add(new JLabel("Password"));
        panel.add(registerPasswordField);
        panel.add(new JLabel("Role"));
        panel.add(registerRoleBox);

        JButton registerButton = new JButton("Create Account");
        registerButton.addActionListener(event -> handleRegister());
        panel.add(new JLabel());
        panel.add(registerButton);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        return panel;
    }

    private void fillLogin(String email, String password) {
        loginEmailField.setText(email);
        loginPasswordField.setText(password);
    }

    private void loginDemo(String email, String password) {
        fillLogin(email, password);
        handleLogin();
    }

    private void handleLogin() {
        try {
            User user = authService.login(
                loginEmailField.getText().trim(),
                new String(loginPasswordField.getPassword())
            );
            DashboardFrame dashboardFrame = new DashboardFrame(user);
            dashboardFrame.setVisible(true);
            dispose();
        } catch (IllegalArgumentException | SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        try {
            User user = new User();
            user.setName(registerNameField.getText().trim());
            user.setEmail(registerEmailField.getText().trim());
            user.setPassword(new String(registerPasswordField.getPassword()));
            user.setRole((Role) registerRoleBox.getSelectedItem());

            if (user.getName().isBlank() || user.getEmail().isBlank() || user.getPassword().isBlank()) {
                throw new IllegalArgumentException("All fields are required.");
            }

            authService.register(user);
            JOptionPane.showMessageDialog(this, "Registration successful. Login now with the same credentials.");
        } catch (IllegalArgumentException | SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
