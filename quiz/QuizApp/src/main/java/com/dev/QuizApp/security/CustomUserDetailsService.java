package com.dev.QuizApp.security;

import com.dev.QuizApp.entity.UserProfile;
import com.dev.QuizApp.service.UserProfileService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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

        // Map Role enum → Spring Security GrantedAuthority ("ROLE_USER" / "ROLE_ADMIN")
        String authority = "ROLE_" + userProfile.getRole().name();

        return User.builder()
                .username(userProfile.getUsername())
                .password(userProfile.getPassword()) // Use stored hashed password
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
