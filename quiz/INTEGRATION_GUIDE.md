# Quiz Leaderboard System - Integration Guide

## Quick Start

This guide explains how to integrate the new leaderboard system into your existing Quiz App.

## What Has Been Added

### Backend Components

#### 1. Enhanced QuizResult Entity
**File:** `src/main/java/com/dev/QuizApp/entity/QuizResult.java`

**Changes:**
- Changed `score` from String to Integer
- Changed `totalQuestions` from String to Integer
- Added `completionTimeSeconds` field (Long)
- Added `getAccuracy()` method for automatic accuracy calculation

**Migration Note:** If you have existing data with string scores, run:
```sql
UPDATE quiz_result SET score = CAST(score AS INTEGER);
UPDATE quiz_result SET total_questions = CAST(total_questions AS INTEGER);
ALTER TABLE quiz_result ADD COLUMN completion_time_seconds BIGINT DEFAULT NULL;
```

#### 2. Data Transfer Objects (DTOs)
**Location:** `src/main/java/com/dev/QuizApp/dto/`

New DTOs:
- `LeaderboardEntryDTO.java` - Single leaderboard entry
- `TopScoresResponseDTO.java` - Top scores response
- `AccuracyLeaderboardDTO.java` - Accuracy rankings response
- `FastestCompletionDTO.java` - Speed rankings response
- `WeeklyLeaderboardDTO.java` - Weekly rankings response

#### 3. Enhanced Repository
**File:** `src/main/java/com/dev/QuizApp/repository/QuizResultRepo.java`

**New Query Methods:**
```
- findTopScoresByCategory(category)
- findHighestAccuracyByCategory(category)
- findFastestCompletionByCategory(category)
- findWeeklyTopScores(category, startDate)
- findAllCategories()
- findByUsernameAndQuizType(username, category)
- findBestScoreForUserInCategory(username, category)
```

#### 4. LeaderboardService
**File:** `src/main/java/com/dev/QuizApp/service/LeaderboardService.java`

**Key Methods:**
```
- getTopScores(category, limit)
- getHighestAccuracy(category, limit)
- getFastestCompletion(category, limit)
- getWeeklyLeaderboard(category, limit)
- getOverallLeaderboard(limit)
- getUserStats(username, category)
- getUserRankByCategory(username, category)
```

#### 5. LeaderboardController
**File:** `src/main/java/com/dev/QuizApp/controller/LeaderboardController.java`

**Endpoints:**
```
GET /api/leaderboard/top-scores/{category}
GET /api/leaderboard/highest-accuracy/{category}
GET /api/leaderboard/fastest-completion/{category}
GET /api/leaderboard/weekly/{category}
GET /api/leaderboard/overall
GET /api/leaderboard/rank/{username}/{category}
GET /api/leaderboard/stats/{username}/{category}
GET /api/leaderboard/categories
GET /api/leaderboard/combined/{category}
```

### Frontend Components

#### 1. Leaderboard Page
**File:** `leaderboard.html`

- Tab-based navigation for different leaderboard types
- Category selector dropdown
- Personal statistics viewer
- Responsive design

#### 2. Frontend Logic
**File:** `leaderboard.js`

- API communication
- Dynamic table generation
- Personal stats visualization
- Time formatting utilities

#### 3. Styling
**File:** `leaderboard.css`

- Modern gradient design
- Responsive layout
- Animation effects
- Mobile-friendly

#### 4. Documentation
**File:** `LEADERBOARD_DOCUMENTATION.md`

Complete technical documentation including:
- System architecture
- Database design
- API references
- Usage examples
- Troubleshooting guide

## Integration Steps

### Step 1: Update QuizResult When Saving Results

**Current Code (QuizResultService):**
```java
public QuizResult saveResult(QuizResult result){
    return repo.save(result);
}
```

**Should be updated to capture completion time:**
```java
public QuizResult saveResult(QuizResult result){
    // Ensure time is captured
    if (result.getCompletionTimeSeconds() == null) {
        result.setCompletionTimeSeconds(0L);
    }
    // Convert scores to integers if they're coming as strings
    if (result.getScore() instanceof String) {
        result.setScore(Integer.parseInt((String) result.getScore()));
    }
    if (result.getTotalQuestions() instanceof String) {
        result.setTotalQuestions(Integer.parseInt((String) result.getTotalQuestions()));
    }
    return repo.save(result);
}
```

### Step 2: Update Frontend Quiz to Track Time

**In `quiz.js`, add time tracking:**
```javascript
let startTime = Date.now();
let quizTimeSeconds = 0;

// When quiz completes:
quizTimeSeconds = Math.floor((Date.now() - startTime) / 1000);

// When saving result:
const result = {
    username: currentUser,
    quizType: currentCategory,
    score: correctAnswers,
    totalQuestions: totalQuestions,
    completionTimeSeconds: quizTimeSeconds
};

fetch('http://localhost:8080/api/results/save', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(result)
});
```

### Step 3: Add Leaderboard Link to Main Page

**Update `index.html` to add leaderboard link:**
```html
<nav>
    <a href="index.html">Dashboard</a>
    <a href="quiz.html">Take Quiz</a>
    <a href="leaderboard.html">Leaderboard</a>  <!-- Add this -->
    <a href="login.html">Logout</a>
</nav>
```

### Step 4: Configure CORS (if needed)

