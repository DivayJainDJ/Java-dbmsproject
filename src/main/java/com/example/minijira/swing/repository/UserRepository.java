package com.example.minijira.swing.repository;

import com.example.minijira.swing.db.DatabaseConnection;
import com.example.minijira.swing.model.*;
import java.sql.*;
import java.util.*;

public class UserRepository {

    public Optional<User> findByEmail(String email) throws SQLException {
        // Used by login and registration checks.
        String sql = "SELECT id, name, email, password, role FROM users WHERE email = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // Replace ? with actual email value.
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                // If a row exists, convert it into User object.
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email, password, role FROM users ORDER BY name";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            // Convert every row from ResultSet into User object.
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }
        return users;
    }

    public User save(User user) throws SQLException {
        // Insert a new user and return the generated id.
        String sql = "INSERT INTO users(name, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Set SQL values from the User object.
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole().name());
            statement.executeUpdate();
            // Read auto-generated primary key after insert.
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        }
    }

    public void ensureDefaultUsers() throws SQLException {
        // Seed demo users so Admin/Developer/Viewer login is always available.
        String sql = """
            INSERT INTO users (id, name, email, password, role)
            VALUES
                (1, 'Admin User', 'admin@minijira.com', 'Password@123', 'ADMIN'),
                (2, 'Dev User', 'dev@minijira.com', 'Password@123', 'DEVELOPER'),
                (3, 'Viewer User', 'viewer@minijira.com', 'Password@123', 'VIEWER')
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                email = VALUES(email),
                password = VALUES(password),
                role = VALUES(role)
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // Run once whenever login/register/dashboard needs demo users to exist.
            statement.executeUpdate();
        }
    }

    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT id, name, email, password, role FROM users WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        // Convert one database row into a User object.
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(Role.valueOf(resultSet.getString("role")));
        return user;
    }
}
