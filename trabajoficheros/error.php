<?php
	$error = $_POST["error"];

	$errorFile = fopen("errors.txt", "a");
	fwrite($errorFile, $error."\n");
	fclose($errorFile);

	echo "Error subido con exito";
?>