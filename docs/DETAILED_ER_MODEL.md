# Detailed ER Model

This document describes the Mini Jira data model in a viva-friendly ER format, including:

- strong and associative entities
- key attributes
- foreign keys
- optional and mandatory participation
- derived attributes
- cardinality
- what should be shown with single line and double line in a handwritten ER diagram

## 1. Entities

### 1. User

Strong entity

Attributes:

- `id` : primary key
- `name` : simple attribute
- `email` : simple attribute, unique
- `password` : simple attribute
- `role` : simple attribute with domain `{ADMIN, DEVELOPER, VIEWER}`
- `created_at` : simple attribute

Suggested derived attributes for ER explanation:

- `project_count` : derived from projects created or joined
- `assigned_task_count` : derived from tasks assigned to the user

### 2. Project

Strong entity

Attributes:

- `id` : primary key
- `name` : simple attribute
- `description` : simple attribute
- `created_by` : foreign key to `User.id`
- `created_at` : simple attribute

Suggested derived attributes:

- `member_count` : derived from `Project_Member`
- `task_count` : derived from `Task`

### 3. Project_Member

Associative entity between `User` and `Project`

Attributes:

- `id` : primary key
- `project_id` : foreign key to `Project.id`
- `user_id` : foreign key to `User.id`
- `added_at` : simple attribute

Notes:

- This resolves the many-to-many relationship between users and projects.
- In ER explanation, this is often shown as an associative entity.

### 4. Task

Strong entity

Attributes:

- `id` : primary key
- `title` : simple attribute
- `description` : simple attribute
- `status` : simple attribute with domain `{TODO, IN_PROGRESS, DONE}`
- `priority` : simple attribute with domain `{LOW, MEDIUM, HIGH}`
- `deadline` : simple attribute
- `project_id` : foreign key to `Project.id`
- `assigned_to` : foreign key to `User.id`, optional
- `created_by` : foreign key to `User.id`
- `created_at` : simple attribute
- `updated_at` : simple attribute

Suggested derived attributes:

- `is_overdue` : derived from current date and deadline
- `task_age` : derived from current date and created_at

### 5. Comment

Strong entity

Attributes:

- `id` : primary key
- `task_id` : foreign key to `Task.id`
- `user_id` : foreign key to `User.id`
- `content` : simple attribute
- `created_at` : simple attribute

Suggested derived attribute:

- `comment_length` : derived from content

### 6. Activity_Log

Strong entity

Attributes:

- `id` : primary key
- `task_id` : foreign key to `Task.id`
- `user_id` : foreign key to `User.id`
- `action` : simple attribute
- `created_at` : simple attribute

Suggested derived attributes:

- `activity_day` : derived from `created_at`

## 2. Relationships

### A. User CREATES Project

- Relationship: `CREATES`
- Cardinality: `User (1) -> (M) Project`
- Participation:
  - Project has total participation in `CREATES`
  - User has partial participation

How to draw:

- `Project` side should be double line because every project must have one creator.
- `User` side can be single line because a user may create zero or many projects.

### B. User JOINS Project through Project_Member

- Relationship type: many-to-many resolved by associative entity
- Cardinality:
  - `User (1) -> (M) Project_Member`
  - `Project (1) -> (M) Project_Member`

Participation:

- `Project_Member` has total participation with both `User` and `Project`
- `User` and `Project` have partial participation

How to draw:

- `Project_Member` to both `User` and `Project` should be double line.

### C. Project HAS Task

- Relationship: `HAS`
- Cardinality: `Project (1) -> (M) Task`

Participation:

- `Task` has total participation because every task must belong to one project
- `Project` has partial participation because a project may exist before tasks are added

How to draw:

- `Task` side double line
- `Project` side single line

### D. User CREATES Task

- Relationship: `CREATES`
- Cardinality: `User (1) -> (M) Task`

Participation:

- `Task` total participation because every task must have one creator
- `User` partial participation

How to draw:

- `Task` side double line
- `User` side single line

### E. User ASSIGNED_TO Task

- Relationship: `ASSIGNED_TO`
- Cardinality: `User (1) -> (M) Task`

Participation:

- `Task` partial participation because `assigned_to` can be null
- `User` partial participation because a user may have zero assigned tasks

