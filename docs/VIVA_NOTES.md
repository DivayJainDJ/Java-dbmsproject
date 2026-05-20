# Viva Notes

## Project summary

Mini Jira Tracker is a role-based task management backend built with Spring Boot and MySQL. It supports project management, task lifecycle tracking, comments, search filters, pagination, and dashboard metrics.

## Why this project is strong

- It is not only CRUD. It includes authentication, authorization, workflow rules, and activity auditing.
- It follows layered architecture: controller, service, repository, and database.
- It uses MySQL foreign keys and indexes for relational consistency and query performance.
- It demonstrates practical backend patterns used in real teams.

## Roles and permissions

- `ADMIN`: full access, including deleting projects and viewing all accessible data.
- `DEVELOPER`: can create projects, create/update tasks, assign tasks, move status, and comment.
- `VIEWER`: read-only access to projects and tasks they can access.

## Security design

- Registration and login are public endpoints.
- Passwords are hashed using BCrypt.
- JWT is returned on login and sent in `Authorization: Bearer <token>`.
- Spring Security filter validates JWT for protected endpoints.

## Task workflow logic

Allowed transitions:

- `TODO -> IN_PROGRESS`
- `IN_PROGRESS -> DONE`

Blocked transitions:

- `TODO -> DONE`
- `DONE -> IN_PROGRESS`
- same-status updates

This enforces a simple Kanban flow similar to Jira boards.

## Activity log purpose

The activity log improves traceability. It stores:

- task creation
- assignment changes
- status changes

This helps answer who changed what and when.

## Search and pagination

- Task listing supports filtering by `status`, `priority`, `assignedUserId`, and `projectId`.
- Pagination is added for project and task list APIs using `page` and `size`.

## Database design explanation

- `users`: stores login identity and role.
- `projects`: top-level work container.
- `project_members`: many-to-many mapping between users and projects.
- `tasks`: actual work items under projects.
- `comments`: collaboration messages on tasks.
- `activity_log`: audit trail for important task events.

## Common viva questions and short answers

### Why did you choose Spring Boot?

It reduces boilerplate, integrates security and JPA cleanly, and is widely used for production Java backends.

### Why MySQL?

The project has strongly relational data with clear foreign key relationships, so MySQL is a good fit.

### Why use DTOs?

DTOs separate API payloads from entity models and help with validation and clean contracts.

### Why service layer between controller and repository?

Business rules like role checks, status transitions, and activity logging belong in the service layer, not in controllers.

### How is authorization handled?

Authentication is via JWT. Authorization is enforced using the authenticated user role plus project membership checks in service methods.

### What makes this scalable?

Layered design, DTO isolation, indexed relational schema, reusable security utilities, and clearly separated modules make it easier to extend.
