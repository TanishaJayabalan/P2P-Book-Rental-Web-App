package com.pesu.bookrental.config;

import com.pesu.bookrental.domain.enums.Role;
import com.pesu.bookrental.domain.model.User;
import com.pesu.bookrental.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedStaffUsers(UserRepository userRepository) {
        return args -> {
            ensureUser(userRepository, "Moderator", "moderator@bookrental.local", "mod123", Role.MODERATOR, "Campus");
            ensureUser(userRepository, "Administrator", "admin@bookrental.local", "admin123", Role.ADMINISTRATOR, "Campus");
        };
    }

    private void ensureUser(UserRepository userRepository,
                            String name,
                            String email,
                            String password,
                            Role role,
                            String location) {
        userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);
            user.setLocation(location);
            return userRepository.save(user);
        });
    }
}
