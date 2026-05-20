package com.example.minijira.swing.service;

import com.example.minijira.swing.model.Project;
import com.example.minijira.swing.model.Role;
import com.example.minijira.swing.model.User;
import com.example.minijira.swing.repository.ProjectRepository;
import java.sql.SQLException;
import java.util.List;

public class ProjectService {
    private final ProjectRepository projectRepository = new ProjectRepository();

    public List<Project> loadProjects(User user) throws SQLException {
        return projectRepository.findProjectsVisibleToUser(user);
    }

    public Project createProject(String name, String description, User currentUser) throws SQLException {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only ADMIN can create projects.");
        }
        Project project = new Project();
        project.setName(name.trim());
        project.setDescription(description.trim());
        project.setCreatedBy(currentUser.getId());
        project.setCreatedByName(currentUser.getName());
        Project savedProject = projectRepository.save(project);
        projectRepository.addProjectMember(savedProject.getId(), currentUser.getId());
        return savedProject;
    }

    public void addMember(Project project, User member, User currentUser) throws SQLException {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only ADMIN can add project members.");
        }
        projectRepository.addProjectMember(project.getId(), member.getId());
    }

    public List<User> loadMembers(Long projectId) throws SQLException {
        return projectRepository.findMembersByProjectId(projectId);
    }
}
