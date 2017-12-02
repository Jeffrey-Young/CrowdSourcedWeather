<?php
/*	Author: Connor Hamlet.
*	Description:
*   This php script is used for adding weather items into the database. 
*   Input type: JSON. Structure:   long: decimal, lat: decimal, 
*	weather: description < 15 chars
*
*      Here is the curl command for testing purposes. 
*
*	 curl -H "Content-Type: application/json" -X 
*	POST{"long":123.234,"lat":34.5432, "weather":"rain"}' 
*	https://weatherclone.hopto.org/addWeather.php
*	
*	Return type: JSON. 
*	Returns:   a json with:  "received": 0 if there is an error, 
*   and "received": 1 is the insertion completed successfully. 
*/
//Decodes JSON
$_POST = json_decode(file_get_contents('php://input'), true);
//Expecting:
//long: Decimal
//lat: Decimal
//weather: string (rain, sun, storm)

$returndata = array(
    'received'      => 0
);
//If any of the fields are empty, return. 
if(empty($_POST["long"]) ||empty($_POST["lat"]) || empty($_POST["weather"])){
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
$stmt = mysqli_prepare($con, "INSERT INTO `weatherItems`(".
		" `latitude`, `longitude`, `weather`, `time`)".
		" VALUES (?,?,?,UNIX_TIMESTAMP())");
mysqli_stmt_bind_param($stmt, 'dds', $lat, $long, $weather);
$lat = $_POST["lat"];
$long = $_POST["long"];
$weather = $_POST["weather"];


if(mysqli_stmt_execute($stmt)){
        $returndata["received"] = 1;
        $json = json_encode($returndata);
        echo $json;
        return;
}
/* bind result variables */
mysqli_stmt_close($stmt);
mysqli_close($con);
