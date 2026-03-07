package com.dev.QuizApp.controller;

import com.dev.QuizApp.entity.QuizResult;
import com.dev.QuizApp.service.QuizResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "*")
public class QuizResultController {

    @Autowired
    private QuizResultService service;

    @PostMapping("/save")
    public QuizResult saveResult(@RequestBody QuizResult result){
        return service.saveResult(result);
    }

    public List<QuizResult> getResults(@PathVariable String username){
        return service.getUserResults(username);
    }
}
