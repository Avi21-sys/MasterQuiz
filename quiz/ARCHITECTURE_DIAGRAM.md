# Leaderboard System Architecture Diagram

## System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT BROWSER                             │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │              FRONTEND (HTML / CSS / JS)                     │  │
│  │                                                             │  │
│  │  ┌────────────────────────────────────────────────────┐   │  │
│  │  │          leaderboard.html                         │   │  │
│  │  │  - Top Scores Tab                                │   │  │
│  │  │  - Accuracy Tab                                  │   │  │
│  │  │  - Speed Tab                                     │   │  │
│  │  │  - Weekly Tab                                    │   │  │
│  │  │  - Personal Stats Tab                           │   │  │
│  │  └────────────────────────────────────────────────────┘   │  │
│  │                      ↓ (HTTP Requests)                      │  │
│  │  ┌────────────────────────────────────────────────────┐   │  │
│  │  │          leaderboard.js                           │   │  │
│  │  │  - API Communication                             │   │  │
│  │  │  - Event Handling                                │   │  │
│  │  │  - Data Processing                              │   │  │
│  │  │  - DOM Updates                                  │   │  │
│  │  └────────────────────────────────────────────────────┘   │  │
│  │                                                             │  │
│  │  ┌────────────────────────────────────────────────────┐   │  │
│  │  │          leaderboard.css                          │   │  │
│  │  │  - Responsive Layout                             │   │  │
│  │  │  - Styling & Animations                         │   │  │
│  │  │  - Theme Support                                │   │  │
│  │  └────────────────────────────────────────────────────┘   │  │
│  └─────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              ↓ (REST API)
                              ↓ (JSON)
┌─────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT BACKEND                             │
│                                                                     │
│  ┌────────────────────────────────────────────────────────┐        │
│  │              LeaderboardController                     │        │
│  │                                                        │        │
│  │  GET /top-scores/{category}      ←───────────────────┼─────┐  │
│  │  GET /highest-accuracy/{category}                    │     │  │
│  │  GET /fastest-completion/{category}                  │     │  │
│  │  GET /weekly/{category}                              │     │  │
│  │  GET /rank/{username}/{category}                     │     │  │
│  │  GET /stats/{username}/{category}                    │     │  │
│  │  POST /save (indirect)                              │     │  │
│  │  GET /categories                                     │     │  │
│  │  GET /combined/{category}                           │     │  │
│  └────────────────────────────────────────────────────────┘     │  │
│                       ↓ (Delegates to)                           │  │
│  ┌────────────────────────────────────────────────────────┐     │  │
│  │              LeaderboardService                       │     │  │
│  │                                                        │     │  │
│  │  getTopScores()          ─────┐                      │     │  │
│  │  getHighestAccuracy()    ─────┼─→ Ranking Logic      │     │  │
│  │  getFastestCompletion()  ─────┤                      │     │  │
│  │  getWeeklyLeaderboard()       │                      │     │  │
│  │  getUserStats()          ─────┤                      │     │  │
│  │  getUserRankByCategory()      │                      │     │  │
│  │  formatCompletionTime()       │                      │     │  │
│  │                               └─→ Statistics         │     │  │
│  │                                  Calculation          │     │  │
│  └────────────────────────────────────────────────────────┘     │  │
│                       ↓ (Uses)                                   │  │
│  ┌────────────────────────────────────────────────────────┐     │  │
│  │              QuizResultRepo                           │     │  │
│  │  (Enhanced JPA Repository)                           │     │  │
│  │                                                        │     │  │
│  │  findTopScoresByCategory()                           │     │  │
│  │  findHighestAccuracyByCategory()                     │     │  │
│  │  findFastestCompletionByCategory()                   │     │  │
│  │  findWeeklyTopScores()                               │     │  │
│  │  findByUsernameAndQuizType()                        │     │  │
│  │  findBestScoreForUserInCategory()                    │     │  │
│  │  findAllCategories()                                │     │  │
│  └────────────────────────────────────────────────────────┘     │  │
│                       ↓ (Maps to)                                │  │
│  ┌────────────────────────────────────────────────────────┐     │  │
│  │         Data Transfer Objects (DTOs)                 │     │  │
│  │                                                        │     │  │
│  │  LeaderboardEntryDTO     [rank, username, score...]  │     │  │
│  │  TopScoresResponseDTO    [category, topScores...]    │     │  │
│  │  AccuracyLeaderboardDTO  [category, accuracy...]     │     │  │
│  │  FastestCompletionDTO    [category, times...]        │     │  │
│  │  WeeklyLeaderboardDTO    [week, category, scores...]│     │  │
│  │                                                        │     │  │
│  │  (Converted to JSON & returned)                      │     │  │
│  └────────────────────────────────────────────────────────┘     │  │
│                       ↓ (Queries)                                │  │
│  ┌────────────────────────────────────────────────────────┐     │  │
│  │              QuizResult Entity                        │     │  │
│  │                                                        │     │  │
│  │  - id (Long)                                         │     │  │
│  │  - username (String)                                │     │  │
│  │  - quizType (String) - category                     │     │  │
│  │  - score (Integer) ← CHANGED                        │     │  │
│  │  - totalQuestions (Integer) ← CHANGED               │     │  │
│  │  - completionTimeSeconds (Long) ← NEW              │     │  │
│  │  - attemptDate (LocalDateTime)                      │     │  │
│  │  - getAccuracy() → Double ← NEW METHOD              │     │  │
│  │                                                        │     │  │
│  └────────────────────────────────────────────────────────┘     │  │
│                       ↓ (Persists)                               │  │
│  ┌────────────────────────────────────────────────────────┐     │  │
│  │              <@Transactional>                        │     │  │
│  │              JPA / Hibernate ORM                     │     │  │
│  └────────────────────────────────────────────────────────┘     │  │
└─────────────────────────────────────────────────────────────────────┘
                              ↓ (SQL)
