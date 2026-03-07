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

    .then(res => res.text())
    .then(data => {

        if(data === "Login Successful"){

            localStorage.setItem("username", username);

            window.location.href = "index.html";
        }
        else{
            alert("Invalid Username or Password");
        }

    });

}