package com.example.minijira.swing.service;

import com.example.minijira.swing.model.*;
import com.example.minijira.swing.repository.ActivityLogRepository;
import com.example.minijira.swing.repository.CommentRepository;
import com.example.minijira.swing.repository.TaskRepository;
import java.sql.*;
import java.util.*;

public class TaskService {
    // TaskService combines task data, comments, and activity history.
    private final TaskRepository taskRepository = new TaskRepository();
    private final CommentRepository commentRepository = new CommentRepository();
    private final ActivityLogRepository activityLogRepository = new ActivityLogRepository();

    public List<Task> loadTasks(Long projectId, TaskStatus status, TaskPriority priority, Long assignedUserId) throws SQLException {
        // Load tasks with optional filters coming from dashboard controls.
        return taskRepository.findByProjectId(projectId, status, priority, assignedUserId);
    }

    public DashboardStats loadStats(Long projectId) throws SQLException {
        // Load summary counts for dashboard cards.
        return taskRepository.loadStats(projectId);
    }

    public Task createTask(Task task, User currentUser) throws SQLException {
        // Viewer cannot create or change data.
        ensureEditor(currentUser);
        // Fill creator fields before saving.
        task.setCreatedById(currentUser.getId());
        task.setCreatedByName(currentUser.getName());
        // If UI does not set a status, start with TODO.
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        Task savedTask = taskRepository.save(task);
        // Every major task action is also written into activity_log.
        activityLogRepository.save(savedTask.getId(), currentUser.getId(), "Created task: " + savedTask.getTitle());
        return savedTask;
    }

    public void updateTask(Task task, User currentUser) throws SQLException {
        // Same permission rule is used for all edit operations.
        ensureEditor(currentUser);
        taskRepository.update(task);
        activityLogRepository.save(task.getId(), currentUser.getId(), "Updated task details");
    }

    public void changeStatus(Task task, TaskStatus nextStatus, User currentUser) throws SQLException {
        ensureEditor(currentUser);
        // Save old status so it can be shown in activity history.
        String previousStatus = task.getStatus().name();
        task.setStatus(nextStatus);
        taskRepository.update(task);
        activityLogRepository.save(task.getId(), currentUser.getId(), "Changed status from " + previousStatus + " to " + nextStatus.name());
    }

    public void deleteTask(Long taskId, User currentUser) throws SQLException {
        // Delete is also restricted to non-viewer roles.
        ensureEditor(currentUser);
        taskRepository.delete(taskId);
    }

    public void addComment(Long taskId, User currentUser, String content) throws SQLException {
        // Comments are saved separately and also recorded in the activity log.
        commentRepository.save(taskId, currentUser.getId(), content.trim());
        activityLogRepository.save(taskId, currentUser.getId(), "Added comment");
    }

    public List<Comment> loadComments(Long taskId) throws SQLException {
        // Load comments for selected task.
        return commentRepository.findByTaskId(taskId);
    }

    public List<ActivityLogEntry> loadActivity(Long taskId) throws SQLException {
        // Load task history for selected task.
        return activityLogRepository.findByTaskId(taskId);
    }

    private void ensureEditor(User currentUser) {
        // Viewer role is read-only in this project.
        if (currentUser.getRole() == Role.VIEWER) {
            throw new IllegalArgumentException("Viewer can only view data.");
        }
    }
}
