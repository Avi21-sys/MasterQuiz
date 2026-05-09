package com.dev.QuizApp.controller;

import com.dev.QuizApp.dto.*;
import com.dev.QuizApp.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    /**
     * Get top scores for a specific category
     * @param category The quiz category
     * @param limit Number of entries to return (optional, default 10)
     */
    @GetMapping("/top-scores/{category}")
    public ResponseEntity<TopScoresResponseDTO> getTopScores(
            @PathVariable String category,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaderboardService.getTopScores(category, limit));
    }

    /**
     * Get highest accuracy leaderboard for a specific category
     * @param category The quiz category
     * @param limit Number of entries to return (optional, default 10)
     */
    @GetMapping("/highest-accuracy/{category}")
    public ResponseEntity<AccuracyLeaderboardDTO> getHighestAccuracy(
            @PathVariable String category,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaderboardService.getHighestAccuracy(category, limit));
    }

    /**
     * Get fastest completion time leaderboard for a specific category
     * @param category The quiz category
     * @param limit Number of entries to return (optional, default 10)
     */
    @GetMapping("/fastest-completion/{category}")
    public ResponseEntity<FastestCompletionDTO> getFastestCompletion(
            @PathVariable String category,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaderboardService.getFastestCompletion(category, limit));
    }

    /**
     * Get weekly leaderboard for a specific category (last 7 days)
     * @param category The quiz category
     * @param limit Number of entries to return (optional, default 10)
     */
    @GetMapping("/weekly/{category}")
    public ResponseEntity<WeeklyLeaderboardDTO> getWeeklyLeaderboard(
            @PathVariable String category,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaderboardService.getWeeklyLeaderboard(category, limit));
    }

    /**
     * Get overall leaderboard across all categories
     * @param limit Number of entries per category (optional, default 10)
     */
    @GetMapping("/overall")
    public ResponseEntity<List<TopScoresResponseDTO>> getOverallLeaderboard(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaderboardService.getOverallLeaderboard(limit));
    }

    /**
     * Get user's rank in a specific category
     * @param username The username to check
     * @param category The quiz category
     */
    @GetMapping("/rank/{username}/{category}")
    public ResponseEntity<Map<String, Object>> getUserRank(
            @PathVariable String username,
            @PathVariable String category) {
        Integer rank = leaderboardService.getUserRankByCategory(username, category);
        return ResponseEntity.ok(Map.of(
                "username", username,
                "category", category,
                "rank", rank > 0 ? rank : "Not ranked",
                "message", rank > 0 ? "User found in rankings" : "User not found in top rankings"
        ));
    }

    /**
     * Get user's statistics for a specific category
     * @param username The username to check
     * @param category The quiz category
     */
    @GetMapping("/stats/{username}/{category}")
    public ResponseEntity<Map<String, Object>> getUserStats(
            @PathVariable String username,
            @PathVariable String category) {
        return ResponseEntity.ok(leaderboardService.getUserStats(username, category));
    }

    /**
     * Get all available categories for leaderboard
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        return ResponseEntity.ok(Map.of(
                "categories", List.of("JAVA", "PYTHON", "JS", "DOTNET"),
                "message", "Available quiz categories"
        ));
    }

    /**
     * Get combined leaderboard data (top scores, highest accuracy, fastest completion)
     * @param category The quiz category
     */
    @GetMapping("/combined/{category}")
    public ResponseEntity<Map<String, Object>> getCombinedLeaderboard(@PathVariable String category) {
        return ResponseEntity.ok(Map.of(
                "topScores", leaderboardService.getTopScores(category, 5),
                "highestAccuracy", leaderboardService.getHighestAccuracy(category, 5),
                "fastestCompletion", leaderboardService.getFastestCompletion(category, 5),
                "weekly", leaderboardService.getWeeklyLeaderboard(category, 5)
        ));
    }
}