┌─────────────────────────────────────────────────────────────────────┐
│                      DATABASE (MySQL/PostgreSQL)                   │
│                                                                     │
│  ┌────────────────────────────────────────────────────────┐        │
│  │              quiz_result TABLE                        │        │
│  │                                                        │        │
│  │  Column               │ Type      │ Changed/New       │        │
│  │  ──────────────────────────────────────────────────    │        │
│  │  id                  │ BIGINT    │                    │        │
│  │  username            │ VARCHAR   │                    │        │
│  │  quiz_type           │ VARCHAR   │   (indexed)        │        │
│  │  score               │ INT       │ ⬆ Changed         │        │
│  │  total_questions     │ INT       │ ⬆ Changed         │        │
│  │  completion_time_sec │ BIGINT    │ ⬆ NEW             │        │
│  │  attempt_date        │ DATETIME  │   (indexed)        │        │
│  │                                                        │        │
│  │  Indexes (Recommended):                              │        │
│  │  - idx_quiz_type                                    │        │
│  │  - idx_score (DESC)                                │        │
│  │  - idx_completion_time                             │        │
│  │  - idx_attempt_date (DESC)                         │        │
│  │  - idx_username_category (composite)              │        │
│  │                                                        │        │
│  └────────────────────────────────────────────────────────┘        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagram

### Getting Top Scores

```
User clicks "Top Scores" Tab
        ↓
JavaScript calculates category
        ↓
leaderboard.js calls fetch()
        ↓
HTTP GET /api/leaderboard/top-scores/{category}
        ↓
LeaderboardController receives request
        ↓
Controller calls LeaderboardService.getTopScores()
        ↓
Service calls QuizResultRepo.findTopScoresByCategory()
        ↓
SQL Query (sorted by score DESC)
        ↓
Database returns QuizResult entities
        ↓
Service maps entities to LeaderboardEntryDTO
        ↓
Service adds rank number to each entry
        ↓
Creates TopScoresResponseDTO wrapper
        ↓
Controller converts to JSON
        ↓
HTTP Response with JSON data
        ↓
JavaScript receives JSON
        ↓
JavaScript processes and formats data
        ↓
JavaScript calls createLeaderboardTable()
        ↓
HTML table created and inserted to DOM
        ↓
CSS styling applied
        ↓
User sees ranked leaderboard with medals 🥇🥈🥉
```

