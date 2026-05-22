package com.example.minijira.swing.db;

import com.example.minijira.swing.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC driver exactly as commonly taught in college JDBC.
            Class.forName(AppConfig.DB_DRIVER);
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL JDBC Driver not found in classpath.", exception);
        }

        // Open a fresh connection whenever a repository needs database access.
        return DriverManager.getConnection(
            AppConfig.DB_URL,
            AppConfig.DB_USER,
            AppConfig.DB_PASSWORD
        );
    }
}
