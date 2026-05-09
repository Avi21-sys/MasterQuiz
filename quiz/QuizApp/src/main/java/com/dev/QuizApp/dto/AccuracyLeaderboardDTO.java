package com.dev.QuizApp.dto;

import java.util.List;

public class AccuracyLeaderboardDTO {
    private String category;
    private List<LeaderboardEntryDTO> highestAccuracy;
    private String generatedAt;

    public AccuracyLeaderboardDTO() {}

    public AccuracyLeaderboardDTO(String category, List<LeaderboardEntryDTO> highestAccuracy, String generatedAt) {
        this.category = category;
        this.highestAccuracy = highestAccuracy;
        this.generatedAt = generatedAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<LeaderboardEntryDTO> getHighestAccuracy() {
        return highestAccuracy;
    }

    public void setHighestAccuracy(List<LeaderboardEntryDTO> highestAccuracy) {
        this.highestAccuracy = highestAccuracy;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}

