package com.example.minijira.swing.ui;

import com.example.minijira.swing.model.ActivityLogEntry;
import com.example.minijira.swing.model.Comment;
import com.example.minijira.swing.model.DashboardStats;
import com.example.minijira.swing.model.Project;
import com.example.minijira.swing.model.Role;
import com.example.minijira.swing.model.Task;
import com.example.minijira.swing.model.TaskPriority;
import com.example.minijira.swing.model.TaskStatus;
import com.example.minijira.swing.model.User;
import com.example.minijira.swing.service.AuthService;
import com.example.minijira.swing.service.ProjectService;
import com.example.minijira.swing.service.TaskService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.Border;

public class DashboardFrame extends JFrame {
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final User currentUser;
    private final AuthService authService = new AuthService();
    private final ProjectService projectService = new ProjectService();
    private final TaskService taskService = new TaskService();

    private final DefaultListModel<Project> projectListModel = new DefaultListModel<>();
    private final JList<Project> projectList = new JList<>(projectListModel);
    private final DefaultTableModel taskTableModel = new DefaultTableModel(
        new Object[]{"Id", "Title", "Status", "Priority", "Assignee", "Deadline"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable taskTable = new JTable(taskTableModel);
    private final JTextArea taskDetailsArea = new JTextArea();
    private final JTextArea commentsArea = new JTextArea();
    private final JTextArea activityArea = new JTextArea();
    private final JTextArea roleNotesArea = new JTextArea();
    private final JTextField projectNameField = new JTextField();
    private final JTextField projectDescriptionField = new JTextField();
    private final JTextField commentField = new JTextField();
    private final JTextField taskSearchField = new JTextField(18);
    private final JComboBox<String> statusFilterBox = new JComboBox<>(new String[]{"ALL", "TODO", "IN_PROGRESS", "DONE"});
    private final JComboBox<String> priorityFilterBox = new JComboBox<>(new String[]{"ALL", "LOW", "MEDIUM", "HIGH"});
    private final JComboBox<String> assigneeFilterBox = new JComboBox<>(new String[]{"ALL"});
    private final JTextArea membersArea = new JTextArea();
    private final JTextArea userDirectoryArea = new JTextArea();
    private final JLabel workspaceRoleLabel = new JLabel("", SwingConstants.LEFT);
    private final JLabel projectActionsLabel = new JLabel("", SwingConstants.LEFT);
    private final JLabel taskActionsLabel = new JLabel("", SwingConstants.LEFT);
    private final JLabel roleBadgeLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel workspaceBannerLabel = new JLabel("", SwingConstants.LEFT);

    private JButton createProjectButton;
    private JButton addMemberButton;
    private JButton addTaskButton;
    private JButton editTaskButton;
    private JButton deleteTaskButton;
    private JButton moveStatusButton;
    private JButton commentButton;
    private JButton refreshButton;
    private JButton resetFiltersButton;
    private JButton myTasksButton;
    private JButton exportTaskButton;

    private final JLabel totalTasksLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel todoTasksLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel inProgressTasksLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel doneTasksLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel highPriorityLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel mediumPriorityLabel = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lowPriorityLabel = new JLabel("0", SwingConstants.CENTER);

    private List<Task> currentTasks = new ArrayList<>();
    private List<User> currentProjectMembers = new ArrayList<>();
    private Color roleAccentColor = new Color(52, 73, 94);
    private JPanel rootPanel;
    private JPanel infoPanel;
    private JPanel statsPanel;
    private JPanel taskPanel;
    private JTabbedPane detailsTabs;

    public DashboardFrame(User currentUser) {
        this.currentUser = currentUser;

        setTitle("Mini Jira Tracker - Swing Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 860);
        setMinimumSize(new Dimension(1220, 760));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainBody(), BorderLayout.CENTER);

        taskDetailsArea.setEditable(false);
        commentsArea.setEditable(false);
        activityArea.setEditable(false);
        membersArea.setEditable(false);
        roleNotesArea.setEditable(false);
        userDirectoryArea.setEditable(false);
        taskDetailsArea.setLineWrap(true);
        commentsArea.setLineWrap(true);
        activityArea.setLineWrap(true);
        membersArea.setLineWrap(true);
        roleNotesArea.setLineWrap(true);
        userDirectoryArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);
        commentsArea.setWrapStyleWord(true);
        activityArea.setWrapStyleWord(true);
        membersArea.setWrapStyleWord(true);
        roleNotesArea.setWrapStyleWord(true);
        userDirectoryArea.setWrapStyleWord(true);
        roleNotesArea.setBackground(UIManager.getColor("Panel.background"));
        userDirectoryArea.setBackground(UIManager.getColor("Panel.background"));

        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                onProjectSelectionChanged();
            }
        });

        taskTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                onTaskSelectionChanged();
            }
        });

        loadProjects();
        configureRoleView();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        panel.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        roleBadgeLabel.setOpaque(true);
        roleBadgeLabel.setForeground(Color.WHITE);
        roleBadgeLabel.setPreferredSize(new Dimension(130, 34));
        roleBadgeLabel.setFont(roleBadgeLabel.getFont().deriveFont(Font.BOLD, 13f));

        JLabel title = new JLabel("Mini Jira Tracker");
        title.setFont(title.getFont().deriveFont(24f));
        leftPanel.add(roleBadgeLabel);
        leftPanel.add(title);
        panel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridLayout(0, 1));
        rightPanel.setOpaque(false);
        rightPanel.add(new JLabel(currentUser.getName() + " | " + currentUser.getRole(), SwingConstants.RIGHT));
        workspaceRoleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(workspaceRoleLabel);
        refreshButton = new JButton("Refresh All");
        refreshButton.addActionListener(event -> loadProjects());
        rightPanel.add(refreshButton);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JSplitPane buildMainBody() {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.23);
        splitPane.setDividerLocation(300);
        splitPane.setLeftComponent(buildProjectSidebar());
        splitPane.setRightComponent(buildWorkspacePanel());
        return splitPane;
    }

    private JPanel buildProjectSidebar() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 6));
        panel.setOpaque(false);

        infoPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Session"));
        infoPanel.add(new JLabel("Name: " + currentUser.getName()));
        infoPanel.add(new JLabel("Email: " + currentUser.getEmail()));
        infoPanel.add(new JLabel("Role: " + currentUser.getRole()));
        infoPanel.add(projectActionsLabel);
        infoPanel.add(taskActionsLabel);
        panel.add(infoPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Projects"));
        centerPanel.setOpaque(false);

        if (currentUser.getRole() == Role.ADMIN) {
            JPanel createPanel = new JPanel(new GridLayout(0, 1, 6, 6));
            createPanel.setOpaque(false);
            createPanel.add(new JLabel("Project Name"));
            createPanel.add(projectNameField);
            createPanel.add(new JLabel("Description"));
            createPanel.add(projectDescriptionField);

            createProjectButton = new JButton("Create Project");
            createProjectButton.addActionListener(event -> createProject());

            addMemberButton = new JButton("Add Member by Email");
            addMemberButton.addActionListener(event -> addProjectMember());

            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.add(createProjectButton);
            buttonPanel.add(addMemberButton);
            createPanel.add(buttonPanel);
            centerPanel.add(createPanel, BorderLayout.NORTH);
        }

        centerPanel.add(new JScrollPane(projectList), BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Role Guidance"));
        notesPanel.setOpaque(false);
        notesPanel.add(new JScrollPane(roleNotesArea), BorderLayout.CENTER);
        notesPanel.setPreferredSize(new Dimension(260, 140));
        panel.add(notesPanel, BorderLayout.SOUTH);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(event -> logout());
        JPanel southPanel = new JPanel(new BorderLayout(0, 8));
        southPanel.setOpaque(false);
        southPanel.add(notesPanel, BorderLayout.CENTER);
        southPanel.add(logoutButton, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildWorkspacePanel() {
        rootPanel = new JPanel(new BorderLayout(10, 10));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 12, 12));
        rootPanel.setOpaque(true);

        JPanel northPanel = new JPanel(new BorderLayout(0, 10));
        northPanel.setOpaque(false);
        northPanel.add(buildWorkspaceBanner(), BorderLayout.NORTH);
        northPanel.add(buildStatsPanel(), BorderLayout.CENTER);

        rootPanel.add(northPanel, BorderLayout.NORTH);
        rootPanel.add(buildTaskAndDetailPanel(), BorderLayout.CENTER);
        return rootPanel;
    }

    private JPanel buildWorkspaceBanner() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(roleAccentColor, 2, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        panel.setBackground(Color.WHITE);
        workspaceBannerLabel.setFont(workspaceBannerLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(workspaceBannerLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 7, 8, 8));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Dashboard"));
        statsPanel.add(statCard("Total", totalTasksLabel));
        statsPanel.add(statCard("TODO", todoTasksLabel));
        statsPanel.add(statCard("In Progress", inProgressTasksLabel));
        statsPanel.add(statCard("Done", doneTasksLabel));
        statsPanel.add(statCard("High", highPriorityLabel));
        statsPanel.add(statCard("Medium", mediumPriorityLabel));
        statsPanel.add(statCard("Low", lowPriorityLabel));
        return statsPanel;
    }

    private JPanel statCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createTitledBorder(title));
        valueLabel.setForeground(roleAccentColor);
        valueLabel.setFont(valueLabel.getFont().deriveFont(20f));
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JSplitPane buildTaskAndDetailPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        splitPane.setTopComponent(buildTaskPanel());
        splitPane.setBottomComponent(buildDetailsPanel());
        return splitPane;
    }

    private JPanel buildTaskPanel() {
        taskPanel = new JPanel(new BorderLayout(8, 8));
        taskPanel.setBorder(BorderFactory.createTitledBorder("Tasks"));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(buildTaskToolbar());
        topPanel.add(buildFilterBar());
        taskPanel.add(topPanel, BorderLayout.NORTH);

        taskTable.setRowHeight(24);
        taskPanel.add(new JScrollPane(taskTable), BorderLayout.CENTER);
        return taskPanel;
    }

    private JPanel buildTaskToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.DEVELOPER) {
            addTaskButton = new JButton("Add Task");
            addTaskButton.addActionListener(event -> openTaskDialog(null));

            editTaskButton = new JButton("Edit Task");
            editTaskButton.addActionListener(event -> openTaskDialog(getSelectedTask()));

            deleteTaskButton = new JButton("Delete Task");
            deleteTaskButton.addActionListener(event -> deleteSelectedTask());

            moveStatusButton = new JButton("Move Status");
            moveStatusButton.addActionListener(event -> moveSelectedTaskStatus());

            toolbar.add(addTaskButton);
            toolbar.add(editTaskButton);
            toolbar.add(deleteTaskButton);
            toolbar.add(moveStatusButton);
        }

        if (currentUser.getRole() == Role.DEVELOPER) {
            myTasksButton = new JButton("My Tasks");
            myTasksButton.addActionListener(event -> filterMyTasks());
            toolbar.add(myTasksButton);
        }

        exportTaskButton = new JButton(currentUser.getRole() == Role.VIEWER ? "Export Review" : "Export Task");
        exportTaskButton.addActionListener(event -> exportSelectedTask());
        toolbar.add(exportTaskButton);
        return toolbar;
    }

    private JPanel buildFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.add(new JLabel("Status"));
        panel.add(statusFilterBox);
        panel.add(new JLabel("Priority"));
        panel.add(priorityFilterBox);
        panel.add(new JLabel("Assignee"));
        panel.add(assigneeFilterBox);
        panel.add(new JLabel("Search"));
        panel.add(taskSearchField);

        JButton applyButton = new JButton("Apply Filters");
        applyButton.addActionListener(event -> loadTasksForSelectedProject());
        panel.add(applyButton);

        resetFiltersButton = new JButton("Reset");
        resetFiltersButton.addActionListener(event -> resetFilters());
        panel.add(resetFiltersButton);
        return panel;
    }

    private JTabbedPane buildDetailsPanel() {
        detailsTabs = new JTabbedPane();
        detailsTabs.setBorder(BorderFactory.createTitledBorder("Task Details"));

        detailsTabs.addTab("Selected Task", new JScrollPane(taskDetailsArea));

        JPanel commentsPanel = new JPanel(new BorderLayout(8, 8));
        commentsPanel.add(new JScrollPane(commentsArea), BorderLayout.CENTER);
        if (currentUser.getRole() != Role.VIEWER) {
            JPanel commentEntryPanel = new JPanel(new BorderLayout(8, 0));
            commentEntryPanel.add(commentField, BorderLayout.CENTER);
            commentButton = new JButton("Add Comment");
            commentButton.addActionListener(event -> addComment());
            commentEntryPanel.add(commentButton, BorderLayout.EAST);
            commentsPanel.add(commentEntryPanel, BorderLayout.SOUTH);
        }
        detailsTabs.addTab("Comments", commentsPanel);

        detailsTabs.addTab("Activity Log", new JScrollPane(activityArea));
        detailsTabs.addTab("Project Members", new JScrollPane(membersArea));
        if (currentUser.getRole() == Role.ADMIN) {
            detailsTabs.addTab("User Directory", new JScrollPane(userDirectoryArea));
        }
        return detailsTabs;
    }

    private void loadProjects() {
        try {
            if (currentUser.getRole() == Role.ADMIN) {
                userDirectoryArea.setText(formatUsers(authService.loadAllUsers()));
            }
            List<Project> projects = projectService.loadProjects(currentUser);
            projectListModel.clear();
            for (Project project : projects) {
                projectListModel.addElement(project);
            }
            if (!projectListModel.isEmpty()) {
                projectList.setSelectedIndex(0);
            } else {
                clearWorkspace();
            }
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void createProject() {
        try {
            if (projectNameField.getText().isBlank()) {
                throw new IllegalArgumentException("Project name is required.");
            }
            projectService.createProject(projectNameField.getText(), projectDescriptionField.getText(), currentUser);
            projectNameField.setText("");
            projectDescriptionField.setText("");
            loadProjects();
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void addProjectMember() {
        Project project = projectList.getSelectedValue();
        if (project == null) {
            showError("Select a project first.");
            return;
        }
        try {
            String email = JOptionPane.showInputDialog(this, "Enter member email:");
            if (email == null || email.isBlank()) {
                return;
            }
            User member = authService.loadAllUsers().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No user found with this email."));
            projectService.addMember(project, member, currentUser);
            loadProjects();
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void onProjectSelectionChanged() {
        Project project = projectList.getSelectedValue();
        if (project == null) {
            clearWorkspace();
            return;
        }
        try {
            currentProjectMembers = projectService.loadMembers(project.getId());
            membersArea.setText(formatMembers(currentProjectMembers));
            loadTasksForSelectedProject();
            loadDashboard(project.getId());
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void loadTasksForSelectedProject() {
        Project project = projectList.getSelectedValue();
        if (project == null) {
            return;
        }
        try {
            TaskStatus status = "ALL".equals(statusFilterBox.getSelectedItem()) ? null : TaskStatus.valueOf((String) statusFilterBox.getSelectedItem());
            TaskPriority priority = "ALL".equals(priorityFilterBox.getSelectedItem()) ? null : TaskPriority.valueOf((String) priorityFilterBox.getSelectedItem());
            Long assigneeId = resolveAssigneeFilter();
            currentTasks = taskService.loadTasks(project.getId(), status, priority, assigneeId);
            applyTaskSearchFilter();
            refreshTaskTable();
            refreshAssigneeFilter();
            onTaskSelectionChanged();
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void resetFilters() {
        statusFilterBox.setSelectedItem("ALL");
        priorityFilterBox.setSelectedItem("ALL");
        taskSearchField.setText("");
        if (assigneeFilterBox.getItemCount() > 0) {
            assigneeFilterBox.setSelectedIndex(0);
        }
        loadTasksForSelectedProject();
    }

    private void filterMyTasks() {
        for (int index = 0; index < assigneeFilterBox.getItemCount(); index++) {
            String item = String.valueOf(assigneeFilterBox.getItemAt(index));
            if (item.startsWith(currentUser.getId() + " | ")) {
                assigneeFilterBox.setSelectedIndex(index);
                break;
            }
        }
        loadTasksForSelectedProject();
    }

    private void loadDashboard(Long projectId) throws SQLException {
        DashboardStats stats = taskService.loadStats(projectId);
        totalTasksLabel.setText(String.valueOf(stats.getTotalTasks()));
        todoTasksLabel.setText(String.valueOf(stats.getTodoTasks()));
        inProgressTasksLabel.setText(String.valueOf(stats.getInProgressTasks()));
        doneTasksLabel.setText(String.valueOf(stats.getDoneTasks()));
        highPriorityLabel.setText(String.valueOf(stats.getHighPriorityTasks()));
        mediumPriorityLabel.setText(String.valueOf(stats.getMediumPriorityTasks()));
        lowPriorityLabel.setText(String.valueOf(stats.getLowPriorityTasks()));
    }

    private void refreshTaskTable() {
        taskTableModel.setRowCount(0);
        for (Task task : currentTasks) {
            taskTableModel.addRow(new Object[]{
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignedToName() == null ? "Unassigned" : task.getAssignedToName(),
                task.getDeadline() == null ? "-" : task.getDeadline().format(INPUT_FORMAT)
            });
        }
        if (!currentTasks.isEmpty()) {
            taskTable.setRowSelectionInterval(0, 0);
        }
    }

    private void applyTaskSearchFilter() {
        String search = taskSearchField.getText().trim().toLowerCase();
        if (search.isBlank()) {
            return;
        }
        currentTasks = currentTasks.stream()
            .filter(task -> {
                String text = (task.getTitle() + " " + task.getDescription() + " " + (task.getAssignedToName() == null ? "" : task.getAssignedToName())).toLowerCase();
                return text.contains(search);
            })
            .toList();
    }

    private void refreshAssigneeFilter() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("ALL");
        for (User user : currentProjectMembers) {
            model.addElement(user.getId() + " | " + user.getName());
        }
        assigneeFilterBox.setModel(model);
    }

    private void onTaskSelectionChanged() {
        Task task = getSelectedTask();
        if (task == null) {
            taskDetailsArea.setText("Select a task.");
            commentsArea.setText("");
            activityArea.setText("");
            return;
        }
        taskDetailsArea.setText(buildTaskDetails(task));
        loadCommentsAndActivity(task.getId());
    }

    private String buildTaskDetails(Task task) {
        return "Title: " + task.getTitle() + "\n"
            + "Description: " + task.getDescription() + "\n"
            + "Status: " + task.getStatus() + "\n"
            + "Priority: " + task.getPriority() + "\n"
            + "Assignee: " + (task.getAssignedToName() == null ? "Unassigned" : task.getAssignedToName()) + "\n"
            + "Deadline: " + (task.getDeadline() == null ? "-" : task.getDeadline().format(INPUT_FORMAT)) + "\n"
            + "Created By: " + task.getCreatedByName();
    }

    private void loadCommentsAndActivity(Long taskId) {
        try {
            commentsArea.setText(formatComments(taskService.loadComments(taskId)));
            activityArea.setText(formatActivity(taskService.loadActivity(taskId)));
        } catch (SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void addComment() {
        Task task = getSelectedTask();
        if (task == null) {
            showError("Select a task first.");
            return;
        }
        try {
            if (commentField.getText().isBlank()) {
                throw new IllegalArgumentException("Comment is required.");
            }
            taskService.addComment(task.getId(), currentUser, commentField.getText());
            commentField.setText("");
            loadCommentsAndActivity(task.getId());
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void openTaskDialog(Task existingTask) {
        Project project = projectList.getSelectedValue();
        if (project == null) {
            showError("Select a project first.");
            return;
        }

        JTextField titleField = new JTextField(existingTask == null ? "" : existingTask.getTitle());
        JTextField descriptionField = new JTextField(existingTask == null ? "" : existingTask.getDescription());
        JComboBox<TaskPriority> priorityBox = new JComboBox<>(TaskPriority.values());
        JComboBox<TaskStatus> statusBox = new JComboBox<>(TaskStatus.values());
        JTextField deadlineField = new JTextField(existingTask == null || existingTask.getDeadline() == null ? "" : existingTask.getDeadline().format(INPUT_FORMAT));
        JComboBox<User> assigneeBox = new JComboBox<>(currentProjectMembers.toArray(new User[0]));

        if (existingTask != null) {
            priorityBox.setSelectedItem(existingTask.getPriority());
            statusBox.setSelectedItem(existingTask.getStatus());
            if (existingTask.getAssignedToId() != null) {
                for (User member : currentProjectMembers) {
                    if (member.getId().equals(existingTask.getAssignedToId())) {
                        assigneeBox.setSelectedItem(member);
                        break;
                    }
                }
            }
        } else if (!currentProjectMembers.isEmpty()) {
            assigneeBox.setSelectedIndex(0);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Title"));
        form.add(titleField);
        form.add(new JLabel("Description"));
        form.add(descriptionField);
        form.add(new JLabel("Priority"));
        form.add(priorityBox);
        form.add(new JLabel("Status"));
        form.add(statusBox);
        form.add(new JLabel("Deadline (yyyy-MM-dd HH:mm)"));
        form.add(deadlineField);
        form.add(new JLabel("Assignee"));
        form.add(assigneeBox);

        int action = JOptionPane.showConfirmDialog(this, form, existingTask == null ? "Create Task" : "Edit Task", JOptionPane.OK_CANCEL_OPTION);
        if (action != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Task task = existingTask == null ? new Task() : existingTask;
            User assignee = (User) assigneeBox.getSelectedItem();
            LocalDateTime deadline = deadlineField.getText().isBlank() ? null : LocalDateTime.parse(deadlineField.getText().trim(), INPUT_FORMAT);
            task.setProjectId(project.getId());
            task.setTitle(titleField.getText().trim());
            task.setDescription(descriptionField.getText().trim());
            task.setPriority((TaskPriority) priorityBox.getSelectedItem());
            task.setStatus((TaskStatus) statusBox.getSelectedItem());
            task.setDeadline(deadline);
            if (assignee != null) {
                task.setAssignedToId(assignee.getId());
                task.setAssignedToName(assignee.getName());
            }

            if (existingTask == null) {
                taskService.createTask(task, currentUser);
            } else {
                taskService.updateTask(task, currentUser);
            }
            loadTasksForSelectedProject();
            loadDashboard(project.getId());
        } catch (IllegalArgumentException | DateTimeParseException | SQLException exception) {
            showError("Could not save task. Check inputs.\n" + exception.getMessage());
        }
    }

    private void deleteSelectedTask() {
        Task task = getSelectedTask();
        if (task == null) {
            showError("Select a task first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete task: " + task.getTitle() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            taskService.deleteTask(task.getId(), currentUser);
            loadTasksForSelectedProject();
            loadDashboard(projectList.getSelectedValue().getId());
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private void exportSelectedTask() {
        Task task = getSelectedTask();
        if (task == null) {
            showError("Select a task first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save task summary");
        chooser.setSelectedFile(new java.io.File(task.getTitle().replaceAll("[^a-zA-Z0-9-_]", "_") + ".txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int option = chooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path filePath = chooser.getSelectedFile().toPath();
        try {
            Files.writeString(filePath, buildTaskDetails(task));
            JOptionPane.showMessageDialog(this, "Task exported to:\n" + filePath);
        } catch (IOException exception) {
            showError("Could not export task.\n" + exception.getMessage());
        }
    }

    private void moveSelectedTaskStatus() {
        Task task = getSelectedTask();
        if (task == null) {
            showError("Select a task first.");
            return;
        }

        TaskStatus nextStatus;
        if (task.getStatus() == TaskStatus.TODO) {
            nextStatus = TaskStatus.IN_PROGRESS;
        } else if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            nextStatus = TaskStatus.DONE;
        } else {
            showError("Task is already DONE.");
            return;
        }

        try {
            taskService.changeStatus(task, nextStatus, currentUser);
            loadTasksForSelectedProject();
            loadDashboard(projectList.getSelectedValue().getId());
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    private Task getSelectedTask() {
        int row = taskTable.getSelectedRow();
        if (row < 0 || row >= currentTasks.size()) {
            return null;
        }
        return currentTasks.get(row);
    }

    private Long resolveAssigneeFilter() {
        Object selected = assigneeFilterBox.getSelectedItem();
        if (selected == null || "ALL".equals(selected)) {
            return null;
        }
        String value = String.valueOf(selected);
        return Long.parseLong(value.substring(0, value.indexOf('|')).trim());
    }

    private void clearWorkspace() {
        taskTableModel.setRowCount(0);
        taskDetailsArea.setText("No project selected.");
        commentsArea.setText("");
        activityArea.setText("");
        membersArea.setText("");
        totalTasksLabel.setText("0");
        todoTasksLabel.setText("0");
        inProgressTasksLabel.setText("0");
        doneTasksLabel.setText("0");
        highPriorityLabel.setText("0");
        mediumPriorityLabel.setText("0");
        lowPriorityLabel.setText("0");
    }

    private String formatComments(List<Comment> comments) {
        StringBuilder builder = new StringBuilder();
        for (Comment comment : comments) {
            builder.append(comment.getUserName())
                .append(" | ")
                .append(comment.getCreatedAt().format(INPUT_FORMAT))
                .append('\n')
                .append(comment.getContent())
                .append("\n\n");
        }
        return builder.length() == 0 ? "No comments yet." : builder.toString();
    }

    private String formatActivity(List<ActivityLogEntry> entries) {
        StringBuilder builder = new StringBuilder();
        for (ActivityLogEntry entry : entries) {
            builder.append(entry.getCreatedAt().format(INPUT_FORMAT))
                .append(" | ")
                .append(entry.getUserName())
                .append(" | ")
                .append(entry.getAction())
                .append('\n');
        }
        return builder.length() == 0 ? "No activity logged yet." : builder.toString();
    }

    private String formatMembers(List<User> members) {
        StringBuilder builder = new StringBuilder();
        for (User member : members) {
            builder.append(member.getName())
                .append(" | ")
                .append(member.getEmail())
                .append(" | ")
                .append(member.getRole())
                .append('\n');
        }
        return builder.length() == 0 ? "No members found." : builder.toString();
    }

    private void logout() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    private void configureRoleView() {
        if (currentUser.getRole() == Role.ADMIN) {
            setTitle("Mini Jira Tracker - Admin Control Dashboard");
            roleAccentColor = new Color(192, 57, 43);
            roleBadgeLabel.setText("ADMIN CONTROL");
            workspaceBannerLabel.setText("Admin control center: manage projects, members, users, and all task operations");
            workspaceRoleLabel.setText("Admin dashboard: project control, members, users, and full task authority");
            projectActionsLabel.setText("Projects: create + add members");
            taskActionsLabel.setText("Tasks: full create/edit/delete/status/comment access");
            roleNotesArea.setText(
                "ADMIN VIEW\n\n"
                    + "- Can create projects\n"
                    + "- Can add members to projects\n"
                    + "- Can create, edit, delete, and move tasks\n"
                    + "- Can comment and inspect full activity\n"
                    + "- Can view all users in User Directory tab"
            );
            applyRoleTheme();
            return;
        }

        if (currentUser.getRole() == Role.DEVELOPER) {
            setTitle("Mini Jira Tracker - Developer Workbench");
            roleAccentColor = new Color(41, 128, 185);
            roleBadgeLabel.setText("DEVELOPER MODE");
            workspaceBannerLabel.setText("Developer workbench: focus on assigned work, task progress, and updates");
            workspaceRoleLabel.setText("Developer dashboard: task execution, comments, status updates");
            projectActionsLabel.setText("Projects: view only");
            taskActionsLabel.setText("Tasks: create/edit/delete/status/comment access");
            roleNotesArea.setText(
                "DEVELOPER VIEW\n\n"
                    + "- Cannot create projects\n"
                    + "- Cannot add members\n"
                    + "- Can create and update tasks inside visible projects\n"
                    + "- Can move tasks through TODO -> IN_PROGRESS -> DONE\n"
                    + "- Can add progress comments"
            );
            createProjectButton.setEnabled(false);
            addMemberButton.setEnabled(false);
            applyRoleTheme();
            return;
        }

        setTitle("Mini Jira Tracker - Viewer Review Console");
        roleAccentColor = new Color(39, 174, 96);
        roleBadgeLabel.setText("VIEWER CONSOLE");
        workspaceBannerLabel.setText("Viewer review console: read-only project, task, member, and activity monitoring");
        workspaceRoleLabel.setText("Viewer dashboard: read-only access for monitoring and viva demo");
        projectActionsLabel.setText("Projects: view only");
        taskActionsLabel.setText("Tasks: read only");
        roleNotesArea.setText(
            "VIEWER VIEW\n\n"
                + "- Cannot create projects\n"
                + "- Cannot add members\n"
                + "- Cannot create, edit, delete, or move tasks\n"
                + "- Cannot add comments\n"
                + "- Can review task details, comments, activity log, and members"
        );
        createProjectButton.setEnabled(false);
        addMemberButton.setEnabled(false);
        addTaskButton.setEnabled(false);
        editTaskButton.setEnabled(false);
        deleteTaskButton.setEnabled(false);
        moveStatusButton.setEnabled(false);
        commentField.setEditable(false);
        applyRoleTheme();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Mini Jira", JOptionPane.ERROR_MESSAGE);
    }

    private String formatUsers(List<User> users) {
        StringBuilder builder = new StringBuilder();
        for (User user : users) {
            builder.append(user.getId())
                .append(" | ")
                .append(user.getName())
                .append(" | ")
                .append(user.getEmail())
                .append(" | ")
                .append(user.getRole())
                .append('\n');
        }
        return builder.length() == 0 ? "No users found." : builder.toString();
    }

    private void applyRoleTheme() {
        if (rootPanel != null) {
            rootPanel.setBackground(new Color(
                Math.min(roleAccentColor.getRed() + 210, 245),
                Math.min(roleAccentColor.getGreen() + 210, 245),
                Math.min(roleAccentColor.getBlue() + 210, 245)
            ));
        }
        roleBadgeLabel.setBackground(roleAccentColor);
        workspaceRoleLabel.setForeground(roleAccentColor);
        workspaceBannerLabel.setForeground(roleAccentColor.darker());
        projectActionsLabel.setForeground(roleAccentColor);
        taskActionsLabel.setForeground(roleAccentColor);
        totalTasksLabel.setForeground(roleAccentColor);
        todoTasksLabel.setForeground(roleAccentColor);
        inProgressTasksLabel.setForeground(roleAccentColor);
        doneTasksLabel.setForeground(roleAccentColor);
        highPriorityLabel.setForeground(roleAccentColor);
        mediumPriorityLabel.setForeground(roleAccentColor);
        lowPriorityLabel.setForeground(roleAccentColor);
        taskTable.setSelectionBackground(roleAccentColor);
        taskTable.setSelectionForeground(Color.WHITE);
        applyAccentToPanel(infoPanel);
        applyAccentToPanel(statsPanel);
        applyAccentToPanel(taskPanel);
        applyAccentToTabs();
        applyAccentToTitledArea(roleNotesArea, "Role Guidance");
        applyAccentToTitledArea(membersArea, "Project Members");
        applyAccentToTitledArea(userDirectoryArea, "User Directory");
    }

    private void applyAccentToPanel(JPanel panel) {
        if (panel != null) {
            Border border = panel.getBorder();
            if (border instanceof TitledBorder titledBorder) {
                titledBorder.setTitleColor(roleAccentColor);
            }
            panel.setBackground(Color.WHITE);
        }
    }

    private void applyAccentToTabs() {
        if (detailsTabs != null) {
            detailsTabs.setForeground(roleAccentColor.darker());
            if (detailsTabs.getBorder() instanceof TitledBorder titledBorder) {
                titledBorder.setTitleColor(roleAccentColor);
            }
        }
    }

    private void applyAccentToTitledArea(JTextArea area, String fallbackTitle) {
        if (area.getParent() instanceof javax.swing.JViewport viewport
            && viewport.getParent() instanceof JScrollPane scrollPane
            && scrollPane.getBorder() instanceof TitledBorder titledBorder) {
            titledBorder.setTitleColor(roleAccentColor);
        }
        area.setCaretColor(roleAccentColor);
        area.setSelectionColor(roleAccentColor.brighter());
    }
}
