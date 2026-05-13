package com.dev.QuizApp.security;

import com.dev.QuizApp.entity.UserProfile;
import com.dev.QuizApp.service.UserProfileService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserProfileService userProfileService;

    public CustomUserDetailsService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserProfile userProfile = userProfileService.findByUsername(username);

        if (userProfile == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return User.builder()
                .username(userProfile.getUsername())
                .password(userProfile.getPassword()) // Use stored hashed password
                .authorities(Collections.emptyList())
                .build();
    }
}
