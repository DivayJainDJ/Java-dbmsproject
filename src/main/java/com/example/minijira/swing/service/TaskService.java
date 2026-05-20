package com.example.minijira.swing.service;

import com.example.minijira.swing.model.ActivityLogEntry;
import com.example.minijira.swing.model.Comment;
import com.example.minijira.swing.model.DashboardStats;
import com.example.minijira.swing.model.Project;
import com.example.minijira.swing.model.Role;
import com.example.minijira.swing.model.Task;
import com.example.minijira.swing.model.TaskPriority;
import com.example.minijira.swing.model.TaskStatus;
import com.example.minijira.swing.model.User;
import com.example.minijira.swing.repository.ActivityLogRepository;
import com.example.minijira.swing.repository.CommentRepository;
import com.example.minijira.swing.repository.TaskRepository;
import java.sql.SQLException;
import java.util.List;

public class TaskService {
    private final TaskRepository taskRepository = new TaskRepository();
    private final CommentRepository commentRepository = new CommentRepository();
    private final ActivityLogRepository activityLogRepository = new ActivityLogRepository();

    public List<Task> loadTasks(Long projectId, TaskStatus status, TaskPriority priority, Long assignedUserId) throws SQLException {
        return taskRepository.findByProjectId(projectId, status, priority, assignedUserId);
    }

    public DashboardStats loadStats(Long projectId) throws SQLException {
        return taskRepository.loadStats(projectId);
    }

    public Task createTask(Task task, User currentUser) throws SQLException {
        ensureEditor(currentUser);
        task.setCreatedById(currentUser.getId());
        task.setCreatedByName(currentUser.getName());
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        Task savedTask = taskRepository.save(task);
        activityLogRepository.save(savedTask.getId(), currentUser.getId(), "Created task: " + savedTask.getTitle());
        return savedTask;
    }

    public void updateTask(Task task, User currentUser) throws SQLException {
        ensureEditor(currentUser);
        taskRepository.update(task);
        activityLogRepository.save(task.getId(), currentUser.getId(), "Updated task details");
    }

    public void changeStatus(Task task, TaskStatus nextStatus, User currentUser) throws SQLException {
        ensureEditor(currentUser);
        String previousStatus = task.getStatus().name();
        task.setStatus(nextStatus);
        taskRepository.update(task);
        activityLogRepository.save(task.getId(), currentUser.getId(), "Changed status from " + previousStatus + " to " + nextStatus.name());
    }

    public void deleteTask(Long taskId, User currentUser) throws SQLException {
        ensureEditor(currentUser);
        taskRepository.delete(taskId);
    }

    public void addComment(Long taskId, User currentUser, String content) throws SQLException {
        commentRepository.save(taskId, currentUser.getId(), content.trim());
        activityLogRepository.save(taskId, currentUser.getId(), "Added comment");
    }

    public List<Comment> loadComments(Long taskId) throws SQLException {
        return commentRepository.findByTaskId(taskId);
    }

    public List<ActivityLogEntry> loadActivity(Long taskId) throws SQLException {
        return activityLogRepository.findByTaskId(taskId);
    }

    public Task buildTask(Project project, String title, String description, TaskPriority priority, TaskStatus status, java.time.LocalDateTime deadline, User assignee) {
        Task task = new Task();
        task.setProjectId(project.getId());
        task.setTitle(title.trim());
        task.setDescription(description.trim());
        task.setPriority(priority);
        task.setStatus(status);
        task.setDeadline(deadline);
        if (assignee != null) {
            task.setAssignedToId(assignee.getId());
            task.setAssignedToName(assignee.getName());
        }
        return task;
    }

    private void ensureEditor(User currentUser) {
        if (currentUser.getRole() == Role.VIEWER) {
            throw new IllegalArgumentException("Viewer can only view data.");
        }
    }
}
