package com.dev.QuizApp.repository;

import com.dev.QuizApp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepo extends JpaRepository<Question, Long> {
    @Query(value = "SELECT * FROM question WHERE category = :category ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Question> findRandomByCategory(String category);
}
