package com.dev.QuizApp.dto;

import java.util.List;

public class TopScoresResponseDTO {
    private String category;
    private List<LeaderboardEntryDTO> topScores;
    private String generatedAt;

    public TopScoresResponseDTO() {}

    public TopScoresResponseDTO(String category, List<LeaderboardEntryDTO> topScores, String generatedAt) {
        this.category = category;
        this.topScores = topScores;
        this.generatedAt = generatedAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<LeaderboardEntryDTO> getTopScores() {
        return topScores;
    }

    public void setTopScores(List<LeaderboardEntryDTO> topScores) {
        this.topScores = topScores;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}

