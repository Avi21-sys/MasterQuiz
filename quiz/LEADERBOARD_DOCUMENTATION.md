# Quiz App Leaderboard System Documentation

## Overview

The Leaderboard System is a comprehensive ranking and tracking feature for the Quiz App that displays:
- **Top Scores** - Players with the highest scores
- **Highest Accuracy** - Players with the best accuracy percentage
- **Fastest Completion Time** - Players who complete quizzes the quickest
- **Weekly Leaderboard** - Rankings from the last 7 days

## System Architecture

### Database Design

#### Enhanced QuizResult Entity
The `QuizResult` entity has been enhanced with the following fields:

```java
@Entity
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;           // Player username
    private String quizType;           // Category (Java, Python, etc.)
    private Integer score;             // Numeric score achieved
    private Integer totalQuestions;    // Total questions in quiz
    private Long completionTimeSeconds; // Time taken in seconds
    private LocalDateTime attemptDate;  // When the quiz was attempted
    
    // Calculated field
    public Double getAccuracy() {
        if (totalQuestions == null || totalQuestions == 0) {
            return 0.0;
        }
        return (double) (score * 100) / totalQuestions;
    }
}
```

### Key Changes from Previous Schema

| Field | Previous | Current | Notes |
|-------|----------|---------|-------|
| `score` | String | Integer | Allows numeric comparisons and calculations |
| `totalQuestions` | String | Integer | Enables accuracy calculation |
| `completionTimeSeconds` | N/A | Long | Track quiz completion speed |

### Repository Layer

The `QuizResultRepo` interface provides specialized queries for leaderboard operations:

```java
// Get top scores for a category, ordered by score descending
List<QuizResult> findTopScoresByCategory(String category);

// Get highest accuracy results
List<QuizResult> findHighestAccuracyByCategory(String category);

// Get fastest completion times
List<QuizResult> findFastestCompletionByCategory(String category);

// Get weekly rankings (last 7 days)
List<QuizResult> findWeeklyTopScores(String category, LocalDateTime startDate);

// Get all unique categories
List<String> findAllCategories();

// User-specific queries
List<QuizResult> findByUsernameAndQuizType(String username, String category);
QuizResult findBestScoreForUserInCategory(String username, String category);
```

### Service Layer

The `LeaderboardService` implements core business logic:

#### Main Methods

1. **getTopScores(category, limit)**
   - Returns top N scores for a category
   - Default limit: 10
   - Sorted by score descending

2. **getHighestAccuracy(category, limit)**
   - Returns users with best accuracy
   - Only includes users with at least 50% score
   - Sorted by accuracy descending

3. **getFastestCompletion(category, limit)**
   - Returns fastest quiz completions
   - Only includes quizzes with recorded time
   - Sorted by time ascending

4. **getWeeklyLeaderboard(category, limit)**
   - Returns top scores from last 7 days
   - Uses current timestamp minus 7 days
   - Sorted by score descending

5. **getOverallLeaderboard(limit)**
   - Returns top scores across all categories
   - Returns combination of all category leaderboards

6. **getUserStats(username, category)**
   - Returns comprehensive user statistics including:
     - Total attempts
     - Best score
     - Average score
     - Average accuracy
     - Average completion time
     - User rank

7. **getUserRankByCategory(username, category)**
   - Returns user's rank position in a category
   - Returns -1 if user not in top rankings

### Controller Layer

The `LeaderboardController` exposes REST endpoints:

#### API Endpoints

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| GET | `/api/leaderboard/top-scores/{category}` | Get top scores | category, limit (optional) |
| GET | `/api/leaderboard/highest-accuracy/{category}` | Get accuracy rankings | category, limit (optional) |
| GET | `/api/leaderboard/fastest-completion/{category}` | Get speed rankings | category, limit (optional) |
| GET | `/api/leaderboard/weekly/{category}` | Get weekly rankings | category, limit (optional) |
| GET | `/api/leaderboard/overall` | Get all categories | limit (optional) |
| GET | `/api/leaderboard/rank/{username}/{category}` | Get user rank | username, category |
| GET | `/api/leaderboard/stats/{username}/{category}` | Get user stats | username, category |
| GET | `/api/leaderboard/categories` | Get all categories | - |
| GET | `/api/leaderboard/combined/{category}` | Get all rankings combined | category |

## Data Transfer Objects (DTOs)

### LeaderboardEntryDTO
Represents a single leaderboard entry:
```java
{
    "rank": 1,
    "username": "john_doe",
    "score": 85,
    "totalQuestions": 100,
    "accuracy": 85.0,
    "completionTimeSeconds": 1200,
    "quizType": "Java",
    "attemptDate": "2026-02-21 15:30:45"
}
```

### TopScoresResponseDTO
Response containing top scores:
```java
{
    "category": "Java",
    "topScores": [ /* LeaderboardEntryDTO array */ ],
    "generatedAt": "2026-02-21 15:35:20"
}
```

### AccuracyLeaderboardDTO
Response for accuracy rankings:
```java
{
    "category": "Python",
    "highestAccuracy": [ /* LeaderboardEntryDTO array */ ],
    "generatedAt": "2026-02-21 15:35:20"
}
```

### FastestCompletionDTO
Response for speed rankings:
```java
{
    "category": "JavaScript",
    "fastestCompletion": [ /* LeaderboardEntryDTO array */ ],
    "generatedAt": "2026-02-21 15:35:20"
}
```

