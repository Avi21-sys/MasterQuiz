package com.dev.QuizApp.repository;

import com.dev.QuizApp.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizResultRepo extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUsername(String username);

    // Top scores by category - ordered by score descending
    @Query("SELECT qr FROM QuizResult qr WHERE UPPER(qr.quizType) IN :categories ORDER BY qr.score DESC, qr.attemptDate DESC")
    List<QuizResult> findTopScoresByCategory(@Param("categories") List<String> categories);

    // Highest accuracy by category - considering only those with at least 50% score
    @Query("SELECT qr FROM QuizResult qr WHERE UPPER(qr.quizType) IN :categories AND qr.score >= (qr.totalQuestions / 2) ORDER BY (CAST(qr.score AS DOUBLE) / qr.totalQuestions) DESC, qr.attemptDate DESC")
    List<QuizResult> findHighestAccuracyByCategory(@Param("categories") List<String> categories);

    // Fastest completion by category
    @Query("SELECT qr FROM QuizResult qr WHERE UPPER(qr.quizType) IN :categories AND qr.completionTimeSeconds IS NOT NULL ORDER BY qr.completionTimeSeconds ASC, qr.attemptDate DESC")
    List<QuizResult> findFastestCompletionByCategory(@Param("categories") List<String> categories);

    // Weekly leaderboard - last 7 days
    @Query("SELECT qr FROM QuizResult qr WHERE UPPER(qr.quizType) IN :categories AND qr.attemptDate >= :startDate ORDER BY qr.score DESC, qr.attemptDate DESC")
    List<QuizResult> findWeeklyTopScores(@Param("categories") List<String> categories, @Param("startDate") LocalDateTime startDate);

    // Get all unique categories
    @Query("SELECT DISTINCT qr.quizType FROM QuizResult qr")
    List<String> findAllCategories();

    // Get results for a specific user and category
    @Query("SELECT qr FROM QuizResult qr WHERE qr.username = :username AND UPPER(qr.quizType) IN :categories")
    List<QuizResult> findByUsernameAndQuizType(@Param("username") String username, @Param("categories") List<String> categories);

    // Get best score for a user in a category
    @Query("SELECT qr FROM QuizResult qr WHERE qr.username = :username AND UPPER(qr.quizType) IN :categories ORDER BY qr.score DESC LIMIT 1")
    QuizResult findBestScoreForUserInCategory(@Param("username") String username, @Param("categories") List<String> categories);
}
