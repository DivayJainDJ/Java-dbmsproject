package com.example.minijira.swing.config;

public final class AppConfig {

    private AppConfig() {
    }

    // Central place for database settings used by JDBC connection code.
    public static final String DB_URL = "jdbc:mysql://localhost:3306/mini_jira_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DB_USER = "appuser";
    public static final String DB_PASSWORD = "1234";
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
}
