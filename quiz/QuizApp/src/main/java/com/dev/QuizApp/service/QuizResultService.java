package com.dev.QuizApp.service;

import com.dev.QuizApp.entity.QuizResult;
import com.dev.QuizApp.repository.QuizResultRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizResultService {

    @Autowired
    private QuizResultRepo repo;

    public QuizResult saveResult(QuizResult result){
        return repo.save(result);
    }

    public List<QuizResult> getUserResults(String username){
        return repo.findByUsername(username);
    }
}
