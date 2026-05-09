# Quiz Time Tracking Implementation Guide

## Overview

This guide shows how to update your Quiz frontend to track completion time and send it to the backend.

## Frontend Implementation (quiz.js)

### 1. Add Quiz Timer at the Beginning

```javascript
// Initialize quiz timer at quiz start
let quizStartTime = null;
let quizEndTime = null;

function initializeQuiz() {
    // Record the start time when quiz begins
    quizStartTime = Date.now();
    
    // ... rest of initialization code
}
```

### 2. Update the Answer Submission

Modify your answer submission to include time calculation:

```javascript
function submitQuiz() {
    // Calculate completion time in seconds
    quizEndTime = Date.now();
    const completionTimeSeconds = Math.floor((quizEndTime - quizStartTime) / 1000);
    
    // Calculate correct answers
    const correctAnswers = calculateCorrectAnswers();
    const totalQuestions = questions.length;
    
    // Prepare result object
    const quizResult = {
        username: currentUser || localStorage.getItem('username'),
        quizType: currentQuizCategory,
        score: correctAnswers,
        totalQuestions: totalQuestions,
        completionTimeSeconds: completionTimeSeconds
    };
    
    // Save result to backend
    saveQuizResult(quizResult);
}

function saveQuizResult(result) {
    fetch('http://localhost:8080/api/results/save', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(result)
    })
    .then(response => response.json())
    .then(data => {
        console.log('Quiz result saved successfully:', data);
        // Show results page
        displayResults(data);
    })
    .catch(error => {
        console.error('Error saving quiz result:', error);
        alert('Error saving quiz result. Please try again.');
    });
}

function displayResults(result) {
    const accuracy = ((result.score / result.totalQuestions) * 100).toFixed(2);
    const timeFormatted = formatTime(result.completionTimeSeconds);
    
    document.getElementById('results-container').innerHTML = `
        <div class="results-card">
            <h2>Quiz Complete!</h2>
            <div class="result-stat">
                <span class="label">Score:</span>
                <span class="value">${result.score}/${result.totalQuestions}</span>
            </div>
            <div class="result-stat">
                <span class="label">Accuracy:</span>
                <span class="value">${accuracy}%</span>
            </div>
            <div class="result-stat">
                <span class="label">Time Taken:</span>
                <span class="value">${timeFormatted}</span>
            </div>
            <button onclick="goToLeaderboard()">View Leaderboard</button>
            <button onclick="location.href='index.html'">Back to Home</button>
        </div>
    `;
}

function formatTime(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    if (hours > 0) {
        return `${hours}h ${minutes}m ${secs}s`;
    } else if (minutes > 0) {
        return `${minutes}m ${secs}s`;
    } else {
        return `${secs}s`;
    }
}

function goToLeaderboard() {
    window.location.href = 'leaderboard.html';
}
```

### 3. Display Timer on Quiz Page (Optional)

Add a visible timer on the quiz page:

```html
<!-- Add this to quiz.html -->
<div class="quiz-timer">
    <span id="elapsed-time">0:00</span>
</div>

<style>
.quiz-timer {
    position: fixed;
    top: 20px;
    right: 20px;
    background: rgba(99, 102, 241, 0.1);
    padding: 10px 20px;
    border-radius: 8px;
    font-weight: bold;
}
</style>
```

```javascript
// Update timer every second
let timerInterval;

function startTimerDisplay() {
    timerInterval = setInterval(() => {
        if (quizStartTime) {
            const elapsedSeconds = Math.floor((Date.now() - quizStartTime) / 1000);
            const minutes = Math.floor(elapsedSeconds / 60);
            const seconds = elapsedSeconds % 60;
            
            document.getElementById('elapsed-time').textContent = 
                `${minutes}:${String(seconds).padStart(2, '0')}`;
        }
    }, 1000);
}

function stopTimerDisplay() {
    clearInterval(timerInterval);
}
```

## Backend Implementation (Java)

### 1. Update QuizResultService

```java
@Service
public class QuizResultService {

    @Autowired
    private QuizResultRepo repo;

    public QuizResult saveResult(QuizResult result) {
        // Ensure time is captured
        if (result.getCompletionTimeSeconds() == null) {
            result.setCompletionTimeSeconds(0L);
        }
        
        // Ensure scores are integers
        if (result.getScore() == null) {
            result.setScore(0);
        }
        if (result.getTotalQuestions() == null) {
            result.setTotalQuestions(0);
        }
        
        // Set attempt date if not provided
        if (result.getAttemptDate() == null) {
            result.setAttemptDate(LocalDateTime.now());
        }
        
        return repo.save(result);
    }

    public List<QuizResult> getUserResults(String username) {
        return repo.findByUsername(username);
    }
    
    public List<QuizResult> getUserResultsByCategory(String username, String category) {
        return repo.findByUsernameAndQuizType(username, category);
    }
}
```