### Getting Personal Statistics

```
User enters username and clicks "Get Stats"
        ↓
JavaScript validates input
        ↓
leaderboard.js calls fetch()
        ↓
HTTP GET /api/leaderboard/stats/{username}/{category}
        ↓
LeaderboardController receives request
        ↓
Controller calls LeaderboardService.getUserStats()
        ↓
Service calls QuizResultRepo.findByUsernameAndQuizType()
        ↓
Database returns all user's results for category
        ↓
Service calculates:
  - Total attempts (count)
  - Best score (max)
  - Average score (mean)
  - Average accuracy (mean of percentages)
  - Average time (mean)
  - User rank (via getUserRankByCategory)
        ↓
Service creates Map<String, Object> with stats
        ↓
Controller converts to JSON
        ↓
HTTP Response with statistics JSON
        ↓
JavaScript receives JSON
        ↓
JavaScript calls createPersonalStatsView()
        ↓
HTML cards created with statistics
        ↓
CSS styling applied (highlight cards for top stats)
        ↓
User sees personal dashboard with all metrics
```

## Component Interaction Matrix

```
                    Controller  Service  Repo  Entity  DTO
┌─────────────────────────────────────────────────────┐
│ frontend request│    ↓
│                 ├──→ validates
│                 ├──→ calls service method
│                 │
│ service         │         ↓
│ (LeaderboardSvc)├──→ calls repo methods
│                 ├──→ performs calculations
│                 ├──→ maps entities to DTOs
│                 │
│ repository      │                ↓
│ (JPA)           ├──→ constructs SQL queries
│                 ├──→ executes on database
│                 ├──→ returns Entity objects
│                 │
│ entity          │                     ↓
│ (QuizResult)    ├──→ data container
│                 ├──→ provides getters
│                 ├──→ has business methods (getAccuracy)
│                 │
│ DTO             │                           ↓
│ (Leaderboard*)  ├──→ JSON representation
│                 ├──→ to frontend
│                 └──→ frontend renders HTML
```

## Request/Response Flow

```
FRONTEND REQUEST
├─ Method: GET
├─ Path: /api/leaderboard/top-scores/Java
├─ Query Params: ?limit=10
└─ Headers: Content-Type: application/json

    ↓ HTTP

BACKEND PROCESSING
├─ LeaderboardController receives request
├─ Extracts path variables & parameters
├─ Calls LeaderboardService.getTopScores("Java", 10)
├─ Service queries database
├─ Service creates LeaderboardEntryDTO list
├─ Service wraps in TopScoresResponseDTO
└─ Converts to JSON

    ↓ HTTP

FRONTEND RESPONSE
├─ Status: 200 OK
├─ Headers: Content-Type: application/json
└─ Body: 
   {
     "category": "Java",
     "topScores": [
       {
         "rank": 1,
         "username": "john_doe",
         "score": 95,
         "totalQuestions": 100,
         "accuracy": 95.0,
         "completionTimeSeconds": 900,
         "quizType": "Java",
         "attemptDate": "2026-02-21 14:30:00"
       },
       ... (more entries)
     ],
     "generatedAt": "2026-02-21 14:35:20"
   }

    ↓ JavaScript

FRONTEND RENDERING
├─ Parse JSON response
├─ Extract topScores array
├─ For each entry:
│  ├─ Create table row
│  ├─ Add rank with medal emoji
│  ├─ Add username
│  ├─ Add score/total
│  ├─ Add accuracy%
│  └─ Add formatted date
├─ Insert table to DOM
├─ Apply CSS styling
└─ Display to user
```

