package com.dev.QuizApp.service;

import com.dev.QuizApp.entity.Role;
import com.dev.QuizApp.entity.UserProfile;
import com.dev.QuizApp.repository.UserProfileRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private static final Path PROFILE_PHOTO_DIR = Path.of("uploads", "profile-photos");

    private final UserProfileRepo userProfileRepo;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserProfileRepo userProfileRepo, PasswordEncoder passwordEncoder) {
        this.userProfileRepo = userProfileRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfile findByUsername(String username) {
        return userProfileRepo.findByUsername(username).orElse(null);
    }

    /** Register a new USER (default role) */
    public UserProfile createUser(String username, String rawPassword) {
        return createUser(username, rawPassword, Role.USER);
    }

    /** Register with an explicit role (used by admin-seeding / admin panel) */
    public UserProfile createUser(String username, String rawPassword, Role role) {
        if (userProfileRepo.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        UserProfile user = new UserProfile();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userProfileRepo.save(user);
    }

    /** Promote an existing user to ADMIN */
    public UserProfile promoteToAdmin(String username) {
        UserProfile user = userProfileRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        user.setRole(Role.ADMIN);
        return userProfileRepo.save(user);
    }

    /** Demote an ADMIN back to USER */
    public UserProfile demoteToUser(String username) {
        UserProfile user = userProfileRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        user.setRole(Role.USER);
        return userProfileRepo.save(user);
    }

    /** Delete a user account (admin operation) */
    public void deleteUser(String username) {
        UserProfile user = userProfileRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        userProfileRepo.delete(user);
    }

    /** List all users (admin operation) */
    public List<UserProfile> getAllUsers() {
        return userProfileRepo.findAll();
    }

    public UserProfile saveProfilePhoto(String username, MultipartFile photo) throws IOException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("Photo is required");
        }

        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image uploads are allowed");
        }

        Files.createDirectories(PROFILE_PHOTO_DIR);

        String extension = getFileExtension(photo.getOriginalFilename());
        String filename = sanitize(username) + "-" + UUID.randomUUID() + extension;
        Path destination = PROFILE_PHOTO_DIR.resolve(filename);

        Files.copy(photo.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        UserProfile profile = userProfileRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Please register or log in before uploading a photo"));
        profile.setPhotoUrl("/uploads/profile-photos/" + filename);

        return userProfileRepo.save(profile);
    }

    public UserProfile getOrCreateProfile(String username) {
        return userProfileRepo.findByUsername(username).orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setUsername(username);
            return profile;
        });
    }

    public Map<String, UserProfile> getProfilesByUsername(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptyMap();
        }

        return userProfileRepo.findByUsernameIn(usernames)
                .stream()
                .collect(Collectors.toMap(UserProfile::getUsername, Function.identity()));
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null) return ".jpg";
        int dotIndex = originalFilename.lastIndexOf('.');
        return dotIndex < 0 ? ".jpg" : originalFilename.substring(dotIndex).toLowerCase();
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
