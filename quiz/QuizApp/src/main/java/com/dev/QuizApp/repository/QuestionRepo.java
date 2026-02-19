package com.dev.QuizApp.repository;

import com.dev.QuizApp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepo extends JpaRepository<Question, Long> {
}
