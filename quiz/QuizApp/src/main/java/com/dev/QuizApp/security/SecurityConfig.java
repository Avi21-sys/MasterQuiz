package com.dev.QuizApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize / @RolesAllowed on methods
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var cfg = new org.springframework.web.cors.CorsConfiguration();
                    cfg.setAllowedOriginPatterns(java.util.List.of(
                            "http://127.0.0.1:5500", "http://localhost:5500"));
                    cfg.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    cfg.setAllowedHeaders(java.util.List.of("*"));
                    cfg.setAllowCredentials(true);
                    return cfg;
                }))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz

                        // ── Public endpoints ─────────────────────────────────────────────
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/login", "/api/register").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // ── Admin-only endpoints ──────────────────────────────────────────
                        // Question management
                        .requestMatchers(HttpMethod.POST,   "/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("ADMIN")

                        // User management
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── User endpoints (USER + ADMIN) ─────────────────────────────────
                        // Read questions (take quiz)
                        .requestMatchers(HttpMethod.GET, "/api/questions/**").hasAnyRole("USER", "ADMIN")

                        // Quiz results
                        .requestMatchers("/api/results/**").hasAnyRole("USER", "ADMIN")

                        // Leaderboard
                        .requestMatchers("/api/leaderboard/**").hasAnyRole("USER", "ADMIN")

                        // Profile
                        .requestMatchers("/api/profile/**").hasAnyRole("USER", "ADMIN")

                        // Anything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}