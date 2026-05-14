// ── Auth guard ────────────────────────────────────────────────────────────────
const CURRENT_USER = localStorage.getItem("username");
const ROLE         = localStorage.getItem("role");
const TOKEN        = localStorage.getItem("token");

if (!TOKEN || ROLE !== "ADMIN") {
    window.location.replace("login.html");
}

document.getElementById("admin-name").textContent = `👤 ${CURRENT_USER}`;

const API = "http://localhost:8080/api";

// ── Auth helper ───────────────────────────────────────────────────────────────
function authHeaders() {
    return {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${TOKEN}`
    };
}

async function apiFetch(url, options = {}) {
    const res = await fetch(url, {
        ...options,
        headers: { ...authHeaders(), ...(options.headers || {}) }
    });

    if (res.status === 401 || res.status === 403) {
        logout();
        throw new Error("Session expired");
    }

    return res;
}

// ── Toast ─────────────────────────────────────────────────────────────────────
let toastTimer;
function showToast(msg, type = "success") {
    const el = document.getElementById("toast");
    el.textContent  = msg;
    el.className    = `toast ${type}`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { el.className = "toast hidden"; }, 3000);
}

// ── Navigation ────────────────────────────────────────────────────────────────
function showSection(name) {
    document.querySelectorAll(".section").forEach(s => s.classList.remove("active"));
    document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));

    document.getElementById(`section-${name}`).classList.add("active");
    document.querySelector(`.nav-btn[onclick="showSection('${name}')"]`).classList.add("active");

    if (name === "questions") loadQuestions();
    if (name === "users")     loadUsers();
}

// ── Logout ────────────────────────────────────────────────────────────────────
function logout() {
    ["username", "token", "role"].forEach(k => localStorage.removeItem(k));
    window.location.href = "login.html";
}

// ══════════════════════════════════════════════════════════════════════════════
//  QUESTIONS
// ══════════════════════════════════════════════════════════════════════════════
let allQuestions = [];

async function loadQuestions() {
    const tbody = document.getElementById("questions-tbody");
    tbody.innerHTML = `<tr><td colspan="5" class="loading-cell">Loading…</td></tr>`;

    try {
        const res  = await apiFetch(`${API}/admin/questions`);
        const data = await res.json();

        const filter = document.getElementById("q-filter-category").value;
        allQuestions = filter === "ALL"
            ? data
            : data.filter(q => q.category === filter);

        renderQuestions(allQuestions);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="loading-cell" style="color:#ef4444">
            Failed to load questions.</td></tr>`;
    }
}

