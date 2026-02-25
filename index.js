// function startQuiz(quizType){
//     alert(`Starting ${quizType.charAt(0).toUpperCase() + quizType.slice(1)} Quiz!`);
//     //redirecting to corresponding qiuz page based on selected type
//     window.location.href = `${quizType}-quiz.html`;
// }

function startQuiz(category){
    localStorage.setItem("quizCategory", category) //save selected subject               
    alert("Starting " + category + " Quiz!")
    window.location.href ="quiz.html" //redirects 
}