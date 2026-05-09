package com.dev.QuizApp.service;

import com.dev.QuizApp.entity.UserProfile;
import com.dev.QuizApp.repository.UserProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private static final Path PROFILE_PHOTO_DIR = Path.of("uploads", "profile-photos");

    @Autowired
    private UserProfileRepo userProfileRepo;

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
                .orElseGet(UserProfile::new);
        profile.setUsername(username);
        profile.setPhotoUrl("/uploads/profile-photos/" + filename);

        return userProfileRepo.save(profile);
    }

    public UserProfile getOrCreateProfile(String username) {
        return userProfileRepo.findByUsername(username).orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setUsername(username);
            return userProfileRepo.save(profile);
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
        if (originalFilename == null) {
            return ".jpg";
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0) {
            return ".jpg";
        }

        return originalFilename.substring(dotIndex).toLowerCase();
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
