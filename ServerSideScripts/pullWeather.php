<?php
/*
*   Author: Connor Hamlet
*   Summary: This file will return a json which holds all of the weather events in our table. 
*   Return type: JSON
*   Return format:    [{"lat":"34.54320000000000","long":"123.23400000000000","weather":"rain","time":1512200033}]
*    [{
*    "lat": "34.54320000000000",
*    "long": "123.23400000000000",
*    "raining": 0,
*    "jacket": 0,
*    "emergency": 0,
*    "time": 1512200033
*}, {
*    "lat": "38.54321222343240000",
*    "long": "84.2344672873100",
*    "raining": 0,
*    "jacket": 0,
*    "emergency": 0,
*    "time": 1512204631
*}]
*   The resultant json is a list (like []) of weather based events. 
*   A weather based event has a lat, long, and time which is in 
*   unix time. There is also a "weather" field to describe the weather. Examples include: "rainy", "sunny", ect. 
* 
*   How to access: To retrieve this json data, perform a GET request on this PHP script
*/


//Let's Java know we are using a JSON type format. 
header('Content-Type: application/json');
$return_data = array();

//Connects to my sql database. 
$con = @mysqli_connect('localhost', 'weathersqluser', 'bCifml0yPaqZFJQe', 'androidfinal');
if (!$con) {
    echo "Error: " . mysqli_connect_error();
	exit();
}
$stmt = mysqli_prepare($con, "SELECT * FROM weatherItems;");
//binding parameters not needed. mysqli_stmt_bind_param($stmt);
mysqli_stmt_execute($stmt);

/* bind result variables */
mysqli_stmt_bind_result($stmt, $itemid, $latitude, $longitude, $raining, $jacket, $emergency, $time);

mysqli_stmt_store_result($stmt);
if (mysqli_stmt_num_rows($stmt) < 1){
        $json = json_encode("{error: 1}");
        echo $json;
        return;
}
/* fetch values */
    while (mysqli_stmt_fetch($stmt)) {
	$return_item = array(
   	 'lat'      => $latitude,
   	 'long'  => $longitude,
   	 'raining' => $raining,
     'jacket' => $jacket,
     'emergency' => $emergency,
   	 'time' => $time
	);
	//saving data in returned JSON object...
	array_push($return_data, $return_item);
    }
$json = json_encode($return_data);
//Sends JSON over the wire. 
echo $json;

//Cleans it up.
mysqli_stmt_close($stmt);
mysqli_close($con);
return;

