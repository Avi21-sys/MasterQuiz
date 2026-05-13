package com.dev.QuizApp.config;

import com.dev.QuizApp.entity.UserProfile;
import com.dev.QuizApp.repository.UserProfileRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDefaultUser(UserProfileRepo userProfileRepo, PasswordEncoder passwordEncoder) {
        return args -> userProfileRepo.findByUsername("user").map(user -> {
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode("password"));
                return userProfileRepo.save(user);
            }

            return user;
        }).orElseGet(() -> {
                UserProfile user = new UserProfile();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("password"));
                return userProfileRepo.save(user);
        });
    }
}
