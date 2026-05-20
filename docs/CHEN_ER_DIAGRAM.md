# Chen ER Diagram Specification

Use this as the exact blueprint if you want to redraw the ER diagram with:

- double rectangles not needed because there are no weak entities here
- double lines for total participation
- dashed ovals for derived attributes
- diamonds for relationships

```mermaid
erDiagram
    USER ||--o{ PROJECT : creates
    USER ||--o{ PROJECT_MEMBER : joins
    PROJECT ||--o{ PROJECT_MEMBER : has
    PROJECT ||--o{ TASK : has
    USER ||--o{ TASK : creates
    USER o|--o{ TASK : assigned_to
    TASK ||--o{ COMMENT : has
    USER ||--o{ COMMENT : writes
    TASK ||--o{ ACTIVITY_LOG : generates
    USER ||--o{ ACTIVITY_LOG : performs

    USER {
        bigint id PK
        varchar name
        varchar email UK
        varchar password
        enum role
        timestamp created_at
    }

    PROJECT {
        bigint id PK
        varchar name
        varchar description
        bigint created_by FK
        timestamp created_at
    }

    PROJECT_MEMBER {
        bigint id PK
        bigint project_id FK
        bigint user_id FK
        timestamp added_at
    }

    TASK {
        bigint id PK
        varchar title
        text description
        enum status
        enum priority
        datetime deadline
        bigint project_id FK
        bigint assigned_to FK
        bigint created_by FK
        timestamp created_at
        timestamp updated_at
    }

    COMMENT {
        bigint id PK
        bigint task_id FK
        bigint user_id FK
        varchar content
        timestamp created_at
    }

    ACTIVITY_LOG {
        bigint id PK
        bigint task_id FK
        bigint user_id FK
        varchar action
        timestamp created_at
    }
```

## Important Note

Mermaid cannot draw full Chen notation details like:

- double participation lines
- dashed ovals for derived attributes
- oval attribute nodes

So for submission, viva sheet, or notebook drawing, use [DETAILED_ER_MODEL.md](/Users/bhavjain/Downloads/mini-jira-tracker/docs/DETAILED_ER_MODEL.md) as the authoritative detailed version.
