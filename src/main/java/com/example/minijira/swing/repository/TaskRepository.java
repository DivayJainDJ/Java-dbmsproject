package com.example.minijira.swing.repository;

import com.example.minijira.swing.db.DatabaseConnection;
import com.example.minijira.swing.model.DashboardStats;
import com.example.minijira.swing.model.Task;
import com.example.minijira.swing.model.TaskPriority;
import com.example.minijira.swing.model.TaskStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

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
            sql.append(" AND t.status = ? ");
        }
        if (priority != null) {
            sql.append(" AND t.priority = ? ");
        }
        if (assignedUserId != null) {
            sql.append(" AND t.assigned_to = ? ");
        }
        sql.append(" ORDER BY FIELD(t.status, 'TODO', 'IN_PROGRESS', 'DONE'), t.deadline ASC, t.id DESC ");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
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
                while (resultSet.next()) {
                    tasks.add(mapTask(resultSet));
                }
            }
        }
        return tasks;
    }

    public Task save(Task task) throws SQLException {
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
            statement.setLong(1, taskId);
            statement.executeUpdate();
        }
    }

    public DashboardStats loadStats(Long projectId) throws SQLException {
        DashboardStats stats = new DashboardStats();
        String sql = """
            SELECT
                COUNT(*) AS total_tasks,
                SUM(CASE WHEN status = 'TODO' THEN 1 ELSE 0 END) AS todo_tasks,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_tasks,
                SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done_tasks,
                SUM(CASE WHEN priority = 'HIGH' THEN 1 ELSE 0 END) AS high_priority_tasks,
                SUM(CASE WHEN priority = 'MEDIUM' THEN 1 ELSE 0 END) AS medium_priority_tasks,
                SUM(CASE WHEN priority = 'LOW' THEN 1 ELSE 0 END) AS low_priority_tasks
            FROM tasks
            WHERE project_id = ?
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    stats.setTotalTasks(resultSet.getInt("total_tasks"));
                    stats.setTodoTasks(resultSet.getInt("todo_tasks"));
                    stats.setInProgressTasks(resultSet.getInt("in_progress_tasks"));
                    stats.setDoneTasks(resultSet.getInt("done_tasks"));
                    stats.setHighPriorityTasks(resultSet.getInt("high_priority_tasks"));
                    stats.setMediumPriorityTasks(resultSet.getInt("medium_priority_tasks"));
                    stats.setLowPriorityTasks(resultSet.getInt("low_priority_tasks"));
                }
            }
        }
        return stats;
    }

    private void fillTaskStatement(Task task, PreparedStatement statement, boolean updating) throws SQLException {
        statement.setString(1, task.getTitle());
        statement.setString(2, task.getDescription());
        statement.setString(3, task.getStatus().name());
        statement.setString(4, task.getPriority().name());
        statement.setTimestamp(5, task.getDeadline() == null ? null : Timestamp.valueOf(task.getDeadline()));
        if (!updating) {
            statement.setLong(6, task.getProjectId());
            if (task.getAssignedToId() == null) {
                statement.setNull(7, java.sql.Types.BIGINT);
            } else {
                statement.setLong(7, task.getAssignedToId());
            }
            statement.setLong(8, task.getCreatedById());
        } else {
            if (task.getAssignedToId() == null) {
                statement.setNull(6, java.sql.Types.BIGINT);
            } else {
                statement.setLong(6, task.getAssignedToId());
            }
            statement.setLong(7, task.getId());
        }
    }

    private Task mapTask(ResultSet resultSet) throws SQLException {
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
