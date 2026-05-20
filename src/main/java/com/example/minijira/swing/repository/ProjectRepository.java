package com.example.minijira.swing.repository;

import com.example.minijira.swing.db.DatabaseConnection;
import com.example.minijira.swing.model.Project;
import com.example.minijira.swing.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    public List<Project> findProjectsVisibleToUser(User user) throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = """
            SELECT p.id, p.name, p.description, p.created_by, u.name AS owner_name,
                   COUNT(pm.user_id) AS member_count
            FROM projects p
            JOIN users u ON u.id = p.created_by
            LEFT JOIN project_members pm ON pm.project_id = p.id
            WHERE (? = 'ADMIN')
               OR p.created_by = ?
               OR EXISTS (
                   SELECT 1 FROM project_members x
                   WHERE x.project_id = p.id AND x.user_id = ?
               )
            GROUP BY p.id, p.name, p.description, p.created_by, u.name
            ORDER BY p.created_at DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getRole().name());
            statement.setLong(2, user.getId());
            statement.setLong(3, user.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(mapProject(resultSet));
                }
            }
        }
        return projects;
    }

    public Project save(Project project) throws SQLException {
        String sql = "INSERT INTO projects(name, description, created_by) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, project.getName());
            statement.setString(2, project.getDescription());
            statement.setLong(3, project.getCreatedBy());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    project.setId(keys.getLong(1));
                }
            }
        }
        return project;
    }

    public void addProjectMember(Long projectId, Long userId) throws SQLException {
        String sql = "INSERT INTO project_members(project_id, user_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE user_id = VALUES(user_id)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    public List<User> findMembersByProjectId(Long projectId) throws SQLException {
        List<User> members = new ArrayList<>();
        String sql = """
            SELECT u.id, u.name, u.email, u.password, u.role
            FROM project_members pm
            JOIN users u ON u.id = pm.user_id
            WHERE pm.project_id = ?
            ORDER BY u.name
            """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User member = new User();
                    member.setId(resultSet.getLong("id"));
                    member.setName(resultSet.getString("name"));
                    member.setEmail(resultSet.getString("email"));
                    member.setPassword(resultSet.getString("password"));
                    member.setRole(com.example.minijira.swing.model.Role.valueOf(resultSet.getString("role")));
                    members.add(member);
                }
            }
        }
        return members;
    }

    private Project mapProject(ResultSet resultSet) throws SQLException {
        Project project = new Project();
        project.setId(resultSet.getLong("id"));
        project.setName(resultSet.getString("name"));
        project.setDescription(resultSet.getString("description"));
        project.setCreatedBy(resultSet.getLong("created_by"));
        project.setCreatedByName(resultSet.getString("owner_name"));
        project.setMemberCount(resultSet.getInt("member_count"));
        return project;
    }
}
