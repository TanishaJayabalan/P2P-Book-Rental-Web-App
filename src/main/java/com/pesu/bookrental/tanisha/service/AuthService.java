package com.pesu.bookrental.tanisha.service;

import com.pesu.bookrental.tanisha.dto.LoginForm;
import com.pesu.bookrental.tanisha.dto.ProfileUpdateForm;
import com.pesu.bookrental.tanisha.dto.RegisterForm;
import com.pesu.bookrental.domain.enums.Role;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.repository.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(RegisterForm form) {
        String normalizedEmail = form.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(form.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(form.getPassword());
        user.setLocation(form.getLocation());
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(LoginForm form) {
        return userRepository.findByEmail(form.getEmail().trim().toLowerCase())
                .filter(user -> user.getPassword().equals(form.getPassword()));
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateProfile(Long userId, ProfileUpdateForm form) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String normalizedEmail = form.getEmail().trim().toLowerCase();
        userRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Another account already uses this email.");
                });

        user.setName(form.getName().trim());
        user.setEmail(normalizedEmail);
        user.setLocation(form.getLocation());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(form.getPassword());
        }
        return userRepository.save(user);
    }
}
