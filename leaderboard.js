// API Base URL
const API_BASE_URL = 'http://localhost:8080/api/leaderboard';

if (!localStorage.getItem('username')) {
    window.location.replace('login.html');
}

// Helper function to get authorization headers
function getAuthHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        ...(token ? { "Authorization": `Bearer ${token}` } : {})
    };
}

function redirectToLogin() {
    localStorage.removeItem('username');
    localStorage.removeItem('token');
    window.location.href = 'login.html';
}

async function fetchWithAuth(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...getAuthHeaders(),
            ...(options.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        redirectToLogin();
        throw new Error('Authentication required');
    }

    return response;
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
    setupTabNavigation();
    setupCategorySelector();
    loadTopScores();
});

/**
 * Setup tab navigation
 */
function setupTabNavigation() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Remove active class from all buttons and contents
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            // Add active class to clicked button and corresponding content
            button.classList.add('active');
            const tabId = button.getAttribute('data-tab');
            document.getElementById(tabId).classList.add('active');

            // Load data based on selected tab
            const category = document.getElementById('category-select').value;
            switch (tabId) {
                case 'top-scores':
                    loadTopScores();
                    break;
                case 'accuracy':
                    loadHighestAccuracy();
                    break;
                case 'fastest':
                    loadFastestCompletion();
                    break;
                case 'weekly':
                    loadWeeklyLeaderboard();
                    break;
                case 'personal':
                    // Personal stats don't auto-load
                    break;
            }
        });
    });
}

/**
 * Setup category selector
 */
function setupCategorySelector() {
    const categorySelect = document.getElementById('category-select');
    categorySelect.addEventListener('change', () => {
        // Reload data for current active tab
        const activeTab = document.querySelector('.tab-btn.active').getAttribute('data-tab');
        switch (activeTab) {
            case 'top-scores':
                loadTopScores();
                break;
            case 'accuracy':
                loadHighestAccuracy();
                break;
            case 'fastest':
                loadFastestCompletion();
                break;
            case 'weekly':
                loadWeeklyLeaderboard();
                break;
        }
    });
}

/**
 * Load top scores leaderboard
 */
function loadTopScores() {
    const category = document.getElementById('category-select').value;
    const contentDiv = document.getElementById('top-scores-content');

    fetchWithAuth(`${API_BASE_URL}/top-scores/${category}?limit=15`)
        .then(response => response.json())
        .then(data => {
            if (data.topScores && data.topScores.length > 0) {
                contentDiv.innerHTML = createLeaderboardTable(data.topScores, 'Top Score');
                updateTimestamp(data.generatedAt);
            } else {
                contentDiv.innerHTML = '<p class="no-data">No data available for this category</p>';
            }
        })
        .catch(error => {
            console.error('Error loading top scores:', error);
            contentDiv.innerHTML = '<p class="error">Failed to load leaderboard</p>';
        });
}

/**
 * Load highest accuracy leaderboard
 */
function loadHighestAccuracy() {
    const category = document.getElementById('category-select').value;
    const contentDiv = document.getElementById('accuracy-content');

    fetchWithAuth(`${API_BASE_URL}/highest-accuracy/${category}?limit=15`)
        .then(response => response.json())
        .then(data => {
            if (data.highestAccuracy && data.highestAccuracy.length > 0) {
                contentDiv.innerHTML = createLeaderboardTable(data.highestAccuracy, 'Accuracy');
                updateTimestamp(data.generatedAt);
            } else {
                contentDiv.innerHTML = '<p class="no-data">No data available for this category</p>';
            }
        })
        .catch(error => {
            console.error('Error loading accuracy:', error);
            contentDiv.innerHTML = '<p class="error">Failed to load leaderboard</p>';
        });
}

/**
 * Load fastest completion leaderboard
 */
function loadFastestCompletion() {
    const category = document.getElementById('category-select').value;
    const contentDiv = document.getElementById('fastest-content');

    fetchWithAuth(`${API_BASE_URL}/fastest-completion/${category}?limit=15`)
        .then(response => response.json())
        .then(data => {
            if (data.fastestCompletion && data.fastestCompletion.length > 0) {
                contentDiv.innerHTML = createLeaderboardTable(data.fastestCompletion, 'Time');
                updateTimestamp(data.generatedAt);
            } else {
                contentDiv.innerHTML = '<p class="no-data">No data available for this category</p>';
            }
        })
        .catch(error => {
            console.error('Error loading fastest completion:', error);
            contentDiv.innerHTML = '<p class="error">Failed to load leaderboard</p>';
        });
}

/**
 * Load weekly leaderboard
 */
function loadWeeklyLeaderboard() {
    const category = document.getElementById('category-select').value;
    const contentDiv = document.getElementById('weekly-content');

    fetchWithAuth(`${API_BASE_URL}/weekly/${category}?limit=15`)
        .then(response => response.json())
        .then(data => {
            if (data.weeklyTopScores && data.weeklyTopScores.length > 0) {
                contentDiv.innerHTML = createLeaderboardTable(data.weeklyTopScores, 'Top Score');
                updateTimestamp(data.generatedAt);
            } else {
                contentDiv.innerHTML = '<p class="no-data">No data available for this week</p>';
            }
        })
        .catch(error => {
            console.error('Error loading weekly leaderboard:', error);
            contentDiv.innerHTML = '<p class="error">Failed to load leaderboard</p>';
        });
}

