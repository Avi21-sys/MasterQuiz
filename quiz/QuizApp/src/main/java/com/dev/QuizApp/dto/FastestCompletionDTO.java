package com.dev.QuizApp.dto;

import java.util.List;

public class FastestCompletionDTO {
    private String category;
    private List<LeaderboardEntryDTO> fastestCompletion;
    private String generatedAt;

    public FastestCompletionDTO() {}

    public FastestCompletionDTO(String category, List<LeaderboardEntryDTO> fastestCompletion, String generatedAt) {
        this.category = category;
        this.fastestCompletion = fastestCompletion;
        this.generatedAt = generatedAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<LeaderboardEntryDTO> getFastestCompletion() {
        return fastestCompletion;
    }

    public void setFastestCompletion(List<LeaderboardEntryDTO> fastestCompletion) {
        this.fastestCompletion = fastestCompletion;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}

