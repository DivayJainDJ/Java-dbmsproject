package com.example.minijira.swing.ui;

import com.example.minijira.swing.model.Role;
import com.example.minijira.swing.model.User;
import com.example.minijira.swing.service.AuthService;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField registerNameField;
    private JTextField registerEmailField;
    private JPasswordField registerPasswordField;
    private JComboBox<Role> roleBox;

    public LoginFrame() {
        // First screen of the application: login and registration.
        setTitle("Mini Jira Tracker");
        setSize(520, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Register", createRegisterPanel());
        add(tabs);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel heading = new JLabel("Mini Jira Tracker", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        panel.add(heading, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        emailField = new JTextField("admin@minijira.com");
        passwordField = new JPasswordField("Password@123");

        formPanel.add(new JLabel("Email"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());

        // Demo buttons help quickly open role-based dashboards during viva/demo.
        JButton adminDemoButton = new JButton("Admin Demo");
        adminDemoButton.addActionListener(e -> demoLogin("admin@minijira.com", "Password@123"));

        JButton developerDemoButton = new JButton("Developer Demo");
        developerDemoButton.addActionListener(e -> demoLogin("dev@minijira.com", "Password@123"));

        JButton viewerDemoButton = new JButton("Viewer Demo");
        viewerDemoButton.addActionListener(e -> demoLogin("viewer@minijira.com", "Password@123"));

        formPanel.add(loginButton);
        formPanel.add(new JLabel(""));
        formPanel.add(adminDemoButton);
        formPanel.add(developerDemoButton);
        formPanel.add(viewerDemoButton);

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        registerNameField = new JTextField();
        registerEmailField = new JTextField();
        registerPasswordField = new JPasswordField();
        roleBox = new JComboBox<>(Role.values());

        formPanel.add(new JLabel("Name"));
        formPanel.add(registerNameField);
        formPanel.add(new JLabel("Email"));
        formPanel.add(registerEmailField);
        formPanel.add(new JLabel("Password"));
        formPanel.add(registerPasswordField);
        formPanel.add(new JLabel("Role"));
        formPanel.add(roleBox);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> register());

        formPanel.add(registerButton);
        formPanel.add(new JLabel(""));

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private void demoLogin(String email, String password) {
        emailField.setText(email);
        passwordField.setText(password);
        login();
    }

    private void login() {
        try {
            // Login is handled by the service layer, not directly by the UI.
            User user = authService.login(emailField.getText().trim(), new String(passwordField.getPassword()));
            DashboardFrame dashboardFrame = new DashboardFrame(user);
            dashboardFrame.setVisible(true);
            dispose();
        } catch (IllegalArgumentException | SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }

    private void register() {
        try {
            User user = new User();
            user.setName(registerNameField.getText().trim());
            user.setEmail(registerEmailField.getText().trim());
            user.setPassword(new String(registerPasswordField.getPassword()));
            user.setRole((Role) roleBox.getSelectedItem());

            // Basic form validation before passing the user to the service layer.
            if (user.getName().isBlank() || user.getEmail().isBlank() || user.getPassword().isBlank()) {
                throw new IllegalArgumentException("All fields are required.");
            }

            authService.register(user);
            JOptionPane.showMessageDialog(this, "Registration successful.");
        } catch (IllegalArgumentException | SQLException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage());
        }
    }
}
