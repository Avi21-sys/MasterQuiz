function login() {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    if (!username || !password) {
        showError("Please enter your username and password.");
        return;
    }

    fetch("http://localhost:8080/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    })
    .then(res => res.json())
    .then(data => {
        if (data.token) {
            localStorage.setItem("username", data.username);
            localStorage.setItem("token",    data.token);
            localStorage.setItem("role",     data.role);   // "USER" or "ADMIN"

            // Redirect based on role
            if (data.role === "ADMIN") {
                window.location.href = "admin.html";
            } else {
                window.location.href = "index.html";
            }
        } else {
            showError(data.message || "Invalid username or password.");
        }
    })
    .catch(err => {
        console.error("Login error:", err);
        showError("Login failed. Please try again.");
    });
}

function register() {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    if (!username || !password) {
        showError("Please enter a username and password.");
        return;
    }

    fetch("http://localhost:8080/api/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    })
    .then(res => res.json())
    .then(data => {
        showSuccess(data.message || "Registration complete! Please log in.");
    })
    .catch(err => {
        console.error("Registration error:", err);
        showError("Registration failed. Please try again.");
    });
}

function showError(msg) {
    const el = document.getElementById("login-message");
    el.textContent = msg;
    el.className = "login-message error";
}

function showSuccess(msg) {
    const el = document.getElementById("login-message");
    el.textContent = msg;
    el.className = "login-message success";
}