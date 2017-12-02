<?php

header('Content-Type: application/json');
//Decodes JSON
$_POST = json_decode(file_get_contents('php://input'), true);

$returndata = array(
    'isAuthenticated'      => 0
);

if(empty($_POST["username"]) || empty($_POST["password"])){
	$json = json_encode($returndata);
	echo $json;
	return;
}
//Connects to my sql database. 
$con = @mysqli_connect('localhost', 'weathersqluser', 'bCifml0yPaqZFJQe', 'androidfinal');
if (!$con) {
    echo "Error: " . mysqli_connect_error();
	exit();
}
$stmt = mysqli_prepare($con, "SELECT * FROM users WHERE username=? AND password=?");
mysqli_stmt_bind_param($stmt, 'ss', $user, $pass);
$user = $_POST["username"];
$pass = $_POST["password"];

mysqli_stmt_execute($stmt);
/* bind result variables */
mysqli_stmt_bind_result($stmt, $id, $name, $password);
mysqli_stmt_store_result($stmt);
if (mysqli_stmt_num_rows($stmt) < 1){
        $json = json_encode($returndata);
        echo $json;
        return;
}
/* fetch values */
    while (mysqli_stmt_fetch($stmt)) {
	$returndata["isAuthenticated"] = 1;
        $json = json_encode($returndata);
        echo $json;
    }

mysqli_stmt_close($stmt);
mysqli_close($con);
