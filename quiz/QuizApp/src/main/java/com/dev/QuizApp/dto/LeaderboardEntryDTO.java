package com.dev.QuizApp.dto;

public class LeaderboardEntryDTO {
    private Integer rank;
    private String username;
    private Integer score;
    private Integer totalQuestions;
    private Double accuracy;
    private Long completionTimeSeconds;
    private String quizType;
    private String attemptDate;
    private String photoUrl;

    public LeaderboardEntryDTO() {}

    public LeaderboardEntryDTO(Integer rank, String username, Integer score, Integer totalQuestions, Double accuracy, Long completionTimeSeconds, String quizType, String attemptDate) {
        this.rank = rank;
        this.username = username;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.accuracy = accuracy;
        this.completionTimeSeconds = completionTimeSeconds;
        this.quizType = quizType;
        this.attemptDate = attemptDate;
    }

    public LeaderboardEntryDTO(Integer rank, String username, Integer score, Integer totalQuestions, Double accuracy, Long completionTimeSeconds, String quizType, String attemptDate, String photoUrl) {
        this(rank, username, score, totalQuestions, accuracy, completionTimeSeconds, quizType, attemptDate);
        this.photoUrl = photoUrl;
    }

    // Getters and Setters
    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Long getCompletionTimeSeconds() {
        return completionTimeSeconds;
    }

    public void setCompletionTimeSeconds(Long completionTimeSeconds) {
        this.completionTimeSeconds = completionTimeSeconds;
    }

    public String getQuizType() {
        return quizType;
    }

    public void setQuizType(String quizType) {
        this.quizType = quizType;
    }

    public String getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(String attemptDate) {
        this.attemptDate = attemptDate;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

