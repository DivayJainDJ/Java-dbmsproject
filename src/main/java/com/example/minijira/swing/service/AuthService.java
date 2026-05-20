package com.example.minijira.swing.service;

import com.example.minijira.swing.model.User;
import com.example.minijira.swing.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;

public class AuthService {
    private final UserRepository userRepository = new UserRepository();

    public User login(String email, String password) throws SQLException {
        userRepository.ensureDefaultUsers();
        User user = userRepository.findByEmail(email.trim())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid password.");
        }
        return user;
    }

    public User register(User user) throws SQLException {
        userRepository.ensureDefaultUsers();
        if (userRepository.findByEmail(user.getEmail().trim()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        return userRepository.save(user);
    }

    public List<User> loadAllUsers() throws SQLException {
        userRepository.ensureDefaultUsers();
        return userRepository.findAll();
    }
}
