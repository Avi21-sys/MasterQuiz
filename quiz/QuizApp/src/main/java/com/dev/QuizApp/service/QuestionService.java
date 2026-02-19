package com.dev.QuizApp.service;

import com.dev.QuizApp.entity.Question;
import com.dev.QuizApp.repository.QuestionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    QuestionRepo questionRepo;
    
    public List<Question> getAllQuestions(){
        List<Question> questionRepoAll = questionRepo.findAll();
        return questionRepoAll;
    }
}