### 2. Update QuizResultController

```java
@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "*")
public class QuizResultController {

    @Autowired
    private QuizResultService service;

    @PostMapping("/save")
    public ResponseEntity<QuizResult> saveResult(@RequestBody QuizResult result) {
        try {
            QuizResult savedResult = service.saveResult(result);
            return ResponseEntity.ok(savedResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<QuizResult>> getUserResults(@PathVariable String username) {
        return ResponseEntity.ok(service.getUserResults(username));
    }
    
    @GetMapping("/user/{username}/category/{category}")
    public ResponseEntity<List<QuizResult>> getUserResultsByCategory(
            @PathVariable String username,
            @PathVariable String category) {
        return ResponseEntity.ok(service.getUserResultsByCategory(username, category));
    }
}
```

## JSON Request/Response Examples

### Request to Save Quiz Result

```json
{
    "username": "john_doe",
    "quizType": "Java",
    "score": 85,
    "totalQuestions": 100,
    "completionTimeSeconds": 1200
}
```

### Response

```json
{
    "id": 42,
    "username": "john_doe",
    "quizType": "Java",
    "score": 85,
    "totalQuestions": 100,
    "completionTimeSeconds": 1200,
    "attemptDate": "2026-02-21T15:35:42.123456"
}
```

## Database Changes

### Add Column if Not Present

```sql
ALTER TABLE quiz_result ADD COLUMN completion_time_seconds BIGINT DEFAULT NULL;
```

### Create Index for Performance

```sql
CREATE INDEX idx_quiz_result_completion_time ON quiz_result(completion_time_seconds);
```

## Testing

### Manual Test with cURL

```bash
curl -X POST http://localhost:8080/api/results/save \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "quizType": "Python",
    "score": 90,
    "totalQuestions": 100,
    "completionTimeSeconds": 900
  }'
```

### Test with Postman

1. Create POST request to `http://localhost:8080/api/results/save`
2. Set header: `Content-Type: application/json`
3. Set body (raw JSON):
```json
{
    "username": "postman_test",
    "quizType": "JavaScript",
    "score": 75,
    "totalQuestions": 100,
    "completionTimeSeconds": 1500
}
```
4. Send request

## Complete Quiz Flow

```
1. User clicks "Start Quiz"
   ↓
2. quizStartTime = Date.now()
   Timer display starts
   ↓
3. User completes quiz
   ↓
4. User clicks "Submit"
   ↓
5. Calculate completionTimeSeconds = (Date.now() - quizStartTime) / 1000
   ↓
6. Send POST to /api/results/save with timing data
   ↓
7. Backend validates and stores result
   ↓
8. Display results with time taken
   ↓
9. User can view leaderboard to see ranking
```

## Performance Optimization

### Batch Queries (Optional)

If you want to get all user statistics in one call:

```javascript
async function getUserDashboard(username) {
    try {
        const [results, stats, rank] = await Promise.all([
            fetch(`http://localhost:8080/api/results/user/${username}`).then(r => r.json()),
            fetch(`http://localhost:8080/api/leaderboard/stats/${username}/Java`).then(r => r.json()),
            fetch(`http://localhost:8080/api/leaderboard/rank/${username}/Java`).then(r => r.json())
        ]);
        
        return { results, stats, rank };
    } catch (error) {
        console.error('Error fetching dashboard:', error);
    }
}
```

## Troubleshooting

### Issue: Time showing as null in database
- Ensure completionTimeSeconds is being sent from frontend
- Check that response is received before redirect
- Verify database column exists

### Issue: Time conversion errors
- Use Math.floor() to ensure whole seconds
- Divide milliseconds by 1000
- Format as BIGINT in database (supports up to 9,223,372,036 seconds ≈ 292 billion years)

### Issue: Frontend timer not updating
- Check that interval is properly started
- Verify DOM element exists
- Ensure clearInterval is called on cleanup

## Sample Complete Implementation

See the complete working example in the provided:
- `quiz.js` (update to include timer)
- `leaderboard.html` (displays completion times)
- `leaderboard.js` (formats and displays times)

---

**Note:** This is a comprehensive guide for adding time tracking. Adjust to your specific implementation needs.

