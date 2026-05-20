# Mini Jira Tracker

College-style Developer Project & Task Tracker built with:

- Java Swing for frontend
- Java + JDBC for backend logic
- MySQL for database

This version follows the common college requirement:

- no browser frontend
- no REST API dependency for demo
- JDBC connection using `Class.forName("com.mysql.cj.jdbc.Driver")`
- MySQL accessed using `DriverManager.getConnection(...)`

## Tech Stack

- Java 17
- Swing
- JDBC
- MySQL Connector/J
- MySQL

## Project Structure

```text
mini-jira-tracker/
├── pom.xml
├── README.md
├── docs/
├── src/
│   └── main/
│       ├── java/com/example/minijira/swing/
│       │   ├── MiniJiraSwingApplication.java
│       │   ├── config/
│       │   ├── db/
│       │   ├── model/
│       │   ├── repository/
│       │   ├── service/
│       │   └── ui/
│       └── resources/
│           ├── schema.sql
│           └── data.sql
```

## Modules Included

- User registration and login
- Role-based access: `ADMIN`, `DEVELOPER`, `VIEWER`
- Project creation
- Add project member by email
- Task creation, update, delete
- Task assignment
- Task workflow: `TODO -> IN_PROGRESS -> DONE`
- Comments on tasks
- Activity log for task actions
- Dashboard counts for task status and priority

## Database Setup

Run these files in MySQL:

1. `src/main/resources/schema.sql`
2. `src/main/resources/data.sql`

Default database used by the app:

```java
jdbc:mysql://localhost:3306/mini_jira_db
```

Default JDBC config is in:

- [AppConfig.java](/Users/bhavjain/Downloads/mini-jira-tracker/src/main/java/com/example/minijira/swing/config/AppConfig.java)

Current values:

```java
DB_URL = "jdbc:mysql://localhost:3306/mini_jira_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
DB_USER = "appuser"
DB_PASSWORD = "1234"
DB_DRIVER = "com.mysql.cj.jdbc.Driver"
```

Update these if your MySQL username/password is different.

## JDBC Requirement

This project uses the exact JDBC style usually expected in college:

```java
Class.forName("com.mysql.cj.jdbc.Driver");
Connection connection = DriverManager.getConnection(url, user, password);
```

Code location:

- [DatabaseConnection.java](/Users/bhavjain/Downloads/mini-jira-tracker/src/main/java/com/example/minijira/swing/db/DatabaseConnection.java)

## Demo Credentials

- `admin@minijira.com / Password@123`
- `dev@minijira.com / Password@123`
- `viewer@minijira.com / Password@123`

## How To Run

### Option 1: Maven

```bash
cd /Users/bhavjain/Downloads/mini-jira-tracker
mvn compile
mvn exec:java
```

### Option 2: IDE with external JDBC jar

If your college specifically wants external jar classpath setup:

1. Download MySQL Connector/J jar
2. Add jar in your IDE project libraries / classpath
3. Run:

```text
com.example.minijira.swing.MiniJiraSwingApplication
```

## Important Swing Files

- [MiniJiraSwingApplication.java](/Users/bhavjain/Downloads/mini-jira-tracker/src/main/java/com/example/minijira/swing/MiniJiraSwingApplication.java)
- [LoginFrame.java](/Users/bhavjain/Downloads/mini-jira-tracker/src/main/java/com/example/minijira/swing/ui/LoginFrame.java)
- [DashboardFrame.java](/Users/bhavjain/Downloads/mini-jira-tracker/src/main/java/com/example/minijira/swing/ui/DashboardFrame.java)

## Main Viva Points

- Swing is used for desktop frontend.
- JDBC is used for database communication.
- MySQL is the only database.
- Clean layers are maintained as:

```text
UI -> Service -> Repository -> MySQL
```

- `VIEWER` can only read data.
- `ADMIN` and `DEVELOPER` can manage projects/tasks.
- Task comments and activity logs are stored in separate tables.

## Notes

- The older Spring/web version is not the run path for the college submission.
- The exam/demo version is the Swing + JDBC version only.
# Java-dbmsproject
