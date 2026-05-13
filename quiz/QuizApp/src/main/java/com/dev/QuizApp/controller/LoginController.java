package com.dev.QuizApp.controller;

import com.dev.QuizApp.dto.LoginRequest;
import com.dev.QuizApp.dto.QuestionDTO;
import com.dev.QuizApp.security.JwtUtil;
import com.dev.QuizApp.service.QuestionService;
import com.dev.QuizApp.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    QuestionService questionService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserProfileService userProfileService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = jwtUtil.generateToken(loginRequest.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", loginRequest.getUsername());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid Username or Password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest loginRequest){
        try {
            userProfileService.createUser(loginRequest.getUsername(), loginRequest.getPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful. Please login with your credentials.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

//    @GetMapping("/questions")
//    public List<QuestionDTO> getQuestions(){
//        return questionService.getAllQuestions();
//    }

    @GetMapping("/questions/{category}")
    public List<QuestionDTO> getByCategory(@PathVariable String category){
        return questionService.getQuestionsByCategory(category.toUpperCase());
    }
}
