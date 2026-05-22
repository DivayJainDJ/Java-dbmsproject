package com.example.minijira.swing.repository;

import com.example.minijira.swing.db.DatabaseConnection;
import com.example.minijira.swing.model.*;
import java.sql.*;
import java.util.*;

public class CommentRepository {

    public List<Comment> findByTaskId(Long taskId) throws SQLException {
        // Load all comments of one task.
        List<Comment> comments = new ArrayList<>();
        String sql = """
            SELECT c.id, c.task_id, c.user_id, u.name AS user_name, c.content, c.created_at
            FROM comments c
            JOIN users u ON u.id = c.user_id
            WHERE c.task_id = ?
            ORDER BY c.created_at DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, taskId);
            try (ResultSet resultSet = statement.executeQuery()) {
                // Convert every row into a Comment object.
                while (resultSet.next()) {
                    Comment comment = new Comment();
                    comment.setId(resultSet.getLong("id"));
                    comment.setTaskId(resultSet.getLong("task_id"));
                    comment.setUserId(resultSet.getLong("user_id"));
                    comment.setUserName(resultSet.getString("user_name"));
                    comment.setContent(resultSet.getString("content"));
                    comment.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    comments.add(comment);
                }
            }
        }
        return comments;
    }

    public void save(Long taskId, Long userId, String content) throws SQLException {
        // Store one new comment in the comments table.
        String sql = "INSERT INTO comments(task_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            // Set task id, user id, and comment text into SQL query.
            statement.setLong(1, taskId);
            statement.setLong(2, userId);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }
}
