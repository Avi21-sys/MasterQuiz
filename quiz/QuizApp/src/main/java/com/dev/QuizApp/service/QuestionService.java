package com.dev.QuizApp.service;

import com.dev.QuizApp.dto.QuestionDTO;
import com.dev.QuizApp.entity.Question;
import com.dev.QuizApp.repository.QuestionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepo questionRepo;
    
//    public List<QuestionDTO> getAllQuestions(){
//        return questionRepo.findAll().stream().map(q -> {
//            QuestionDTO dto = new QuestionDTO();
//
//            dto.setId(q.getQuestionId());
//            dto.setQuestionText(q.getQuestionText());
//            dto.setCorrectAnswer(q.getCorrectAnswer());
//
//            List<String> optionList = q.getOptions()
//                    .stream()
//                    .map(o-> o.getOptionText())
//                    .toList();
//
//            dto.setOptions(optionList);
//
//            return dto;
//        }).toList();

    public List<QuestionDTO> getQuestionsByCategory(String category){

        return questionRepo.findRandomByCategory(category)
                .stream()
                .map(q -> {

                    QuestionDTO dto = new QuestionDTO();

                    dto.setId(q.getQuestionId());
                    dto.setQuestionText(q.getQuestionText());
                    dto.setCorrectAnswer(q.getCorrectAnswer());

                    List<String> optionList = q.getOptions()
                            .stream()
                            .map(o -> o.getOptionText())
                            .toList();

                    dto.setOptions(optionList);

                    return dto;

                })
                .toList();
    }
}