/**
 * Load personal statistics
 */
function loadPersonalStats() {
    const username = document.getElementById('username-input').value.trim();
    const category = document.getElementById('category-select').value;

    if (!username) {
        alert('Please enter a username');
        return;
    }

    const contentDiv = document.getElementById('personal-stats-content');
    contentDiv.innerHTML = '<div class="loading">Loading...</div>';

    fetchWithAuth(`${API_BASE_URL}/stats/${username}/${category}`)
        .then(response => response.json())
        .then(data => {
            contentDiv.innerHTML = createPersonalStatsView(data);
        })
        .catch(error => {
            console.error('Error loading personal stats:', error);
            contentDiv.innerHTML = '<p class="error">Failed to load statistics</p>';
        });
}

/**
 * Create HTML table for leaderboard
 */
function createLeaderboardTable(entries, sortType) {
    let html = '<table class="leaderboard-table">';
    html += '<thead><tr>';
    html += '<th class="rank-col">Rank</th>';
    html += '<th class="username-col">Username</th>';
    html += '<th class="score-col">Score</th>';
    html += '<th class="accuracy-col">Accuracy</th>';

    if (sortType === 'Time') {
        html += '<th class="time-col">Completion Time</th>';
    }

    html += '<th class="date-col">Attempt Date</th>';
    html += '</tr></thead>';
    html += '<tbody>';

    entries.forEach(entry => {
        const medal = getMedalEmoji(entry.rank);
        const timeStr = sortType === 'Time' ? formatTime(entry.completionTimeSeconds) : formatTime(entry.completionTimeSeconds);

        html += `<tr class="rank-row rank-${entry.rank}">`;
        html += `<td class="rank-cell">${medal} ${entry.rank}</td>`;
        html += `<td class="username-cell">${entry.username}</td>`;
        html += `<td class="score-cell"><strong>${entry.score}/${entry.totalQuestions}</strong></td>`;
        html += `<td class="accuracy-cell">${entry.accuracy}%</td>`;

        if (sortType === 'Time') {
            html += `<td class="time-cell">${timeStr}</td>`;
        }

        html += `<td class="date-cell">${formatDate(entry.attemptDate)}</td>`;
        html += '</tr>';
    });

    html += '</tbody></table>';
    return html;
}

/**
 * Create personal statistics view
 */
function createPersonalStatsView(stats) {
    if (stats.totalAttempts === 0) {
        return `<div class="no-data">
            <p>${stats.message}</p>
        </div>`;
    }

    let html = '<div class="personal-stats">';
    html += `<div class="stat-card">
        <div class="stat-label">Username</div>
        <div class="stat-value">${stats.username}</div>
    </div>`;

    html += `<div class="stat-card">
        <div class="stat-label">Category</div>
        <div class="stat-value">${stats.category}</div>
    </div>`;

    html += `<div class="stat-card">
        <div class="stat-label">Total Attempts</div>
        <div class="stat-value">${stats.totalAttempts}</div>
    </div>`;

    html += `<div class="stat-card highlight">
        <div class="stat-label">Best Score</div>
        <div class="stat-value">${stats.bestScore}/${stats.totalQuestions}</div>
    </div>`;

    html += `<div class="stat-card">
        <div class="stat-label">Average Score</div>
        <div class="stat-value">${stats.averageScore}</div>
    </div>`;

    html += `<div class="stat-card highlight">
        <div class="stat-label">Average Accuracy</div>
        <div class="stat-value">${stats.averageAccuracy}%</div>
    </div>`;

    html += `<div class="stat-card">
        <div class="stat-label">Average Time</div>
        <div class="stat-value">${stats.averageCompletionTime}</div>
    </div>`;

    html += `<div class="stat-card rank-card">
        <div class="stat-label">Your Rank</div>
        <div class="stat-value rank-badge">${stats.userRank}</div>
    </div>`;

    html += '</div>';
    return html;
}

/**
 * Get medal emoji based on rank
 */
function getMedalEmoji(rank) {
    switch (rank) {
        case 1:
            return '#1';
        case 2:
            return '#2';
        case 3:
            return '#3';
        default:
            return '#';
    }
}

/**
 * Format time in seconds to readable format
 */
function formatTime(seconds) {
    if (!seconds || seconds === 0) {
        return 'N/A';
    }

    if (seconds < 60) {
        return `${seconds}s`;
    } else if (seconds < 3600) {
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${minutes}m ${secs}s`;
    } else {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return `${hours}h ${minutes}m`;
    }
}

/**
 * Format date string
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

/**
 * Update last updated timestamp
 */
function updateTimestamp(timestamp) {
    document.getElementById('last-updated').textContent = timestamp;
}

