// function startQuiz(quizType){
//     alert(`Starting ${quizType.charAt(0).toUpperCase() + quizType.slice(1)} Quiz!`);
//     //redirecting to corresponding qiuz page based on selected type
//     window.location.href = `${quizType}-quiz.html`;
// }

if (!localStorage.getItem("username")) {
    window.location.replace("login.html");
}

function startQuiz(category){
    localStorage.setItem("quizCategory", category) //save selected subject               
    alert("Starting " + category + " Quiz!")
    window.location.href ="quiz.html" //redirects 
}

const LEADERBOARD_API_URL = "http://localhost:8080/api/leaderboard";
const PROFILE_API_URL = "http://localhost:8080/api/profile";
const TOP_PERFORMER_CATEGORIES = ["JAVA", "PYTHON", "JS", "DOTNET"];
const TOP_PERFORMER_IMAGES = [
    "./images/m1.png",
    "./images/m2.png",
    "./images/m3.png",
    "./images/m4.png",
    "./images/m5.png",
    "./images/m6.png"
];

// Helper function to get authorization headers
function getAuthHeaders() {
    const token = localStorage.getItem("token");
    return {
        "Content-Type": "application/json",
        ...(token ? { "Authorization": `Bearer ${token}` } : {})
    };
}

function redirectToLogin() {
    localStorage.removeItem("username");
    localStorage.removeItem("token");
    window.location.href = "login.html";
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
        throw new Error("Authentication required");
    }

    return response;
}

document.addEventListener("DOMContentLoaded", () => {
    setupProfilePhotoUpload();
    loadTopPerformers();
    setInterval(loadTopPerformers, 10000);
});

function setupProfilePhotoUpload() {
    const photoInput = document.getElementById("profile-photo-input");

    if (!photoInput) {
        return;
    }

    photoInput.addEventListener("change", async () => {
        const file = photoInput.files[0];
        const username = localStorage.getItem("username");

        if (!file) {
            return;
        }

        if (!username) {
            photoInput.value = "";
            window.location.href = "login.html";
            return;
        }

        try {
            await uploadProfilePhoto(username, file);
            await loadTopPerformers();
            alert("Profile photo uploaded.");
        } catch (error) {
            console.error("Error uploading profile photo:", error);
            alert("Could not upload your photo. Please try again.");
        } finally {
            photoInput.value = "";
        }
    });
}

async function uploadProfilePhoto(username, file) {
    const formData = new FormData();
    formData.append("username", username);
    formData.append("photo", file);

    const token = localStorage.getItem("token");
    const headers = token ? { "Authorization": `Bearer ${token}` } : {};

    const response = await fetch(`${PROFILE_API_URL}/photo`, {
        method: "POST",
        headers: headers,
        body: formData
    });

    if (!response.ok) {
        throw new Error("Photo upload failed");
    }

    return response.json();
}

async function loadTopPerformers() {
    const grid = document.getElementById("top-performer-grid");
    const status = document.getElementById("top-performer-status");

    if (!grid || !status) {
        return;
    }

    try {
        const responses = await Promise.all(
            TOP_PERFORMER_CATEGORIES.map(category =>
                fetchWithAuth(`${LEADERBOARD_API_URL}/top-scores/${category}?limit=6`)
                    .then(response => response.ok ? response.json() : Promise.reject(response))
                    .then(data => (data.topScores || []).map(entry => ({
                        ...entry,
                        quizType: category
                    })))
            )
        );

        const topPerformers = getBestPerUser(responses.flat()).slice(0, 6);
        renderTopPerformers(topPerformers);
        status.textContent = `Live top performers updated ${new Date().toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit"
        })}`;
    } catch (error) {
        console.error("Error loading live top performers:", error);
        grid.innerHTML = '<div class="performer-empty">Live performers are unavailable right now.</div>';
        status.textContent = "Live rankings unavailable";
    }
}

function getBestPerUser(entries) {
    const bestByUser = new Map();

    entries.forEach(entry => {
        const username = entry.username || "Unknown";
        const currentBest = bestByUser.get(username);

        if (!currentBest || isBetterPerformer(entry, currentBest)) {
            bestByUser.set(username, entry);
        }
    });

    return Array.from(bestByUser.values()).sort(isBetterPerformerSort);
}

function isBetterPerformer(entry, currentBest) {
    if (entry.score !== currentBest.score) {
        return entry.score > currentBest.score;
    }

    const entryAccuracy = entry.accuracy || 0;
    const bestAccuracy = currentBest.accuracy || 0;

    if (entryAccuracy !== bestAccuracy) {
        return entryAccuracy > bestAccuracy;
    }

    const entryTime = entry.completionTimeSeconds || Number.MAX_SAFE_INTEGER;
    const bestTime = currentBest.completionTimeSeconds || Number.MAX_SAFE_INTEGER;
    return entryTime < bestTime;
}

function isBetterPerformerSort(a, b) {
    if (b.score !== a.score) {
        return b.score - a.score;
    }

    if ((b.accuracy || 0) !== (a.accuracy || 0)) {
        return (b.accuracy || 0) - (a.accuracy || 0);
    }

    return (a.completionTimeSeconds || Number.MAX_SAFE_INTEGER) -
        (b.completionTimeSeconds || Number.MAX_SAFE_INTEGER);
}

function renderTopPerformers(performers) {
    const grid = document.getElementById("top-performer-grid");

    if (performers.length === 0) {
        grid.innerHTML = '<div class="performer-empty">No quiz attempts yet. Complete a quiz to appear here.</div>';
        return;
    }

    grid.innerHTML = performers.map((performer, index) => {
        const photo = getPerformerPhoto(performer, index);
        const username = escapeHtml(performer.username || "Unknown");
        const category = formatCategory(performer.quizType);
        const score = `${performer.score}/${performer.totalQuestions}`;
        const accuracy = performer.accuracy ? `${performer.accuracy}%` : "0%";
        const time = formatPerformerTime(performer.completionTimeSeconds);

        return `
            <article class="performer-card">
                <div class="performer-photo-wrap">
                    <img src="${photo}" alt="${username}" onerror="this.src='${TOP_PERFORMER_IMAGES[index % TOP_PERFORMER_IMAGES.length]}'">
                    <span class="performer-rank">#${index + 1}</span>
                </div>
                <div class="performer-details">
                    <h3>${username}</h3>
                    <p>${category}</p>
                    <div class="performer-stats">
                        <span>${score}</span>
                        <span>${accuracy}</span>
                        <span>${time}</span>
                    </div>
                </div>
            </article>
        `;
    }).join("");
}

function getPerformerPhoto(performer, index) {
    if (performer.photoUrl) {
        return `http://localhost:8080${performer.photoUrl}`;
    }

    return TOP_PERFORMER_IMAGES[index % TOP_PERFORMER_IMAGES.length];
}

function formatCategory(category) {
    const labels = {
        JAVA: "Java",
        PYTHON: "Python",
        JS: "JavaScript",
        DOTNET: ".NET"
    };

    return labels[category] || category;
}

function formatPerformerTime(seconds) {
    if (!seconds) {
        return "No time";
    }

    if (seconds < 60) {
        return `${seconds}s`;
    }

    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}m ${remainingSeconds}s`;
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
