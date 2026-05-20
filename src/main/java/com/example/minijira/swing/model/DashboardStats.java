package com.example.minijira.swing.model;

public class DashboardStats {
    private int totalTasks;
    private int todoTasks;
    private int inProgressTasks;
    private int doneTasks;
    private int highPriorityTasks;
    private int mediumPriorityTasks;
    private int lowPriorityTasks;

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

    public int getHighPriorityTasks() {
        return highPriorityTasks;
    }

    public void setHighPriorityTasks(int highPriorityTasks) {
        this.highPriorityTasks = highPriorityTasks;
    }

    public int getMediumPriorityTasks() {
        return mediumPriorityTasks;
    }

    public void setMediumPriorityTasks(int mediumPriorityTasks) {
        this.mediumPriorityTasks = mediumPriorityTasks;
    }

    public int getLowPriorityTasks() {
        return lowPriorityTasks;
    }

    public void setLowPriorityTasks(int lowPriorityTasks) {
        this.lowPriorityTasks = lowPriorityTasks;
    }
}
