<?php
/*	Author: Connor Hamlet.
*	Description:
*   This php script is used for adding weather items into the database. 
*   Input type: JSON. Structure:   long: decimal, lat: decimal, 
*	raining: 0 or 1, jacket: 0 or 1, emergency: 0 or 1
*
*      Here is the curl command for testing purposes. 
*
*	 curl -H "Content-Type: application/json" -X 
*	POST{"long":123.234,"lat":34.5432, "raining":0, "jacket":0, "emergency":0}' 
*	https://weatherclone.hopto.org/addWeather.php
*	
*	Return type: JSON. 
*	Returns:   a json with:  "received": 0 if there is an error, 
*   and "received": 1 is the insertion completed successfully. 
*/
header('Content-Type: application/json');
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

if(!isset($_POST["long"]) ||!isset($_POST["lat"]) || 
	!isset($_POST["raining"]) || 
	!isset($_POST["jacket"]) || 
	!isset($_POST["emergency"])){

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
		" `latitude`, `longitude`, `raining`, `jacket`, `emergency`, `time`)".
		" VALUES (?,?,?,?,?,UNIX_TIMESTAMP())");
mysqli_stmt_bind_param($stmt, 'ddiii', $lat, $long, $raining, $jacket, $emergency);
$lat = $_POST["lat"];
$long = $_POST["long"];
$raining = $_POST["raining"];
$jacket = $_POST["jacket"];
$emergency = $_POST["emergency"];

if(mysqli_stmt_execute($stmt)){
        $returndata["received"] = 1;
}
$json = json_encode($returndata);
echo $json;
/* bind result variables */
mysqli_stmt_close($stmt);
mysqli_close($con);

