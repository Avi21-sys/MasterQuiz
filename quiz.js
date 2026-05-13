const username = localStorage.getItem("username");

if(!username){
    window.location.href = "login.html";
}

const category = localStorage.getItem("quizCategory") || "JAVA";  //when nothing saved it saved java
document.title = category + " QUIZ";
document.querySelector("h1").innerText = category + " QUIZ";
const API_URL = `http://localhost:8080/api/questions/${category}`;

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

let questions = [];
let currentIndex = 0;
let score = 0;
let timeLeft = 16;
let timerInterval;
let quizStartTime = Date.now();

const correctSound = new Audio("sound/correct.mp3");
const wrongSound = new Audio("sound/wrong.mp3");

// HTML Elements
const questionText = document.getElementById("question-text");
const optionsContainer = document.getElementById("options-container");
const nextButton = document.getElementById("next-button");
const timerEl = document.getElementById("timer");
const currentQ = document.getElementById("current-question");
const totalQ = document.getElementById("total-questions");
const progress = document.getElementById("progress");
const timerCircle = document.getElementById("timer-progress");


// Load questions from backend
async function loadQuestions() {
    try {
        quizStartTime = Date.now();
        const response = await fetchWithAuth(API_URL);
        questions = await response.json();

        totalQ.innerText = questions.length;

        showQuestion();
        startTimer();

    } catch (error) {
        console.error("Error loading questions:", error);
        questionText.innerText = "Failed to load questions!";
    }
}


// Show current question
function showQuestion() {

    resetTimer();

    const q = questions[currentIndex];

    questionText.innerText = q.questionText;
    currentQ.innerText = currentIndex + 1;

    const progressPercent = ((currentIndex + 1) / questions.length) * 100;
    progress.style.width = progressPercent + "%";

    optionsContainer.innerHTML = "";
    nextButton.disabled = true;

    q.options.forEach(option => {

        const btn = document.createElement("button");
        btn.classList.add("option-btn");
        btn.innerText = option;

        btn.onclick = () => selectAnswer(btn, option);

        optionsContainer.appendChild(btn);
    });
}


// Select answer
function selectAnswer(button, selectedAnswer) {

    clearInterval(timerInterval); // stop timer

    const currentQData = questions[currentIndex]; 
    const correctAnswer = currentQData.correctAnswer;

    const buttons = document.querySelectorAll(".option-btn");

    buttons.forEach(btn => btn.disabled = true);

    if (selectedAnswer === correctAnswer) {
        button.classList.add("correct");
        score++;
        correctSound.play();        // Sound
    } else {
        button.classList.add("wrong");
        wrongSound.play();

        // Show correct answer
        buttons.forEach(btn => {
            if (btn.innerText === correctAnswer) {
                btn.classList.add("correct");
            }
        });
    }

    // SHOW EXPLANATION
    const explanationBox = document.createElement("div");
    explanationBox.classList.add("explanation-box");

    explanationBox.innerHTML = `
        <p><strong>Correct Answer:</strong> ${correctAnswer}</p>
        <p><strong>Explanation:</strong> ${currentQData.explanation}</p>
    `;

    optionsContainer.appendChild(explanationBox);

    // ENABLE NEXT BUTTON
    nextButton.disabled = false;

    console.log("Answer clicked");
    console.log("Button enabled:", nextButton.disabled);
}


// Next question
function nextQuestion() {

    currentIndex++;

    if (currentIndex < questions.length) {
        showQuestion();
        startTimer();
    } else {
        showResult();
    }
}


function showResult() {

    clearInterval(timerInterval);
    const completionTimeSeconds = Math.floor((Date.now() - quizStartTime) / 1000);

    // Save result to backend
    fetchWithAuth("http://localhost:8080/api/results/save", {
        method: "POST",
        body: JSON.stringify({
            username: username,
            quizType: category,
            score: score,
            totalQuestions: questions.length,
            completionTimeSeconds: completionTimeSeconds
        })
    });

    // Confetti blast
    confetti({
        particleCount: 150,
        spread: 70,
        origin: { y: 0.6 }
    });

    document.querySelector(".quiz-container").innerHTML = `

        <div class="result-box">

            <h1>Quiz Completed</h1>

            <p>Your Score</p>

            <p>${score} / ${questions.length}</p>

            <p>Time Taken: ${formatTime(completionTimeSeconds)}</p>

            <button class="next-button" onclick="location.reload()">
                Restart Quiz
            </button>

            <button class="next-button" onclick="window.location.href='leaderboard.html'">
                View Leaderboard
            </button>

        </div>

    `;
}

function formatTime(seconds) {
    if (seconds < 60) {
        return `${seconds}s`;
    }

    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}m ${remainingSeconds}s`;
}


// Timer
function startTimer(){

    timeLeft = 16;
    timerEl.innerText = timeLeft;

    const circumference = 188;

    timerInterval = setInterval(() => {

        timeLeft--;

        timerEl.innerText = timeLeft;

        const offset = circumference - (timeLeft / 16) * circumference;
        timerCircle.style.strokeDashoffset = offset;

        if(timeLeft === 0){
            clearInterval(timerInterval);
            autoNext();
        }

    },1000);
}


// Reset timer
function resetTimer() {
    clearInterval(timerInterval);
    timeLeft = 16;
    timerEl.innerText = timeLeft;
    timerCircle.style.strokeDashoffset = 0;
}


// Auto next if time over
function autoNext() {

    const buttons = document.querySelectorAll(".option-btn");

    buttons.forEach(btn => btn.disabled = true);

    nextButton.disabled = false;
}

// Start App
loadQuestions();
