package com.example.minijira.swing.model;

// Stores summary counts shown in the dashboard cards.
public class DashboardStats {
    private int totalTasks;
    private int todoTasks;
    private int inProgressTasks;
    private int doneTasks;

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getTodoTasks() {
        return todoTasks;
    }

    public void setTodoTasks(int todoTasks) {
        this.todoTasks = todoTasks;
    }

    public int getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(int inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public int getDoneTasks() {
        return doneTasks;
    }

    public void setDoneTasks(int doneTasks) {
        this.doneTasks = doneTasks;
    }
}
