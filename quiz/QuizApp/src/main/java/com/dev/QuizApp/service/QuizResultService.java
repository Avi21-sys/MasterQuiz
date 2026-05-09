package com.dev.QuizApp.service;

import com.dev.QuizApp.entity.QuizResult;
import com.dev.QuizApp.repository.QuizResultRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class QuizResultService {

    @Autowired
    private QuizResultRepo repo;

    public QuizResult saveResult(QuizResult result){
        result.setQuizType(normalizeCategory(result.getQuizType()));

        if (result.getScore() == null) {
            result.setScore(0);
        }

        if (result.getTotalQuestions() == null) {
            result.setTotalQuestions(0);
        }

        if (result.getCompletionTimeSeconds() == null) {
            result.setCompletionTimeSeconds(0L);
        }

        return repo.save(result);
    }

    public List<QuizResult> getUserResults(String username){
        return repo.findByUsername(username);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "JAVA";
        }

        String normalized = category.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "JAVASCRIPT", "JAVA SCRIPT" -> "JS";
            case ".NET", "NET", "C#", "DOT NET" -> "DOTNET";
            default -> normalized;
        };
    }
}
