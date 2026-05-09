package com.dev.QuizApp.dto;

import java.util.List;

public class WeeklyLeaderboardDTO {
    private String week;
    private String category;
    private List<LeaderboardEntryDTO> weeklyTopScores;
    private String generatedAt;

    public WeeklyLeaderboardDTO() {}

    public WeeklyLeaderboardDTO(String week, String category, List<LeaderboardEntryDTO> weeklyTopScores, String generatedAt) {
        this.week = week;
        this.category = category;
        this.weeklyTopScores = weeklyTopScores;
        this.generatedAt = generatedAt;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<LeaderboardEntryDTO> getWeeklyTopScores() {
        return weeklyTopScores;
    }

    public void setWeeklyTopScores(List<LeaderboardEntryDTO> weeklyTopScores) {
        this.weeklyTopScores = weeklyTopScores;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}