### WeeklyLeaderboardDTO
Response for weekly rankings:
```java
{
    "week": "Last 7 Days",
    "category": "React",
    "weeklyTopScores": [ /* LeaderboardEntryDTO array */ ],
    "generatedAt": "2026-02-21 15:35:20"
}
```

## Frontend Components

### Files Added

1. **leaderboard.html** - Main leaderboard page
2. **leaderboard.js** - JavaScript functionality
3. **leaderboard.css** - Styling

### Features

#### Tab Navigation
- Top Scores
- Highest Accuracy
- Fastest Completion
- Weekly Rankings
- Personal Statistics

#### Category Selector
- Dropdown to select quiz category
- Dynamically loads data for selected category
- Options: Java, Python, JavaScript, React, Spring Boot, DevOps, .NET, Compiler

#### Display Features
- Ranked tables with medal emojis (🥇 🥈 🥉)
- Responsive design for mobile/desktop
- Real-time data loading
- Color-coded rankings
- Timestamp display

#### Personal Statistics View
- Enter username to view personal stats
- Displays:
  - Total attempts
  - Best score
  - Average score
  - Average accuracy
  - Average completion time
  - User rank

## Usage Examples

### Backend API Usage

#### Get Top 10 Scores for Java Category
```bash
curl http://localhost:8080/api/leaderboard/top-scores/Java?limit=10
```

#### Get User Statistics
```bash
curl http://localhost:8080/api/leaderboard/stats/john_doe/Python
```

#### Get User Rank
```bash
curl http://localhost:8080/api/leaderboard/rank/john_doe/React
```

#### Get Weekly Leaderboard
```bash
curl http://localhost:8080/api/leaderboard/weekly/JavaScript
```

### Frontend Usage

1. **Navigate to Leaderboard**
   - Open `leaderboard.html` in browser

2. **View Rankings**
   - Click on desired ranking tab
   - Select category from dropdown
   - View ranked entries

3. **Check Personal Stats**
   - Click "Personal Stats" tab
   - Enter username
   - Click "Get Stats" button

## Ranking Logic

### Top Scores Ranking
- **Sort By:** Score (descending), then attempt date (descending)
- **Criteria:** All quiz results
- **Format:** Rank | Username | Score/Total Questions | Accuracy% | Attempt Date

### Highest Accuracy Ranking
- **Sort By:** Accuracy (descending), then attempt date (descending)
- **Criteria:** Score must be at least 50% of total questions
- **Formula:** (Score / Total Questions) × 100
- **Format:** Rank | Username | Score/Total Questions | Accuracy% | Attempt Date

### Fastest Completion Ranking
- **Sort By:** Completion time (ascending - lowest = best)
- **Criteria:** Must have completion time recorded
- **Format:** Rank | Username | Score/Total Questions | Accuracy% | Time (seconds) | Attempt Date

### Weekly Leaderboard
- **Sort By:** Score (descending), then attempt date (descending)
- **Criteria:** Attempts from last 7 days
- **Date Range:** Current DateTime minus 7 days to current DateTime

### User Rank Calculation
- **Process:** Count all users with better score in category
- **Formula:** Rank = (Number of users with better score) + 1
- **Result:** Returns -1 if user not in rankings

## Performance Considerations

### Query Optimization
- Custom queries use indexed fields (username, quizType, score)
- Queries limited to top N results to reduce data transfer
- Lazy loading used for relationships

### Caching Recommendations
- Cache leaderboard results for 5-15 minutes
- Update on each quiz completion
- Consider Redis for distributed caching

### Scalability
- Current design supports 10,000+ users efficiently
- Database indexes recommended on:
  - `quizType` (category)
  - `score` (for sorting)
  - `completionTimeSeconds`
  - `attemptDate` (for weekly queries)
  - `(quizType, score)` - composite index

## Migration Guide

### For Existing Data

If you have existing `QuizResult` data with String scores:

```sql
-- Convert String scores to Integer
UPDATE quiz_result SET score = CAST(score AS INTEGER);
UPDATE quiz_result SET total_questions = CAST(total_questions AS INTEGER);

-- Add new column if needed
ALTER TABLE quiz_result ADD COLUMN completion_time_seconds BIGINT DEFAULT NULL;
```

### Updated QuizResultService

The `QuizResultService` remains backward compatible but now supports:
- Integer scores for better calculations
- Completion time tracking
- Enhanced accuracy calculations

## Future Enhancements

1. **Monthly Leaderboards** - Rankings for each month
2. **Achievement Badges** - Award badges for milestones
3. **Trending Players** - Show players with most improvement
4. **Category-Specific Streaks** - Track win streaks
5. **Global Statistics** - Overall app statistics
6. **Export Functionality** - Export leaderboards to CSV/PDF
7. **Real-time Updates** - WebSocket for live leaderboard updates
8. **Seasonal Rankings** - Reset rankings seasonally

## Troubleshooting

### No Data Appearing
- Ensure quiz results have been submitted with all required fields
- Check that `quizType` field matches category names
- Verify database connection

### Incorrect Rankings
- Clear browser cache
- Verify `completionTimeSeconds` is populated for speed rankings
- Check that `score` and `totalQuestions` are Integer types

### API Errors
- Ensure backend is running on port 8080
- Check CORS configuration
- Verify Spring Boot application is fully started

## Support

For issues or questions:
1. Check the logs in the Spring Boot application
2. Review the API endpoint documentation
3. Verify database schema matches requirements
4. Test endpoints using cURL or Postman

---

**Version:** 1.0  
**Last Updated:** February 21, 2026

