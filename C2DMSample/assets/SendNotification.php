<?php

// Registration key of the device you want to send a notification to
$userKey = $_POST["UserGoogleKey"];

$gServerKey = "";
$gLoginKeyAttempts = 0;
$gPostError = "";

if( $userKey ) {
	
	// First your server will need a Google Login key for the account you registered for c2dm with
	// The first time you will need to make a post request to google's servers to get this.
	// Then that value should be stored on your server and re-used to push future messages
	GetGoogleKey("dummy_server", "dummy_username", "dummy_password", "dummy_database");
	
	$message = "Message you want to send to the device";
	$collapseKey = "something_simple"; // this value can be used when multiple messages are sent to a device while it's offline, as only 1 will get sent when it wakes up with all the others under the collapse key
	
	$messageID = do_post_request("https://android.apis.google.com/c2dm/send", "registration_id=" . $userKey . "&collapse_key=$collapseKey&data.notification=$message", "Authorization: GoogleLogin auth=" . $gServerKey);

	if( strlen($gPostError) > 0 ) {
		// Check to see if your Server key is old and needs to be updated
		if( strpos($gPostError, "401") > -1 ) {
			echo "Invalid Google Login Key";
			UpdateGoogleKey($server, $username, $password, $database);
			$messageID = do_post_request("https://android.apis.google.com/c2dm/send", "registration_id=" . $user->GoogleKey . "&collapse_key=$collapseKey&data.notification=$message", "Authorization: GoogleLogin auth=" . $gServerKey);
			echo "Second Attempt: " . $messageID . "<br/>";
			}
		}
	}


//*****************************************************
// GetGoogleKey
//*****************************************************
function GetGoogleKey($server, $username, $password, $database) {
	try {
		global $gServerKey;
		// I store my server key in a MySQL database, so the first thing I try is to retrieve that value
		// If no rows come back, that means I need to fetch one for the first time
		$mysqli_key = new mysqli($server, $username, $password, $database);
		$key = $mysqli_key->query( "CALL GetGoogleLoginKey()" );
		if( $mysqli_key->affected_rows == 0 ) {
			UpdateGoogleKey($server, $username, $password, $database);
			}
		else {
			$gServerKey = $key->fetch_object()->LoginKey;
			}
		}
	catch( Exception $error ) {
		echo 'GetGoogleKey: ' .  $error->getMessage() . "\n";
		}

	}


//**************************************************
// UpdateGoogleKey
//**************************************************
function UpdateGoogleKey($server, $username, $password, $database) {
	global $gLoginKeyAttempts;
	global $gServerKey;
	try {
		$email = "<YOUR_EMAIL>";
		$password = "<YOUR_PASSWORD>";
		if( $gLoginKeyAttempts <= 3 ) {
			$gServerKey = do_post_request("https://www.google.com/accounts/ClientLogin", "accountType=HOSTED_OR_GOOGLE&Email=$email&Passwd=$password&service=ac2dm&source=teleknEsis-C2DMSample-1.0");
			if( $gServerKey && strlen($gServerKey) > 0 ) {
				$startIndex = strpos($gServerKey,"Auth=");
				$gServerKey = substr($gServerKey, $startIndex + 5);
				// Now I save the server key to a MySQL db
				$mysqli = new mysqli($server, $username, $password, $database);
				$result = $mysqli->query( "CALL UpdateGoogleLoginKey( '$gServerKey' )" );
				}
			else {
				// retry
				$gLoginKeyAttempts += 1;
				UpdateGoogleKey($server, $username, $password, $database);
				}
			}
		else {
			echo "ERROR UPDATING GOOGLE LOGIN KEY";
			}
		}
	catch( Exception $error ) {
		echo "UpdateGoogleKey: " . $error->getMessage() . "\n";
		}
	}


//-----------------------------------------------
// do_post_request
//-----------------------------------------------
function do_post_request($url, $data, $optional_headers = null) {
	global $gPostError;
	$params = array('http' => array(
			  'method' => 'POST',
			  'content' => $data
			));
	if ($optional_headers !== null) {
		$params['http']['header'] = $optional_headers;
		}
	$ctx = stream_context_create($params);
	$fp = fopen($url, 'rb', false, $ctx);
	if( $fp ) {
		$response = stream_get_contents($fp);
		if ($response === false) {
			throw new Exception("Problem reading data from $url, $php_errormsg");
			}
		$gPostError = "";
		return $response;
		}
	else {
		$gPostError = $php_errormsg;
		}
	}


?>