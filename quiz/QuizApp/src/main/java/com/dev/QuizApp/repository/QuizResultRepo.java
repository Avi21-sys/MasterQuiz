package com.dev.QuizApp.repository;

import com.dev.QuizApp.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepo extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUsername(String username);
}
