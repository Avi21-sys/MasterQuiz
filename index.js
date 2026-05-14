// ── Auth guard ───────────────────────────────────────────────────────────────
if (!localStorage.getItem("username")) {
    window.location.replace("login.html");
}

// Redirect admins away from the user home page
if (localStorage.getItem("role") === "ADMIN") {
    window.location.replace("admin.html");
}

// ── Quiz launch ───────────────────────────────────────────────────────────────
function startQuiz(category) {
    localStorage.setItem("quizCategory", category);
    alert("Starting " + category + " Quiz!");
    window.location.href = "quiz.html";
}

function logout() {
    localStorage.removeItem("username");
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}

// ── API helpers ───────────────────────────────────────────────────────────────
const LEADERBOARD_API_URL = "http://localhost:8080/api/leaderboard";
const PROFILE_API_URL     = "http://localhost:8080/api/profile";
const TOP_PERFORMER_CATEGORIES = ["JAVA", "PYTHON", "JS", "DOTNET"];
const TOP_PERFORMER_IMAGES = [
    "./images/m1.png", "./images/m2.png", "./images/m3.png",
    "./images/m4.png", "./images/m5.png", "./images/m6.png"
];

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
    localStorage.removeItem("role");
    window.location.href = "login.html";
}

async function fetchWithAuth(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: { ...getAuthHeaders(), ...(options.headers || {}) }
    });
    if (response.status === 401 || response.status === 403) {
        redirectToLogin();
        throw new Error("Authentication required");
    }
    return response;
}

// ── Top Performers ────────────────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
    setupProfilePhotoUpload();
    loadTopPerformers();
    setInterval(loadTopPerformers, 10000);
});

function setupProfilePhotoUpload() {
    const photoInput = document.getElementById("profile-photo-input");
    if (!photoInput) return;

    photoInput.addEventListener("change", async () => {
        const file     = photoInput.files[0];
        const username = localStorage.getItem("username");
        if (!file)     return;
        if (!username) { window.location.href = "login.html"; return; }

        try {
            await uploadProfilePhoto(username, file);
            await loadTopPerformers();
            alert("Profile photo uploaded.");
        } catch (err) {
            console.error("Upload error:", err);
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

    const token   = localStorage.getItem("token");
    const headers = token ? { "Authorization": `Bearer ${token}` } : {};

    const res = await fetch(`${PROFILE_API_URL}/photo`, {
        method: "POST", headers, body: formData
    });
    if (!res.ok) throw new Error("Photo upload failed");
    return res.json();
}

async function loadTopPerformers() {
    const grid   = document.getElementById("top-performer-grid");
    const status = document.getElementById("top-performer-status");
    if (!grid || !status) return;

    try {
        const responses = await Promise.all(
            TOP_PERFORMER_CATEGORIES.map(cat =>
                fetchWithAuth(`${LEADERBOARD_API_URL}/top-scores/${cat}?limit=6`)
                    .then(r => r.ok ? r.json() : Promise.reject(r))
                    .then(d => (d.topScores || []).map(e => ({ ...e, quizType: cat })))
            )
        );

        const topPerformers = getBestPerUser(responses.flat()).slice(0, 6);
        renderTopPerformers(topPerformers);
        status.textContent = `Live top performers updated ${new Date().toLocaleTimeString([], {
            hour: "2-digit", minute: "2-digit"
        })}`;
    } catch (err) {
        console.error("Error loading top performers:", err);
        grid.innerHTML  = '<div class="performer-empty">Live performers are unavailable right now.</div>';
        status.textContent = "Live rankings unavailable";
    }
}

function getBestPerUser(entries) {
    const best = new Map();
    entries.forEach(e => {
        const cur = best.get(e.username);
        if (!cur || isBetterPerformer(e, cur)) best.set(e.username, e);
    });
    return Array.from(best.values()).sort(isBetterPerformerSort);
}

function isBetterPerformer(a, b) {
    if (a.score !== b.score) return a.score > b.score;
    const accA = a.accuracy || 0, accB = b.accuracy || 0;
    if (accA !== accB) return accA > accB;
    return (a.completionTimeSeconds || Number.MAX_SAFE_INTEGER) <
           (b.completionTimeSeconds || Number.MAX_SAFE_INTEGER);
}

function isBetterPerformerSort(a, b) {
    if (b.score !== a.score) return b.score - a.score;
    if ((b.accuracy || 0) !== (a.accuracy || 0)) return (b.accuracy || 0) - (a.accuracy || 0);
    return (a.completionTimeSeconds || Number.MAX_SAFE_INTEGER) -
           (b.completionTimeSeconds || Number.MAX_SAFE_INTEGER);
}

function renderTopPerformers(performers) {
    const grid = document.getElementById("top-performer-grid");
    if (!performers.length) {
        grid.innerHTML = '<div class="performer-empty">No quiz attempts yet. Complete a quiz to appear here.</div>';
        return;
    }

    grid.innerHTML = performers.map((p, i) => {
        const photo    = p.photoUrl
            ? `http://localhost:8080${p.photoUrl}`
            : TOP_PERFORMER_IMAGES[i % TOP_PERFORMER_IMAGES.length];
        const username = escapeHtml(p.username || "Unknown");
        const category = formatCategory(p.quizType);
        const score    = `${p.score}/${p.totalQuestions}`;
        const accuracy = p.accuracy ? `${p.accuracy}%` : "0%";
        const time     = formatPerformerTime(p.completionTimeSeconds);

        return `
            <article class="performer-card">
                <div class="performer-photo-wrap">
                    <img src="${photo}" alt="${username}"
                         onerror="this.src='${TOP_PERFORMER_IMAGES[i % TOP_PERFORMER_IMAGES.length]}'">
                    <span class="performer-rank">#${i + 1}</span>
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
            </article>`;
    }).join("");
}

function formatCategory(cat) {
    return { JAVA: "Java", PYTHON: "Python", JS: "JavaScript", DOTNET: ".NET" }[cat] || cat;
}

function formatPerformerTime(s) {
    if (!s) return "No time";
    if (s < 60) return `${s}s`;
    return `${Math.floor(s / 60)}m ${s % 60}s`;
}

function escapeHtml(v) {
    return String(v)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}