package com.dev.QuizApp.config;

import com.dev.QuizApp.entity.Role;
import com.dev.QuizApp.entity.UserProfile;
import com.dev.QuizApp.repository.UserProfileRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDefaultUsers(UserProfileRepo userProfileRepo,
                                              PasswordEncoder passwordEncoder) {
        return args -> {

            // ── Seed default USER ────────────────────────────────────────────
            userProfileRepo.findByUsername("user").map(u -> {
                if (u.getPassword() == null || u.getPassword().isBlank()) {
                    u.setPassword(passwordEncoder.encode("password"));
                }
                if (u.getRole() == null) {
                    u.setRole(Role.USER);
                }
                return userProfileRepo.save(u);
            }).orElseGet(() -> {
                UserProfile u = new UserProfile();
                u.setUsername("user");
                u.setPassword(passwordEncoder.encode("password"));
                u.setRole(Role.USER);
                return userProfileRepo.save(u);
            });

            // ── Seed default ADMIN ───────────────────────────────────────────
            userProfileRepo.findByUsername("admin").orElseGet(() -> {
                UserProfile admin = new UserProfile();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                return userProfileRepo.save(admin);
            });
        };
    }
}
