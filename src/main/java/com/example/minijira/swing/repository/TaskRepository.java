package com.example.minijira.swing.repository;

import com.example.minijira.swing.db.DatabaseConnection;
import com.example.minijira.swing.model.*;
import java.sql.*;
import java.util.*;

public class TaskRepository {

    // Load tasks for one project with optional filters.
    public List<Task> findByProjectId(Long projectId, TaskStatus status, TaskPriority priority, Long assignedUserId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT t.id, t.title, t.description, t.status, t.priority, t.deadline, t.project_id,
                   t.assigned_to, au.name AS assigned_to_name, t.created_by, cu.name AS created_by_name,
                   t.created_at, t.updated_at
            FROM tasks t
            LEFT JOIN users au ON au.id = t.assigned_to
            JOIN users cu ON cu.id = t.created_by
            WHERE t.project_id = ?
            """);
        if (status != null) {
            // Add status condition only when user selected a specific status.
            sql.append(" AND t.status = ? ");
        }
        if (priority != null) {
            // Add priority condition only when user selected a specific priority.
            sql.append(" AND t.priority = ? ");
        }
        if (assignedUserId != null) {
            // Add assignee condition only when user selected a specific member.
            sql.append(" AND t.assigned_to = ? ");
        }
        // Order tasks by workflow stage and then by deadline.
        sql.append(" ORDER BY FIELD(t.status, 'TODO', 'IN_PROGRESS', 'DONE'), t.deadline ASC, t.id DESC ");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            // Parameter index is moved step by step because some filters are optional.
            int index = 1;
            statement.setLong(index++, projectId);
            if (status != null) {
                statement.setString(index++, status.name());
            }
            if (priority != null) {
                statement.setString(index++, priority.name());
            }
            if (assignedUserId != null) {
                statement.setLong(index, assignedUserId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                // Convert each returned row into a Task object.
                while (resultSet.next()) {
                    tasks.add(mapTask(resultSet));
                }
            }
        }
        return tasks;
    }

    public Task save(Task task) throws SQLException {
        // Insert task into database and return generated id.
        String sql = """
            INSERT INTO tasks(title, description, status, priority, deadline, project_id, assigned_to, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillTaskStatement(task, statement, false);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getLong(1));
                }
            }
        }
        return task;
    }

    public void update(Task task) throws SQLException {
        // Used for both edit-task and move-status actions.
        String sql = """
            UPDATE tasks
            SET title = ?, description = ?, status = ?, priority = ?, deadline = ?, assigned_to = ?
            WHERE id = ?
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillTaskStatement(task, statement, true);
            statement.executeUpdate();
        }
    }

    public void delete(Long taskId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            // Remove one task by primary key.
            statement.setLong(1, taskId);
            statement.executeUpdate();
        }
    }

    public DashboardStats loadStats(Long projectId) throws SQLException {
        // Dashboard counts are calculated directly in SQL for simplicity.
        DashboardStats stats = new DashboardStats();
        String sql = """
            SELECT
                COUNT(*) AS total_tasks,
                SUM(CASE WHEN status = 'TODO' THEN 1 ELSE 0 END) AS todo_tasks,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_tasks,
                SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done_tasks
            FROM tasks
            WHERE project_id = ?
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                // Only one row is returned because SQL is using COUNT and SUM.
                if (resultSet.next()) {
                    stats.setTotalTasks(resultSet.getInt("total_tasks"));
                    stats.setTodoTasks(resultSet.getInt("todo_tasks"));
                    stats.setInProgressTasks(resultSet.getInt("in_progress_tasks"));
                    stats.setDoneTasks(resultSet.getInt("done_tasks"));
                }
            }
        }
        return stats;
    }

    private void fillTaskStatement(Task task, PreparedStatement statement, boolean updating) throws SQLException {
        // Shared JDBC parameter setup for insert and update.
        statement.setString(1, task.getTitle());
        statement.setString(2, task.getDescription());
        statement.setString(3, task.getStatus().name());
        statement.setString(4, task.getPriority().name());
        statement.setTimestamp(5, task.getDeadline() == null ? null : Timestamp.valueOf(task.getDeadline()));
        if (!updating) {
            // Insert case: project id, assignee, and creator id are needed.
            statement.setLong(6, task.getProjectId());
            setAssignedUser(statement, 7, task.getAssignedToId());
            statement.setLong(8, task.getCreatedById());
        } else {
            // Update case: use task id in WHERE clause.
            setAssignedUser(statement, 6, task.getAssignedToId());
            statement.setLong(7, task.getId());
        }
    }

    private void setAssignedUser(PreparedStatement statement, int index, Long assignedToId) throws SQLException {
        // Handle both assigned and unassigned tasks.
        if (assignedToId == null) {
            statement.setNull(index, java.sql.Types.BIGINT);
        } else {
            statement.setLong(index, assignedToId);
        }
    }

    private Task mapTask(ResultSet resultSet) throws SQLException {
        // Convert one database row into a Task object.
        Task task = new Task();
        task.setId(resultSet.getLong("id"));
        task.setTitle(resultSet.getString("title"));
        task.setDescription(resultSet.getString("description"));
        task.setStatus(TaskStatus.valueOf(resultSet.getString("status")));
        task.setPriority(TaskPriority.valueOf(resultSet.getString("priority")));
        Timestamp deadline = resultSet.getTimestamp("deadline");
        if (deadline != null) {
            task.setDeadline(deadline.toLocalDateTime());
        }
        task.setProjectId(resultSet.getLong("project_id"));
        long assignedId = resultSet.getLong("assigned_to");
        // getLong returns 0 when SQL value is NULL, so wasNull() is checked.
        if (!resultSet.wasNull()) {
            task.setAssignedToId(assignedId);
        }
        task.setAssignedToName(resultSet.getString("assigned_to_name"));
        task.setCreatedById(resultSet.getLong("created_by"));
        task.setCreatedByName(resultSet.getString("created_by_name"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            task.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            task.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return task;
    }
}
