package com.example.minijira.swing.model;

// Represents one system user such as Admin, Developer, or Viewer.
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    // Role decides which dashboard actions are allowed.
    private Role role;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        // Used in combo boxes and user selections inside the UI.
        return name + " (" + role + ")";
    }
}
