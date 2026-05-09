package com.dev.QuizApp.service;

import com.dev.QuizApp.dto.*;
import com.dev.QuizApp.entity.QuizResult;
import com.dev.QuizApp.entity.UserProfile;
import com.dev.QuizApp.repository.QuizResultRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private QuizResultRepo quizResultRepo;

    @Autowired
    private UserProfileService userProfileService;

    private static final int LEADERBOARD_SIZE = 10;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get top scores for a specific category
     */
    public TopScoresResponseDTO getTopScores(String category, int limit) {
        category = normalizeCategory(category);
        List<QuizResult> results = quizResultRepo.findTopScoresByCategory(categoryAliases(category)).stream()
                .limit(Math.min(limit, LEADERBOARD_SIZE))
                .collect(Collectors.toList());

        List<LeaderboardEntryDTO> entries = convertToLeaderboardEntries(results);

        return new TopScoresResponseDTO(
                category,
                entries,
                LocalDateTime.now().format(formatter)
        );
    }

    /**
     * Get top scores for a specific category (default limit of 10)
     */
    public TopScoresResponseDTO getTopScores(String category) {
        return getTopScores(category, LEADERBOARD_SIZE);
    }

    /**
     * Get the highest accuracy leaderboard for a specific category
     */
    public AccuracyLeaderboardDTO getHighestAccuracy(String category, int limit) {
        category = normalizeCategory(category);
        List<QuizResult> results = quizResultRepo.findHighestAccuracyByCategory(categoryAliases(category)).stream()
                .limit(Math.min(limit, LEADERBOARD_SIZE))
                .collect(Collectors.toList());

        List<LeaderboardEntryDTO> entries = convertToLeaderboardEntries(results);

        return new AccuracyLeaderboardDTO(
                category,
                entries,
                LocalDateTime.now().format(formatter)
        );
    }

    /**
     * Get highest accuracy leaderboard (default limit of 10)
     */
    public AccuracyLeaderboardDTO getHighestAccuracy(String category) {
        return getHighestAccuracy(category, LEADERBOARD_SIZE);
    }

    /**
     * Get the fastest completion time leaderboard for a specific category
     */
    public FastestCompletionDTO getFastestCompletion(String category, int limit) {
        category = normalizeCategory(category);
        List<QuizResult> results = quizResultRepo.findFastestCompletionByCategory(categoryAliases(category)).stream()
                .limit(Math.min(limit, LEADERBOARD_SIZE))
                .collect(Collectors.toList());

        List<LeaderboardEntryDTO> entries = convertToLeaderboardEntries(results);

        return new FastestCompletionDTO(
                category,
                entries,
                LocalDateTime.now().format(formatter)
        );
    }

    /**
     * Get fastest completion time leaderboard (default limit of 10)
     */
    public FastestCompletionDTO getFastestCompletion(String category) {
        return getFastestCompletion(category, LEADERBOARD_SIZE);
    }

    /**
     * Get weekly leaderboard for a specific category
     */
    public WeeklyLeaderboardDTO getWeeklyLeaderboard(String category, int limit) {
        category = normalizeCategory(category);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<QuizResult> results = quizResultRepo.findWeeklyTopScores(categoryAliases(category), sevenDaysAgo).stream()
                .limit(Math.min(limit, LEADERBOARD_SIZE))
                .collect(Collectors.toList());

        List<LeaderboardEntryDTO> entries = convertToLeaderboardEntries(results);

        return new WeeklyLeaderboardDTO(
                "Last 7 Days",
                category,
                entries,
                LocalDateTime.now().format(formatter)
        );
    }

    /**
     * Get weekly leaderboard (default limit of 10)
     */
    public WeeklyLeaderboardDTO getWeeklyLeaderboard(String category) {
        return getWeeklyLeaderboard(category, LEADERBOARD_SIZE);
    }

    /**
     * Get overall leaderboard across all categories
     */
    public List<TopScoresResponseDTO> getOverallLeaderboard(int limit) {
        List<String> categories = quizResultRepo.findAllCategories();

        return categories.stream()
                .map(category -> getTopScores(category, limit))
                .collect(Collectors.toList());
    }

    /**
     * Get user rank in a specific category
     */
    public Integer getUserRankByCategory(String username, String category) {
        category = normalizeCategory(category);
        List<QuizResult> allResults = quizResultRepo.findTopScoresByCategory(categoryAliases(category));

        for (int i = 0; i < allResults.size(); i++) {
            if (allResults.get(i).getUsername().equals(username)) {
                return i + 1;
            }
        }

        return -1; // User not found in rankings
    }

    /**
     * Get user's personal statistics for a category
     */
    public Map<String, Object> getUserStats(String username, String category) {
        category = normalizeCategory(category);
        List<QuizResult> userResults = quizResultRepo.findByUsernameAndQuizType(username, categoryAliases(category));

        if (userResults.isEmpty()) {
            return Map.of(
                    "username", username,
                    "category", category,
                    "totalAttempts", 0,
                    "message", "No results found for this user"
            );
        }

        double avgScore = userResults.stream()
                .mapToInt(QuizResult::getScore)
                .average()
                .orElse(0.0);

        double avgAccuracy = userResults.stream()
                .mapToDouble(qr -> qr.getAccuracy())
                .average()
                .orElse(0.0);

        long avgTime = Math.round(userResults.stream()
                .mapToLong(qr -> qr.getCompletionTimeSeconds() != null ? qr.getCompletionTimeSeconds() : 0)
                .average()
                .orElse(0.0));

        QuizResult bestResult = userResults.stream()
                .max(Comparator.comparingInt(QuizResult::getScore))
                .orElse(null);

        Integer userRank = getUserRankByCategory(username, category);

        Map<String, Object> stats = new HashMap<>();
        stats.put("username", username);
        stats.put("category", category);
        stats.put("totalAttempts", userResults.size());
        stats.put("bestScore", bestResult != null ? bestResult.getScore() : 0);
        stats.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
        stats.put("averageAccuracy", Math.round(avgAccuracy * 100.0) / 100.0);
        stats.put("averageCompletionTime", avgTime + " seconds");
        stats.put("userRank", userRank > 0 ? userRank : "Not ranked");
        stats.put("totalQuestions", bestResult != null ? bestResult.getTotalQuestions() : 0);

        return stats;
    }

    /**
     * Convert QuizResult entities to LeaderboardEntryDTO with rankings
     */
    private List<LeaderboardEntryDTO> convertToLeaderboardEntries(List<QuizResult> results) {
        List<LeaderboardEntryDTO> entries = new ArrayList<>();
        Map<String, UserProfile> profilesByUsername = getProfilesForResults(results);
        int rank = 1;

        for (QuizResult result : results) {
            UserProfile profile = profilesByUsername.get(result.getUsername());
            String photoUrl = profile != null ? profile.getPhotoUrl() : null;

            LeaderboardEntryDTO entry = new LeaderboardEntryDTO(
                    rank,
                    result.getUsername(),
                    result.getScore(),
                    result.getTotalQuestions(),
                    Math.round(result.getAccuracy() * 100.0) / 100.0,
                    result.getCompletionTimeSeconds(),
                    result.getQuizType(),
                    result.getAttemptDate().format(formatter),
                    photoUrl
            );
            entries.add(entry);
            rank++;
        }

        return entries;
    }

    private Map<String, UserProfile> getProfilesForResults(List<QuizResult> results) {
        try {
            return userProfileService.getProfilesByUsername(
                    results.stream()
                            .map(QuizResult::getUsername)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet())
            );
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    /**
     * Format completion time in human-readable format
     */
    public static String formatCompletionTime(Long seconds) {
        if (seconds == null || seconds == 0) {
            return "Not recorded";
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d h %d m %d s", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d m %d s", minutes, secs);
        } else {
            return String.format("%d s", secs);
        }
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

    private List<String> categoryAliases(String category) {
        return switch (normalizeCategory(category)) {
            case "JS" -> List.of("JS", "JAVASCRIPT", "JAVA SCRIPT");
            case "DOTNET" -> List.of("DOTNET", ".NET", "NET", "DOT NET", "C#");
            default -> List.of(normalizeCategory(category));
        };
    }
}
