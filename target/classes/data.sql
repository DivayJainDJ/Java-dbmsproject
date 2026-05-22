-- Use the Mini Jira database created by schema.sql
USE mini_jira_db;

-- Default users for easy login and viva demo.
INSERT INTO users (id, name, email, password, role)
VALUES
    (1, 'Admin User', 'admin@minijira.com', 'Password@123', 'ADMIN'),
    (2, 'Dev User', 'dev@minijira.com', 'Password@123', 'DEVELOPER'),
    (3, 'Viewer User', 'viewer@minijira.com', 'Password@123', 'VIEWER')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    email = VALUES(email),
    password = VALUES(password),
    role = VALUES(role);

-- Default project used to demonstrate project and task features.
INSERT INTO projects (id, name, description, created_by)
VALUES
    (1, 'Mini Jira Backend', 'Spring Boot and MySQL backend for task tracking', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    created_by = VALUES(created_by);

-- Add all default users to the sample project.
INSERT INTO project_members (id, project_id, user_id)
VALUES
    (1, 1, 1),
    (2, 1, 2),
    (3, 1, 3)
ON DUPLICATE KEY UPDATE
    project_id = VALUES(project_id),
    user_id = VALUES(user_id);

-- Sample tasks for showing status workflow and dashboard counts.
INSERT INTO tasks (id, title, description, status, priority, deadline, project_id, assigned_to, created_by)
VALUES
    (1, 'Design schema', 'Create MySQL schema for Mini Jira', 'DONE', 'HIGH', '2026-04-10 18:00:00', 1, 2, 1),
    (2, 'Build auth module', 'Implement login and registration APIs', 'IN_PROGRESS', 'HIGH', '2026-04-12 18:00:00', 1, 2, 1),
    (3, 'Create task board API', 'Expose filtered task listing endpoints', 'TODO', 'MEDIUM', '2026-04-15 18:00:00', 1, 2, 1)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    status = VALUES(status),
    priority = VALUES(priority),
    deadline = VALUES(deadline),
    project_id = VALUES(project_id),
    assigned_to = VALUES(assigned_to),
    created_by = VALUES(created_by);

-- Sample comments for task discussion history.
INSERT INTO comments (id, task_id, user_id, content)
VALUES
    (1, 2, 1, 'Start with JWT and password hashing.'),
    (2, 2, 2, 'Repository and DTOs are next.')
ON DUPLICATE KEY UPDATE
    task_id = VALUES(task_id),
    user_id = VALUES(user_id),
    content = VALUES(content);

-- Sample activity log entries for showing task history.
INSERT INTO activity_log (id, task_id, user_id, action)
VALUES
    (1, 1, 1, 'Created task'),
    (2, 1, 2, 'Changed status from TODO to DONE'),
    (3, 2, 1, 'Assigned task to Dev User')
ON DUPLICATE KEY UPDATE
    task_id = VALUES(task_id),
    user_id = VALUES(user_id),
    action = VALUES(action);