## Database Query Pattern

```
Finding Top Scores:
┌─────────────────────────────────────────────────────┐
│ SQL (auto-generated by JPA):                       │
│                                                     │
│ SELECT * FROM quiz_result                         │
│ WHERE quiz_type = 'Java'                          │
│ ORDER BY score DESC, attempt_date DESC            │
│ LIMIT 10                                          │
│                                                     │
│ Result Set:                                       │
│ ┌─────────────────────────────────────────────┐  │
│ │ id | username | score | total | time | date│  │
│ ├─────────────────────────────────────────────┤  │
│ │ 1  | alice    | 95    | 100   | 900  | ... │  │
│ │ 2  | bob      | 92    | 100   | 1200 | ... │  │
│ │ 3  | charlie  | 90    | 100   | 800  | ... │  │
│ └─────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

        ↓ (Mapping)

┌─────────────────────────────────────────────────────┐
│ Java Objects (QuizResult entities):               │
│                                                     │
│ [                                                  │
│   QuizResult(id=1, username="alice", score=95...),│
│   QuizResult(id=2, username="bob", score=92...),  │
│   QuizResult(id=3, username="charlie", score=90...)
│ ]                                                  │
└─────────────────────────────────────────────────────┘

        ↓ (DTO Conversion & Ranking)

┌─────────────────────────────────────────────────────┐
│ DTOs (LeaderboardEntryDTO list):                  │
│                                                     │
│ TopScoresResponseDTO {                            │
│   category: "Java",                              │
│   topScores: [                                    │
│     LeaderboardEntryDTO(                          │
│       rank=1,                                     │
│       username="alice",                          │
│       score=95,                                  │
│       accuracy=95.0,                             │
│       time=900                                   │
│     ),                                           │
│     LeaderboardEntryDTO(...),                    │
│     ...                                          │
│   ],                                             │
│   generatedAt: "2026-02-21 14:35:20"            │
│ }                                                 │
└─────────────────────────────────────────────────────┘

        ↓ (JSON Serialization)

┌─────────────────────────────────────────────────────┐
│ JSON Response:                                     │
│                                                     │
│ {                                                  │
│   "category": "Java",                            │
│   "topScores": [                                 │
│     {                                            │
│       "rank": 1,                                 │
│       "username": "alice",                       │
│       "score": 95,                               │
│       "totalQuestions": 100,                     │
│       "accuracy": 95.0,                          │
│       "completionTimeSeconds": 900,              │
│       "quizType": "Java",                        │
│       "attemptDate": "2026-02-21 14:30:00"      │
│     },                                           │
│     ...                                          │
│   ],                                             │
│   "generatedAt": "2026-02-21 14:35:20"          │
│ }                                                 │
└─────────────────────────────────────────────────────┘
```

## Ranking Algorithm

```
ACCURACY RANKING:

Input: List<QuizResult> for category
Output: Ranked list sorted by accuracy

Step 1: Filter
├─ Remove results with score < 50% of total
└─ Keep only high-quality attempts

Step 2: Calculate Accuracy
├─ For each result: accuracy = (score / totalQuestions) × 100
└─ Store as Double (e.g., 95.5)

Step 3: Sort
├─ Primary: By accuracy (descending - highest first)
├─ Secondary: By attemptDate (descending - most recent first)
└─ Result: Ranked list

Step 4: Add Ranks
├─ Counter = 1
├─ For each result in sorted list:
│  ├─ Assign rank = counter
│  ├─ Create LeaderboardEntryDTO
│  ├─ counter++
│  └─ Add to result list
└─ Result: Ranked entries with rank numbers

Step 5: Return
└─ Wrap in AccuracyLeaderboardDTO with metadata
```

---

**System is modular, scalable, and follows Spring Boot best practices!**

