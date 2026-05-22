package com.example.minijira.swing.service;

import com.example.minijira.swing.model.*;
import com.example.minijira.swing.repository.UserRepository;
import java.sql.*;
import java.util.*;

public class AuthService {
    // AuthService uses UserRepository for all user table operations.
    private final UserRepository userRepository = new UserRepository();

    public User login(String email, String password) throws SQLException {
        // Keep default demo users available even on a fresh database.
        userRepository.ensureDefaultUsers();
        // Find user by email first.
        User user = userRepository.findByEmail(email.trim())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
        // Compare entered password with stored password.
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid password.");
        }
        return user;
    }

    public User register(User user) throws SQLException {
        userRepository.ensureDefaultUsers();
        // Remove extra spaces from email before saving.
        String email = user.getEmail().trim();
        user.setEmail(email);
        // Stop duplicate registration for the same email.
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        return userRepository.save(user);
    }

    public List<User> loadAllUsers() throws SQLException {
        // Mainly used by Admin dashboard user list.
        userRepository.ensureDefaultUsers();
        return userRepository.findAll();
    }
}
