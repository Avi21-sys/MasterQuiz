function startQuiz(quizType){
    alert(`Starting ${quizType.charAt(0).toUpperCase() + quizType.slice(1)} Quiz!`);
    //redirecting to corresponding qiuz page based on selected type
    window.location.href = `${quizType}-quiz.html`;
}