How to draw:

- single line on both sides

### F. Task HAS Comment

- Relationship: `HAS`
- Cardinality: `Task (1) -> (M) Comment`

Participation:

- `Comment` total participation because every comment belongs to one task
- `Task` partial participation because a task may have no comments

How to draw:

- `Comment` side double line
- `Task` side single line

### G. User WRITES Comment

- Relationship: `WRITES`
- Cardinality: `User (1) -> (M) Comment`

Participation:

- `Comment` total participation because every comment must be written by one user
- `User` partial participation

How to draw:

- `Comment` side double line
- `User` side single line

### H. Task GENERATES Activity_Log

- Relationship: `GENERATES`
- Cardinality: `Task (1) -> (M) Activity_Log`

Participation:

- `Activity_Log` total participation because each log belongs to one task
- `Task` partial participation because a task may exist before any logged activity

How to draw:

- `Activity_Log` side double line
- `Task` side single line

### I. User PERFORMS Activity_Log

- Relationship: `PERFORMS`
- Cardinality: `User (1) -> (M) Activity_Log`

Participation:

- `Activity_Log` total participation because every log action is performed by one user
- `User` partial participation

How to draw:

- `Activity_Log` side double line
- `User` side single line

## 3. Attribute Classification For Viva

### Key attributes

- `User.id`
- `Project.id`
- `Project_Member.id`
- `Task.id`
- `Comment.id`
- `Activity_Log.id`

### Foreign key attributes

- `Project.created_by`
- `Project_Member.project_id`
- `Project_Member.user_id`
- `Task.project_id`
- `Task.assigned_to`
- `Task.created_by`
- `Comment.task_id`
- `Comment.user_id`
- `Activity_Log.task_id`
- `Activity_Log.user_id`

### Simple attributes

- `name`
- `email`
- `password`
- `role`
- `description`
- `title`
- `status`
- `priority`
- `deadline`
- `content`
- `action`
- timestamps

### Multivalued attributes

None in the current project.

If your examiner asks, say:

> This design does not use multivalued attributes because repeated values are normalized into separate rows or relationship tables, for example project membership.

### Composite attributes

None explicitly stored in the schema.

Possible conceptual example:

- `name` could be broken into `first_name` and `last_name`, but the current implementation stores it as a single simple attribute.

### Derived attributes

Not stored physically in the database, but valid in conceptual ER discussion:

- `User.project_count`
- `User.assigned_task_count`
- `Project.member_count`
- `Project.task_count`
- `Task.is_overdue`
- `Task.task_age`
- `Comment.comment_length`

In Chen notation:

- derived attributes are drawn with dashed ovals

## 4. What To Draw With Double Lines

In a handwritten detailed ER diagram, use double lines for total participation on these sides:

- `Project` in `CREATES` with `User`
- `Project_Member` to `User`
- `Project_Member` to `Project`
- `Task` in `Project HAS Task`
- `Task` in `User CREATES Task`
- `Comment` in `Task HAS Comment`
- `Comment` in `User WRITES Comment`
- `Activity_Log` in `Task GENERATES Activity_Log`
- `Activity_Log` in `User PERFORMS Activity_Log`

Do not use double line for `Task ASSIGNED_TO User`, because assignment is optional in this project.

## 5. Correct Detailed ER Layout To Use

Use these entities and relation labels:

- `USER`
- `PROJECT`
- `PROJECT_MEMBER`
- `TASK`
- `COMMENT`
- `ACTIVITY_LOG`

Use these relationship names:

- `CREATES`
- `JOINS`
- `HAS`
- `ASSIGNED_TO`
- `WRITES`
- `GENERATES`
- `PERFORMS`

Do not use:

- `LOGGED IN` between `TASK` and `ACTIVITY_LOG`

That relation is conceptually wrong for this project.

## 6. Viva-Ready Summary

You can explain it like this:

> The system has six main entities: User, Project, Project_Member, Task, Comment, and Activity_Log. User and Project have a many-to-many association resolved through Project_Member. Each project is created by one user, each task belongs to one project and is created by one user, and a task may optionally be assigned to a user. Comments and activity logs both have total participation because they cannot exist without their parent task and user reference. Derived values such as member count, task count, and overdue status are computed in logic rather than stored in the schema.
