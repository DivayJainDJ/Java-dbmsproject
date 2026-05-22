package com.example.minijira.swing.ui;

import com.example.minijira.swing.model.*;
import com.example.minijira.swing.service.AuthService;
import com.example.minijira.swing.service.ProjectService;
import com.example.minijira.swing.service.TaskService;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DashboardFrame extends JFrame {

    // Logged-in user decides both permissions and dashboard appearance.
    private final User currentUser;
    private final AuthService authService = new AuthService();
    private final ProjectService projectService = new ProjectService();
    private final TaskService taskService = new TaskService();

    // Common format used when showing task deadlines and timestamps.
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Left-side project list and center task table are the main data views.
    private DefaultListModel<Project> projectListModel = new DefaultListModel<>();
    private JList<Project> projectList = new JList<>(projectListModel);
    private DefaultTableModel taskTableModel = new DefaultTableModel(
        new Object[]{"Id", "Title", "Status", "Priority", "Assignee", "Deadline"}, 0
    );
    private JTable taskTable = new JTable(taskTableModel);

    // These text areas are used inside the lower tabs.
    private JTextArea taskArea = new JTextArea();
    private JTextArea commentsArea = new JTextArea();
    private JTextArea activityArea = new JTextArea();
    private JTextArea membersArea = new JTextArea();
    private JTextArea extraArea = new JTextArea();

    // Input controls used for project creation, task search, and comments.
    private JTextField projectNameField = new JTextField();
    private JTextField projectDescriptionField = new JTextField();
    private JTextField taskSearchField = new JTextField();
    private JTextField commentField = new JTextField();

    // Simple filter controls for narrowing visible tasks.
    private JComboBox<String> statusBox = new JComboBox<>(new String[]{"ALL", "TODO", "IN_PROGRESS", "DONE"});
    private JComboBox<String> priorityBox = new JComboBox<>(new String[]{"ALL", "LOW", "MEDIUM", "HIGH"});
    private JComboBox<String> assigneeBox = new JComboBox<>(new String[]{"ALL"});

    // Summary labels are updated whenever project/task data changes.
    private JLabel roleLabel = new JLabel();
    private JLabel totalLabel = new JLabel("0", SwingConstants.CENTER);
    private JLabel todoLabel = new JLabel("0", SwingConstants.CENTER);
    private JLabel progressLabel = new JLabel("0", SwingConstants.CENTER);
    private JLabel doneLabel = new JLabel("0", SwingConstants.CENTER);

    // Action buttons are enabled or shown depending on the role.
    private JButton createProjectButton = new JButton("Create Project");
    private JButton addMemberButton = new JButton("Add Member");
    private JButton addTaskButton = new JButton("Add Task");
    private JButton editTaskButton = new JButton("Edit Task");
    private JButton deleteTaskButton = new JButton("Delete Task");
    private JButton statusTaskButton = new JButton("Move Status");
    private JButton commentButton = new JButton("Add Comment");

    // These lists keep the currently loaded project members and tasks in memory.
    private List<Task> currentTasks = new ArrayList<>();
    private List<User> currentMembers = new ArrayList<>();

    public DashboardFrame(User currentUser) {
        this.currentUser = currentUser;

        // Main window after login. Its actions change depending on the user's role.
        setTitle(getWindowTitle());
        setSize(1250, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        taskArea.setEditable(false);
        commentsArea.setEditable(false);
        activityArea.setEditable(false);
        membersArea.setEditable(false);
        extraArea.setEditable(false);

        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                projectChanged();
            }
        });

        taskTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                taskChanged();
            }
        });

        taskTableModel = new DefaultTableModel(new Object[]{"Id", "Title", "Status", "Priority", "Assignee", "Deadline"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        taskTable.setModel(taskTableModel);

        setupButtons();
        setupRoleUi();
        loadProjects();
    }

    private JPanel createHeader() {
        // Top bar shows application name on left and role banner on right.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel title = new JLabel("Mini Jira Tracker");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(title, BorderLayout.WEST);

        roleLabel.setText(getRoleBannerText());
        roleLabel.setOpaque(true);
        roleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setBackground(getRoleColor());
        roleLabel.setPreferredSize(new Dimension(250, 35));
        panel.add(roleLabel, BorderLayout.EAST);

        return panel;
    }

    private JSplitPane createMainPanel() {
        // Main screen is split into left navigation and right work area.
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.25);
        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());
        return splitPane;
    }

    private JPanel createLeftPanel() {
        // Left side contains session info, project tools, project list, and logout.
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 5));

        JPanel topPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        topPanel.setBorder(BorderFactory.createTitledBorder("Session"));
        topPanel.add(new JLabel("User: " + currentUser.getName()));
        topPanel.add(new JLabel("Email: " + currentUser.getEmail()));
        topPanel.add(new JLabel("Role: " + currentUser.getRole()));
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createTitledBorder(getProjectsPanelTitle()));

        // Only admin can create projects and add members.
        if (currentUser.getRole() == Role.ADMIN) {
            JPanel createPanel = new JPanel(new GridLayout(0, 1, 6, 6));
            createPanel.add(new JLabel("Project Name"));
            createPanel.add(projectNameField);
            createPanel.add(new JLabel("Description"));
            createPanel.add(projectDescriptionField);

            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
            buttonPanel.add(createProjectButton);
            buttonPanel.add(addMemberButton);
            createPanel.add(buttonPanel);
            centerPanel.add(createPanel, BorderLayout.NORTH);
        }

        centerPanel.add(new JScrollPane(projectList), BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        panel.add(logoutButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRightPanel() {
        // Right side contains stats, task table, and detail tabs.
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(createStatsPanel(), BorderLayout.NORTH);
        topPanel.add(createTaskPanel(), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.60);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(createTabs());

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsPanel() {
        // Small cards show quick project summary numbers.
        JPanel panel = new JPanel(new GridLayout(1, 4, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(getStatsPanelTitle()));
        panel.add(createStatCard("Total", totalLabel));
        panel.add(createStatCard("TODO", todoLabel));
        panel.add(createStatCard("In Progress", progressLabel));
        panel.add(createStatCard("Done", doneLabel));
        return panel;
    }

    private JPanel createStatCard(String title, JLabel label) {
        // Reusable card for one summary value.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        label.setForeground(getRoleColor());
        label.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTaskPanel() {
        // Task panel combines buttons, filters, and task table.
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(getTaskPanelTitle()));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(createToolbar());
        topPanel.add(createFilterPanel());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(taskTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createToolbar() {
        // Toolbar contains actions such as add/edit/delete/move/export task.
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Viewer is read-only, so task action buttons are not shown.
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.DEVELOPER) {
            panel.add(addTaskButton);
            panel.add(editTaskButton);
            panel.add(deleteTaskButton);
            panel.add(statusTaskButton);
        }

        if (currentUser.getRole() == Role.DEVELOPER) {
            JButton myTasksButton = new JButton("My Tasks");
            myTasksButton.addActionListener(e -> showMyTasks());
            panel.add(myTasksButton);
        }

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadProjects());
        panel.add(refreshButton);

        JButton exportButton = new JButton("Export Task");
        exportButton.addActionListener(e -> exportTask());
        panel.add(exportButton);

        return panel;
    }

    private JPanel createFilterPanel() {
        // Filter row helps narrow down tasks without writing new SQL manually in UI.
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Status"));
        panel.add(statusBox);
        panel.add(new JLabel("Priority"));
        panel.add(priorityBox);
        panel.add(new JLabel("Assignee"));
        panel.add(assigneeBox);
        panel.add(new JLabel("Search"));
        taskSearchField.setColumns(12);
        panel.add(taskSearchField);

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> loadTasks());
        panel.add(applyButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetFilters());
        panel.add(resetButton);

        return panel;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        // These tabs help show all task-related information in one place.
        tabs.addTab("Task Info", new JScrollPane(taskArea));
        tabs.addTab("Comments", createCommentsPanel());
        tabs.addTab("Activity", new JScrollPane(activityArea));
        tabs.addTab("Members", new JScrollPane(membersArea));

        if (currentUser.getRole() == Role.ADMIN) {
            tabs.addTab("Users", new JScrollPane(extraArea));
        } else if (currentUser.getRole() == Role.DEVELOPER) {
            tabs.addTab("Work Notes", new JScrollPane(extraArea));
        } else {
            tabs.addTab("Review Notes", new JScrollPane(extraArea));
        }
        return tabs;
    }

    private JPanel createCommentsPanel() {
        // Viewer can read comments, while Admin/Developer can also write comments.
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(commentsArea), BorderLayout.CENTER);

        if (currentUser.getRole() != Role.VIEWER) {
            JPanel bottom = new JPanel(new BorderLayout(8, 0));
            bottom.add(commentField, BorderLayout.CENTER);
            bottom.add(commentButton, BorderLayout.EAST);
            panel.add(bottom, BorderLayout.SOUTH);
        }
        return panel;
    }

    private void setupButtons() {
        // Connect every button to its matching dashboard action method.
        createProjectButton.addActionListener(e -> createProject());
        addMemberButton.addActionListener(e -> addMember());
        addTaskButton.addActionListener(e -> openTaskDialog(null));
        editTaskButton.addActionListener(e -> openTaskDialog(getSelectedTask()));
        deleteTaskButton.addActionListener(e -> deleteTask());
        statusTaskButton.addActionListener(e -> moveStatus());
        commentButton.addActionListener(e -> addComment());
    }

    private void setupRoleUi() {
        // Default text shown in the role-specific extra tab.
        extraArea.setText(getExtraTabText());
    }

    private void loadProjects() {
        try {
            // Admin gets an extra tab with the full user list.
            if (currentUser.getRole() == Role.ADMIN) {
                extraArea.setText(formatUsers(authService.loadAllUsers()));
            }

            List<Project> projects = projectService.loadProjects(currentUser);
            projectListModel.clear();
            for (Project project : projects) {
                projectListModel.addElement(project);
            }
            if (!projectListModel.isEmpty()) {
                projectList.setSelectedIndex(0);
            } else {
                clearAllText();
            }
        } catch (SQLException exception) {
            showMessage(exception.getMessage());
        }
    }

    private void projectChanged() {
        // When user selects a project, refresh members, tasks, and summary counts.
        Project project = projectList.getSelectedValue();
        if (project == null) {
            clearAllText();
            return;
        }

        try {
            currentMembers = projectService.loadMembers(project.getId());
            membersArea.setText(formatMembers(currentMembers));
            loadTasks();
            loadStats(project.getId());
        } catch (SQLException exception) {
            showMessage(exception.getMessage());
        }
    }

    private void loadTasks() {
        Project project = projectList.getSelectedValue();
        if (project == null) {
            return;
        }

        try {
            // Filters are read from the UI and passed to the service/repository layer.
            TaskStatus status = "ALL".equals(statusBox.getSelectedItem()) ? null : TaskStatus.valueOf((String) statusBox.getSelectedItem());
            TaskPriority priority = "ALL".equals(priorityBox.getSelectedItem()) ? null : TaskPriority.valueOf((String) priorityBox.getSelectedItem());
            Long userId = getSelectedAssigneeId();

            currentTasks = taskService.loadTasks(project.getId(), status, priority, userId);

            String search = taskSearchField.getText().trim().toLowerCase();
            if (!search.isBlank()) {
                List<Task> filtered = new ArrayList<>();
                for (Task task : currentTasks) {
                    String text = (task.getTitle() + " " + task.getDescription() + " " + task.getAssignedToName()).toLowerCase();
                    if (text.contains(search)) {
                        filtered.add(task);
                    }
                }
                currentTasks = filtered;
            }

            reloadTaskTable();
            reloadAssigneeBox();
            taskChanged();
        } catch (SQLException exception) {
            showMessage(exception.getMessage());
        }
    }

    private void loadStats(Long projectId) throws SQLException {
        // Read summary counts from service and place them into UI labels.
        DashboardStats stats = taskService.loadStats(projectId);
        totalLabel.setText(String.valueOf(stats.getTotalTasks()));
        todoLabel.setText(String.valueOf(stats.getTodoTasks()));
        progressLabel.setText(String.valueOf(stats.getInProgressTasks()));
        doneLabel.setText(String.valueOf(stats.getDoneTasks()));
    }

    private void reloadTaskTable() {
        // Rebuild the visible task table from the currentTasks list.
        taskTableModel.setRowCount(0);
        for (Task task : currentTasks) {
            taskTableModel.addRow(new Object[]{
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignedToName() == null ? "Unassigned" : task.getAssignedToName(),
                task.getDeadline() == null ? "-" : task.getDeadline().format(formatter)
            });
        }
        if (!currentTasks.isEmpty()) {
            taskTable.setRowSelectionInterval(0, 0);
        }
    }

    private void reloadAssigneeBox() {
        // Rebuild assignee filter options based on selected project's members.
        String oldValue = assigneeBox.getSelectedItem() == null ? "ALL" : assigneeBox.getSelectedItem().toString();
        assigneeBox.removeAllItems();
        assigneeBox.addItem("ALL");
        for (User user : currentMembers) {
            assigneeBox.addItem(user.getId() + " | " + user.getName());
        }
        assigneeBox.setSelectedItem(oldValue);
    }

    private void taskChanged() {
        // When a task is selected, show its details, comments, and activity history.
        Task task = getSelectedTask();
        if (task == null) {
            taskArea.setText("Select a task.");
            commentsArea.setText("");
            activityArea.setText("");
            return;
        }

        taskArea.setText(
            "Title: " + task.getTitle() + "\n"
                + "Description: " + task.getDescription() + "\n"
                + "Status: " + task.getStatus() + "\n"
                + "Priority: " + task.getPriority() + "\n"
                + "Assignee: " + (task.getAssignedToName() == null ? "Unassigned" : task.getAssignedToName()) + "\n"
                + "Deadline: " + (task.getDeadline() == null ? "-" : task.getDeadline().format(formatter)) + "\n"
                + "Created By: " + task.getCreatedByName()
        );

        try {
            commentsArea.setText(formatComments(taskService.loadComments(task.getId())));
            activityArea.setText(formatActivity(taskService.loadActivity(task.getId())));
        } catch (SQLException exception) {
            showMessage(exception.getMessage());
        }
    }

    private void createProject() {
        // Read values from project form and pass them to service layer.
        try {
            if (projectNameField.getText().trim().isBlank()) {
                throw new IllegalArgumentException("Project name is required.");
            }
            projectService.createProject(projectNameField.getText(), projectDescriptionField.getText(), currentUser);
            projectNameField.setText("");
            projectDescriptionField.setText("");
            loadProjects();
        } catch (Exception exception) {
            showMessage(exception.getMessage());
        }
    }

    private void addMember() {
        // Add a project member by asking for an existing user's email.
        Project project = projectList.getSelectedValue();
        if (project == null) {
            showMessage("Select a project first.");
            return;
        }

        try {
            String email = JOptionPane.showInputDialog(this, "Enter member email:");
            if (email == null || email.trim().isBlank()) {
                return;
            }
            List<User> users = authService.loadAllUsers();
            User selectedUser = null;
            for (User user : users) {
                if (user.getEmail().equalsIgnoreCase(email.trim())) {
                    selectedUser = user;
                    break;
                }
            }
            if (selectedUser == null) {
                throw new IllegalArgumentException("User not found.");
            }
            projectService.addMember(project, selectedUser, currentUser);
            projectChanged();
        } catch (Exception exception) {
            showMessage(exception.getMessage());
        }
    }

    private void openTaskDialog(Task oldTask) {
        Project project = projectList.getSelectedValue();
        if (project == null) {
            showMessage("Select a project first.");
            return;
        }

        // Same dialog is reused for both add and edit task.
        JTextField titleField = new JTextField(oldTask == null ? "" : oldTask.getTitle());
        JTextField descriptionField = new JTextField(oldTask == null ? "" : oldTask.getDescription());
        JComboBox<TaskPriority> priorityField = new JComboBox<>(TaskPriority.values());
        JComboBox<TaskStatus> statusField = new JComboBox<>(TaskStatus.values());
        JTextField deadlineField = new JTextField(oldTask == null || oldTask.getDeadline() == null ? "" : oldTask.getDeadline().format(formatter));
        JComboBox<User> assigneeField = new JComboBox<>(currentMembers.toArray(new User[0]));

        if (oldTask != null) {
            priorityField.setSelectedItem(oldTask.getPriority());
            statusField.setSelectedItem(oldTask.getStatus());
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Title"));
        form.add(titleField);
        form.add(new JLabel("Description"));
        form.add(descriptionField);
        form.add(new JLabel("Priority"));
        form.add(priorityField);
        form.add(new JLabel("Status"));
        form.add(statusField);
        form.add(new JLabel("Deadline yyyy-MM-dd HH:mm"));
        form.add(deadlineField);
        form.add(new JLabel("Assignee"));
        form.add(assigneeField);

        int result = JOptionPane.showConfirmDialog(this, form, oldTask == null ? "Add Task" : "Edit Task", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Task task = oldTask == null ? new Task() : oldTask;
            task.setProjectId(project.getId());
            task.setTitle(titleField.getText().trim());
            task.setDescription(descriptionField.getText().trim());
            task.setPriority((TaskPriority) priorityField.getSelectedItem());
            task.setStatus((TaskStatus) statusField.getSelectedItem());
            if (!deadlineField.getText().trim().isBlank()) {
                task.setDeadline(LocalDateTime.parse(deadlineField.getText().trim(), formatter));
            } else {
                task.setDeadline(null);
            }
            User user = (User) assigneeField.getSelectedItem();
            if (user != null) {
                task.setAssignedToId(user.getId());
                task.setAssignedToName(user.getName());
            }

            if (oldTask == null) {
                taskService.createTask(task, currentUser);
            } else {
                taskService.updateTask(task, currentUser);
            }

            loadTasks();
            loadStats(project.getId());
        } catch (Exception exception) {
            showMessage("Task save failed: " + exception.getMessage());
        }
    }

    private void deleteTask() {
        // Delete the selected task after confirmation dialog.
        Task task = getSelectedTask();
        if (task == null) {
            showMessage("Select a task first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected task?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            taskService.deleteTask(task.getId(), currentUser);
            loadTasks();
            loadStats(projectList.getSelectedValue().getId());
        } catch (Exception exception) {
            showMessage(exception.getMessage());
        }
    }

    private void moveStatus() {
        Task task = getSelectedTask();
        if (task == null) {
            showMessage("Select a task first.");
            return;
        }

        // Keep workflow simple: TODO -> IN_PROGRESS -> DONE
        TaskStatus nextStatus;
        if (task.getStatus() == TaskStatus.TODO) {
            nextStatus = TaskStatus.IN_PROGRESS;
        } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            nextStatus = TaskStatus.DONE;
        } else {
            showMessage("Task already completed.");
            return;
        }

        try {
            taskService.changeStatus(task, nextStatus, currentUser);
            loadTasks();
            loadStats(projectList.getSelectedValue().getId());
        } catch (Exception exception) {
            showMessage(exception.getMessage());
        }
    }

    private void addComment() {
        // Save a new comment for the currently selected task.
        Task task = getSelectedTask();
        if (task == null) {
            showMessage("Select a task first.");
            return;
        }
        try {
            if (commentField.getText().trim().isBlank()) {
                throw new IllegalArgumentException("Comment is empty.");
            }
            taskService.addComment(task.getId(), currentUser, commentField.getText());
            commentField.setText("");
            taskChanged();
        } catch (Exception exception) {
            showMessage(exception.getMessage());
        }
    }

    private void showMyTasks() {
        // Developer shortcut: automatically apply assignee filter for self.
        for (int i = 0; i < assigneeBox.getItemCount(); i++) {
            String item = String.valueOf(assigneeBox.getItemAt(i));
            if (item.startsWith(currentUser.getId() + " | ")) {
                assigneeBox.setSelectedIndex(i);
                break;
            }
        }
        loadTasks();
    }

    private void exportTask() {
        // Export the selected task's visible details into a simple text file.
        Task task = getSelectedTask();
        if (task == null) {
            showMessage("Select a task first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int option = chooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            Files.writeString(Path.of(chooser.getSelectedFile().getAbsolutePath()), taskArea.getText());
            showMessage("Task exported.");
        } catch (IOException exception) {
            showMessage("Export failed.");
        }
    }

    private void resetFilters() {
        // Bring task filters back to default "show all" state.
        statusBox.setSelectedIndex(0);
        priorityBox.setSelectedIndex(0);
        assigneeBox.setSelectedIndex(0);
        taskSearchField.setText("");
        loadTasks();
    }

    private Task getSelectedTask() {
        // Convert currently selected table row back into Task object.
        int row = taskTable.getSelectedRow();
        if (row < 0 || row >= currentTasks.size()) {
            return null;
        }
        return currentTasks.get(row);
    }

    private Long getSelectedAssigneeId() {
        // Assignee combo stores items like "2 | Dev User", so id is parsed from text.
        Object value = assigneeBox.getSelectedItem();
        if (value == null || value.toString().equals("ALL")) {
            return null;
        }
        String text = value.toString();
        return Long.parseLong(text.substring(0, text.indexOf("|")).trim());
    }

    private void clearAllText() {
        // Used when there is no selected project or no data to show.
        taskTableModel.setRowCount(0);
        taskArea.setText("");
        commentsArea.setText("");
        activityArea.setText("");
        membersArea.setText("");
        totalLabel.setText("0");
        todoLabel.setText("0");
        progressLabel.setText("0");
        doneLabel.setText("0");
    }

    private String formatComments(List<Comment> comments) {
        // Prepare comment list for textarea display.
        StringBuilder builder = new StringBuilder();
        if (comments.isEmpty()) {
            return "No comments";
        }
        for (Comment comment : comments) {
            builder.append(comment.getUserName()).append(" - ").append(comment.getCreatedAt().format(formatter)).append("\n");
            builder.append(comment.getContent()).append("\n\n");
        }
        return builder.toString();
    }

    private String formatActivity(List<ActivityLogEntry> entries) {
        // Prepare activity log list for textarea display.
        StringBuilder builder = new StringBuilder();
        if (entries.isEmpty()) {
            return "No activity";
        }
        for (ActivityLogEntry entry : entries) {
            builder.append(entry.getCreatedAt().format(formatter))
                .append(" - ")
                .append(entry.getUserName())
                .append(" - ")
                .append(entry.getAction())
                .append("\n");
        }
        return builder.toString();
    }

    private String formatMembers(List<User> members) {
        // Prepare member list for textarea display.
        StringBuilder builder = new StringBuilder();
        if (members.isEmpty()) {
            return "No members";
        }
        for (User user : members) {
            builder.append(user.getName()).append(" - ").append(user.getEmail()).append(" - ").append(user.getRole()).append("\n");
        }
        return builder.toString();
    }

    private String formatUsers(List<User> users) {
        // Admin user directory text shown in extra tab.
        StringBuilder builder = new StringBuilder();
        for (User user : users) {
            builder.append(user.getId()).append(" - ").append(user.getName()).append(" - ").append(user.getRole()).append("\n");
        }
        return builder.toString();
    }

    private void logout() {
        // Return to login screen and close the current dashboard window.
        new LoginFrame().setVisible(true);
        dispose();
    }

    private void showMessage(String message) {
        // Small helper to show popup messages in one common style.
        JOptionPane.showMessageDialog(this, message);
    }

    private String getWindowTitle() {
        // Window title changes by role so dashboards feel different.
        if (currentUser.getRole() == Role.ADMIN) {
            return "Admin Dashboard";
        }
        if (currentUser.getRole() == Role.DEVELOPER) {
            return "Developer Dashboard";
        }
        return "Viewer Dashboard";
    }

    private String getRoleBannerText() {
        // Role banner text changes by role.
        if (currentUser.getRole() == Role.ADMIN) {
            return "ADMIN CONTROL PANEL";
        }
        if (currentUser.getRole() == Role.DEVELOPER) {
            return "DEVELOPER WORKSPACE";
        }
        return "VIEWER READ ONLY";
    }

    private Color getRoleColor() {
        // Role color is reused in banner and summary cards.
        if (currentUser.getRole() == Role.ADMIN) {
            return new Color(192, 57, 43);
        }
        if (currentUser.getRole() == Role.DEVELOPER) {
            return new Color(41, 128, 185);
        }
        return new Color(39, 174, 96);
    }

    private String getProjectsPanelTitle() {
        // Left project section title changes by role.
        if (currentUser.getRole() == Role.ADMIN) {
            return "Project Management";
        }
        if (currentUser.getRole() == Role.DEVELOPER) {
            return "Assigned Projects";
        }
        return "Project Viewer";
    }

    private String getStatsPanelTitle() {
        // Summary card panel title changes by role.
        if (currentUser.getRole() == Role.ADMIN) {
            return "Project Summary";
        }
        if (currentUser.getRole() == Role.DEVELOPER) {
            return "Work Summary";
        }
        return "Read Only Summary";
    }

    private String getTaskPanelTitle() {
        // Task panel title changes by role.
        if (currentUser.getRole() == Role.ADMIN) {
            return "Task Administration";
        }
        if (currentUser.getRole() == Role.DEVELOPER) {
            return "Task Workbench";
        }
        return "Task Review";
    }

    private String getExtraTabText() {
        // Fallback text for the role-specific extra tab.
        if (currentUser.getRole() == Role.ADMIN) {
            return "Admin can review all users from this tab.";
        }
        if (currentUser.getRole() == Role.DEVELOPER) {
            return "Developer can search tasks, update status, and add comments.";
        }
        return "Viewer can only read data and monitor project progress.";
    }
}
