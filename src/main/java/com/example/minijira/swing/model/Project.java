package com.example.minijira.swing.model;

// Represents one project created inside the Mini Jira system.
public class Project {
    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private String createdByName;
    // Member count is shown in the project list UI.
    private int memberCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    @Override
    public String toString() {
        // This text is shown directly in the JList for projects.
        return name + " | Owner: " + createdByName + " | Members: " + memberCount;
    }
}
