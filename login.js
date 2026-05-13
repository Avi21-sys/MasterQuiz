function login(){

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    fetch("http://localhost:8080/api/login",{
        method:"POST",
        headers:{
            "Content-Type":"application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    })

    .then(res => res.json())
    .then(data => {

        if(data.token){

            localStorage.setItem("username", username);
            localStorage.setItem("token", data.token);

            window.location.href = "index.html";
        }
        else{
            alert(data.message || "Invalid Username or Password");
        }

    })
    .catch(error => {
        console.error("Login error:", error);
        alert("Login failed. Please try again.");
    });

}

function register(){

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    if (!username || !password) {
        alert("Please enter a username and password");
        return;
    }

    fetch("http://localhost:8080/api/register",{
        method:"POST",
        headers:{
            "Content-Type":"application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
    .then(res => res.json())
    .then(data => {
        alert(data.message || "Registration complete");
    })
    .catch(error => {
        console.error("Registration error:", error);
        alert("Registration failed. Please try again.");
    });
}
