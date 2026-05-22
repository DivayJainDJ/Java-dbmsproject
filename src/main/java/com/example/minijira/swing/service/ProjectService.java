package com.example.minijira.swing.service;

import com.example.minijira.swing.model.*;
import com.example.minijira.swing.repository.ProjectRepository;
import java.sql.*;
import java.util.*;

public class ProjectService {
    // ProjectService talks to ProjectRepository for project-related SQL work.
    private final ProjectRepository projectRepository = new ProjectRepository();

    public List<Project> loadProjects(User user) throws SQLException {
        // Load only those projects which the logged-in user is allowed to see.
        return projectRepository.findProjectsVisibleToUser(user);
    }

    public Project createProject(String name, String description, User currentUser) throws SQLException {
        // Project creation is restricted to admin.
        ensureAdmin(currentUser, "Only ADMIN can create projects.");
        // Create a Project object from UI values.
        Project project = new Project();
        project.setName(name.trim());
        project.setDescription(description.trim());
        project.setCreatedBy(currentUser.getId());
        project.setCreatedByName(currentUser.getName());
        // Save project first, then add admin as the first member.
        Project savedProject = projectRepository.save(project);
        projectRepository.addProjectMember(savedProject.getId(), currentUser.getId());
        return savedProject;
    }

    public void addMember(Project project, User member, User currentUser) throws SQLException {
        // Member management is also restricted to admin.
        ensureAdmin(currentUser, "Only ADMIN can add project members.");
        // Add selected user to the selected project.
        projectRepository.addProjectMember(project.getId(), member.getId());
    }

    public List<User> loadMembers(Long projectId) throws SQLException {
        // Load all members of one project for member tab and task assignee selection.
        return projectRepository.findMembersByProjectId(projectId);
    }

    private void ensureAdmin(User currentUser, String message) {
        // Common role check reused by admin-only actions.
        if (currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException(message);
        }
    }
}
