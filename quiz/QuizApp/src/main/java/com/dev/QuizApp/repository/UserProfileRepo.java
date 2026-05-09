package com.dev.QuizApp.repository;

import com.dev.QuizApp.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepo extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);

    List<UserProfile> findByUsernameIn(Collection<String> usernames);
}