The controllers already have `@CrossOrigin(origins = "*")`, but if you need more restrictive settings:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:8080")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);
    }
}
```

## API Examples

### JavaScript/Frontend Usage

```javascript
// Get top scores
const response = await fetch('http://localhost:8080/api/leaderboard/top-scores/Java');
const data = await response.json();
console.log(data.topScores);

// Get user stats
const statsResponse = await fetch('http://localhost:8080/api/leaderboard/stats/john_doe/Python');
const stats = await statsResponse.json();
console.log(stats);

// Get user rank
const rankResponse = await fetch('http://localhost:8080/api/leaderboard/rank/john_doe/React');
const rank = await rankResponse.json();
console.log(`Rank: ${rank.rank}`);
```

## Testing

### Test the Backend

1. **Start Spring Boot Application**
   ```bash
   cd QuizApp
   mvn spring-boot:run
   ```

2. **Create Test Data**
   ```bash
   curl -X POST http://localhost:8080/api/results/save \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "quizType": "Java",
       "score": 85,
       "totalQuestions": 100,
       "completionTimeSeconds": 1200
     }'
   ```

3. **Query Leaderboard**
   ```bash
   curl http://localhost:8080/api/leaderboard/top-scores/Java
   ```

### Test the Frontend

1. Open `leaderboard.html` in browser
2. Test each tab
3. Change categories
4. Enter username to view personal stats
5. Verify responsive design on mobile

## Database Schema

### SQL to Create Indexes (Optional but Recommended)

```sql
-- Create indexes for leaderboard queries
CREATE INDEX idx_quiz_result_quiz_type ON quiz_result(quiz_type);
CREATE INDEX idx_quiz_result_score ON quiz_result(score DESC);
CREATE INDEX idx_quiz_result_completion_time ON quiz_result(completion_time_seconds);
CREATE INDEX idx_quiz_result_attempt_date ON quiz_result(attempt_date DESC);
CREATE INDEX idx_quiz_result_username_category ON quiz_result(username, quiz_type);
CREATE INDEX idx_quiz_result_score_calc ON quiz_result(score DESC, total_questions, quiz_type);
```

## File Structure

```
Quiz App/
├── quix-app/
│   ├── index.html
│   ├── quiz.html
│   ├── leaderboard.html          ← NEW
│   ├── login.html
│   ├── index.js
│   ├── quiz.js
│   ├── leaderboard.js            ← NEW
│   ├── login.js
│   ├── style.css
│   ├── leaderboard.css           ← NEW
│   ├── images/
│   └── sound/
│
└── quiz/QuizApp/
    ├── pom.xml
    ├── LEADERBOARD_DOCUMENTATION.md  ← NEW
    └── src/main/java/com/dev/QuizApp/
        ├── controller/
        │   ├── LoginController.java
        │   ├── QuizResultController.java
        │   └── LeaderboardController.java        ← NEW
        ├── service/
        │   ├── QuestionService.java
        │   ├── QuizResultService.java
        │   └── LeaderboardService.java           ← NEW
        ├── dto/
        │   ├── LoginRequest.java
        │   ├── QuestionDTO.java
        │   ├── LeaderboardEntryDTO.java          ← NEW
        │   ├── TopScoresResponseDTO.java         ← NEW
        │   ├── AccuracyLeaderboardDTO.java       ← NEW
        │   ├── FastestCompletionDTO.java         ← NEW
        │   └── WeeklyLeaderboardDTO.java         ← NEW
        ├── entity/
        │   ├── Question.java
        │   ├── QuizResult.java (UPDATED)
        │   ├── Options.java
        │   └── ...
        └── repository/
            ├── QuestionRepo.java
            ├── QuizResultRepo.java (UPDATED)
            └── ...
```

## Breaking Changes

### Score and Total Questions

The `score` and `totalQuestions` fields have changed from String to Integer.

**If using raw JSON:**
```javascript
// Before (String)
{ "score": "85", "totalQuestions": "100" }

// After (Integer)
{ "score": 85, "totalQuestions": 100 }
```

**Update Frontend Code:**
```javascript
// Before
const score = parseInt(result.score);

// After
const score = result.score; // Already an integer
```

## Troubleshooting

### Issue: "Leaderboard endpoints not found"
- Ensure LeaderboardController.java is in the correct package
- Restart Spring Boot application
- Check that @ComponentScan includes the controller package

### Issue: "No data showing in leaderboard"
- Verify quiz results are being saved
- Check that `quizType` field matches category names exactly
- Ensure database migration has been run for field type changes

### Issue: "Accuracy showing 0%"
- Verify `score` and `totalQuestions` are not null
- Check that they are Integer type (not String)
- Ensure `totalQuestions` is not zero

### Issue: "Weekly leaderboard empty"
- Ensure quiz results have recent `attemptDate` values
- Check that current date is correctly set on server

## Performance Tips

1. **Limit Results:** Always use reasonable limits (e.g., 10-50)
2. **Cache Results:** Consider caching leaderboard data for 5-15 minutes
3. **Database Indexes:** Create indexes on frequently queried fields
4. **Pagination:** For large datasets, implement pagination

## Next Steps

1. Deploy updated backend to production
2. Test with sample data
3. Integrate leaderboard link into main application
4. Train users on leaderboard features
5. Monitor performance and gather feedback

## Support & Maintenance

- Document any customizations made
- Keep DTOs and controllers in sync
- Monitor database performance
- Regularly review leaderboard accuracy
- Archive old data periodically if needed

---

**For detailed technical documentation, see:** `LEADERBOARD_DOCUMENTATION.md`

