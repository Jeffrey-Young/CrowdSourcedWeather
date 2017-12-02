<?php
/*	Author: Connor Hamlet.
*	Description:
*   This php script is used for registering accounts

*/
header('Content-Type: application/json');
//Decodes JSON
$_POST = json_decode(file_get_contents('php://input'), true);

$returndata = array(
    'received'      => 0
);
//If any of the fields are empty, return. 

if(!isset($_POST["username"]) ||!isset($_POST["password"])){
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
//Preparing statement, prevents sql injection. This will add
//the weather item to table. 
$stmt = mysqli_prepare($con, "INSERT INTO `users`(".
		" `username`, `password`)".
		" VALUES (?,?)");
mysqli_stmt_bind_param($stmt, 'ss', $user, $pass);
$user = $_POST["username"];
$pass = $_POST["password"];

if(mysqli_stmt_execute($stmt)){
        $returndata["received"] = 1;
}

$json = json_encode($returndata);
echo $json;
/* bind result variables */
mysqli_stmt_close($stmt);
mysqli_close($con);

