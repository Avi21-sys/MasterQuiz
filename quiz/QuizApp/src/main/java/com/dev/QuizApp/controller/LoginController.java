package com.dev.QuizApp.controller;

import com.dev.QuizApp.dto.LoginRequest;
import com.dev.QuizApp.dto.QuestionDTO;
import com.dev.QuizApp.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    QuestionService questionService;

    private final String USERNAME = "user";
    private final String PASSWORD = "password";

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest){
        if(USERNAME.equals(loginRequest.getUsername()) && PASSWORD.equals(loginRequest.getPassword())){
            return "Login Successful";
        } else{
            return "Invalid Username or Password";
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