function renderQuestions(list) {
    const tbody = document.getElementById("questions-tbody");
    if (!list.length) {
        tbody.innerHTML = `<tr><td colspan="5" class="loading-cell">No questions found.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(q => `
        <tr>
            <td>${q.questionId}</td>
            <td><span class="badge badge-user">${q.category}</span></td>
            <td class="question-cell" title="${escHtml(q.questionText)}">${escHtml(q.questionText)}</td>
            <td>${escHtml(q.correctAnswer)}</td>
            <td class="action-cell">
                <button class="btn btn-sm btn-primary" onclick="openModal('edit', ${q.questionId})">Edit</button>
                <button class="btn btn-sm btn-danger"  onclick="promptDeleteQuestion(${q.questionId})">Delete</button>
            </td>
        </tr>`).join("");
}

function filterTable() {
    const term = document.getElementById("q-search").value.toLowerCase();
    const filtered = allQuestions.filter(q =>
        q.questionText.toLowerCase().includes(term) ||
        q.category.toLowerCase().includes(term) ||
        q.correctAnswer.toLowerCase().includes(term)
    );
    renderQuestions(filtered);
}

// ── Add / Edit modal ──────────────────────────────────────────────────────────
function openModal(mode, id = null) {
    clearModal();
    document.getElementById("modal-title").textContent =
        mode === "add" ? "Add Question" : "Edit Question";
    document.getElementById("question-modal").classList.remove("hidden");

    if (mode === "edit" && id !== null) {
        const q = allQuestions.find(q => q.questionId === id);
        if (!q) return;
        document.getElementById("edit-id").value           = q.questionId;
        document.getElementById("modal-category").value    = q.category;
        document.getElementById("modal-question").value    = q.questionText;
        document.getElementById("modal-answer").value      = q.correctAnswer;
        document.getElementById("modal-explanation").value = q.explanation || "";
        document.getElementById("modal-options").value     =
            (q.options || []).map(o => o.optionText || o).join("\n");
    }
}

function closeModal() {
    document.getElementById("question-modal").classList.add("hidden");
    clearModal();
}

function clearModal() {
    ["edit-id","modal-question","modal-options","modal-answer","modal-explanation"]
        .forEach(id => { document.getElementById(id).value = ""; });
    document.getElementById("modal-category").value = "JAVA";
}

async function saveQuestion() {
    const id          = document.getElementById("edit-id").value;
    const category    = document.getElementById("modal-category").value;
    const questionText= document.getElementById("modal-question").value.trim();
    const optionsRaw  = document.getElementById("modal-options").value.trim();
    const correctAnswer=document.getElementById("modal-answer").value.trim();
    const explanation = document.getElementById("modal-explanation").value.trim();

    if (!questionText || !correctAnswer) {
        showToast("Question text and correct answer are required.", "error"); return;
    }

    const options = optionsRaw
        .split("\n")
        .map(s => s.trim())
        .filter(Boolean);

    if (options.length < 2) {
        showToast("Please provide at least 2 options.", "error"); return;
    }

    if (!options.includes(correctAnswer)) {
        showToast("Correct answer must exactly match one of the options.", "error"); return;
    }

    const payload = { category, questionText, correctAnswer, explanation, options };

    try {
        const isEdit = id !== "";
        const res    = await apiFetch(
            isEdit ? `${API}/admin/questions/${id}` : `${API}/admin/questions`,
            { method: isEdit ? "PUT" : "POST", body: JSON.stringify(payload) }
        );

        if (!res.ok) throw new Error("Save failed");

        showToast(isEdit ? "Question updated!" : "Question added!");
        closeModal();
        loadQuestions();
    } catch {
        showToast("Could not save question. Please try again.", "error");
    }
}

// ── Delete question ───────────────────────────────────────────────────────────
let pendingDeleteFn = null;

function promptDeleteQuestion(id) {
    document.getElementById("confirm-text").textContent =
        `Delete question #${id}? This cannot be undone.`;
    pendingDeleteFn = () => deleteQuestion(id);
    document.getElementById("confirm-modal").classList.remove("hidden");
}

function closeConfirm() {
    document.getElementById("confirm-modal").classList.add("hidden");
    pendingDeleteFn = null;
}

function confirmAction() {
    if (pendingDeleteFn) pendingDeleteFn();
    closeConfirm();
}

async function deleteQuestion(id) {
    try {
        const res = await apiFetch(`${API}/admin/questions/${id}`, { method: "DELETE" });
        if (!res.ok) throw new Error();
        showToast("Question deleted.");
        loadQuestions();
    } catch {
        showToast("Could not delete question.", "error");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  USERS
// ══════════════════════════════════════════════════════════════════════════════
let allUsers = [];

async function loadUsers() {
    const tbody = document.getElementById("users-tbody");
    tbody.innerHTML = `<tr><td colspan="4" class="loading-cell">Loading…</td></tr>`;

    try {
        const res  = await apiFetch(`${API}/admin/users`);
        allUsers   = await res.json();
        renderUsers(allUsers);
    } catch {
        tbody.innerHTML = `<tr><td colspan="4" class="loading-cell" style="color:#ef4444">
            Failed to load users.</td></tr>`;
    }
}

function renderUsers(list) {
    const tbody = document.getElementById("users-tbody");
    if (!list.length) {
        tbody.innerHTML = `<tr><td colspan="4" class="loading-cell">No users found.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(u => {
        const isAdmin    = u.role === "ADMIN";
        const isSelf     = u.username === CURRENT_USER;
        const badgeCls   = isAdmin ? "badge-admin" : "badge-user";
        const badgeLbl   = isAdmin ? "Admin" : "User";

        const promoteBtn = !isAdmin && !isSelf
            ? `<button class="btn btn-sm btn-warn"
                       onclick="changeRole('${u.username}','promote')">→ Admin</button>`
            : "";

        const demoteBtn  = isAdmin && !isSelf
            ? `<button class="btn btn-sm btn-secondary"
                       onclick="changeRole('${u.username}','demote')">→ User</button>`
            : "";

        const deleteBtn  = !isSelf
            ? `<button class="btn btn-sm btn-danger"
                       onclick="promptDeleteUser('${u.username}')">Delete</button>`
            : `<span style="color:var(--text-muted);font-size:.8em">(you)</span>`;

        return `
            <tr>
                <td>${u.id}</td>
                <td><strong>${escHtml(u.username)}</strong></td>
                <td><span class="badge ${badgeCls}">${badgeLbl}</span></td>
                <td class="action-cell">${promoteBtn}${demoteBtn}${deleteBtn}</td>
            </tr>`;
    }).join("");
}

function filterUsers() {
    const term     = document.getElementById("u-search").value.toLowerCase();
    const filtered = allUsers.filter(u =>
        u.username.toLowerCase().includes(term) ||
        u.role.toLowerCase().includes(term)
    );
    renderUsers(filtered);
}

async function changeRole(username, action) {
    try {
        const res = await apiFetch(`${API}/admin/users/${username}/${action}`, { method: "PUT" });
        if (!res.ok) throw new Error();
        showToast(`${username} ${action === "promote" ? "promoted to Admin" : "demoted to User"}.`);
        loadUsers();
    } catch {
        showToast("Could not update role.", "error");
    }
}

function promptDeleteUser(username) {
    document.getElementById("confirm-text").textContent =
        `Permanently delete user "${username}"? All their data will be lost.`;
    pendingDeleteFn = () => deleteUser(username);
    document.getElementById("confirm-modal").classList.remove("hidden");
}

async function deleteUser(username) {
    try {
        const res = await apiFetch(`${API}/admin/users/${username}`, { method: "DELETE" });
        if (!res.ok) throw new Error();
        showToast(`User "${username}" deleted.`);
        loadUsers();
    } catch {
        showToast("Could not delete user.", "error");
    }
}

// ── Utility ───────────────────────────────────────────────────────────────────
function escHtml(v = "") {
    return String(v)
        .replace(/&/g,"&amp;").replace(/</g,"&lt;")
        .replace(/>/g,"&gt;").replace(/"/g,"&quot;");
}

// ── Boot ──────────────────────────────────────────────────────────────────────
loadQuestions();   // default section on load