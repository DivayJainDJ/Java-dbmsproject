package com.example.minijira.swing.repository;

import com.example.minijira.swing.db.DatabaseConnection;
import com.example.minijira.swing.model.ActivityLogEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogRepository {

    public void save(Long taskId, Long userId, String action) throws SQLException {
        String sql = "INSERT INTO activity_log(task_id, user_id, action) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            statement.setLong(2, userId);
            statement.setString(3, action);
            statement.executeUpdate();
        }
    }

    public List<ActivityLogEntry> findByTaskId(Long taskId) throws SQLException {
        List<ActivityLogEntry> entries = new ArrayList<>();
        String sql = """
            SELECT a.id, a.task_id, a.user_id, a.action, a.created_at, u.name AS user_name
            FROM activity_log a
            JOIN users u ON u.id = a.user_id
            WHERE a.task_id = ?
            ORDER BY a.created_at DESC
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ActivityLogEntry entry = new ActivityLogEntry();
                    entry.setId(resultSet.getLong("id"));
                    entry.setTaskId(resultSet.getLong("task_id"));
                    entry.setUserId(resultSet.getLong("user_id"));
                    entry.setUserName(resultSet.getString("user_name"));
                    entry.setAction(resultSet.getString("action"));
                    entry.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    entries.add(entry);
                }
            }
        }
        return entries;
    }
}
