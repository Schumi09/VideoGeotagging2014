<?php
include 'orientation.php';

// Where the file is going to be placed 
$target_path = "uploads/";

/* Add the original filename to our target path.  
Result is "uploads/filename.extension" */
if(isset($_FILES['uploadedfile'])){
$target_path = $target_path . basename( $_FILES['uploadedfile']['name']);


if(move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $target_path)) {
    echo "The file ".  basename( $_FILES['uploadedfile']['name']). 
    " has been uploaded";
   chmod ("uploads/".basename( $_FILES['uploadedfile']['name']), 0644);
   storeData("uploads/".basename( $_FILES['uploadedfile']['name']));
} else{
    echo "There was an error uploading the file, please try again!";
   echo "filename: " .  basename( $_FILES['uploadedfile']['name']);
   echo "target_path: " .$target_path;
   
}
}

function storeData($file){
		$kml = simplexml_load_file($file);
		$placemarks = $kml->Document->Placemark;
		$index = 0;
		
		// Verbindungsaufbau und Auswahl der Datenbank
		$dbconn = pg_connect("host=localhost dbname=postgres user=name password=pw")
		or die('Verbindungsaufbau fehlgeschlagen: ' . pg_last_error());

		 $docname = $kml->Document->name;
		
		 
		 foreach ($placemarks as $value){
			$extendeddata = $value->ExtendedData->xpath("//SchemaData[@schemaUrl='#VideoGeotagging']");
			if(kml_child_exists($value, 'Point')){
			
			$name = $value->name;
			$position		= get_extended_data_element($value, 'Position')[$index];
			$lon		= get_extended_data_element($value, 'Lon')[$index];	
			$lat		= get_extended_data_element($value, 'Lat')[$index];	
			$time 		= get_extended_data_element($value, 'Time')[$index];
			$date 		= get_extended_data_element($value, 'Date')[$index];
			$heading 	= get_extended_data_element($value, 'Heading')[$index];
			$description = $kml->Document->description;
			$address = $kml->Document->address;
			
			
			$orientation_polygon = calculateOrientationPolygon($lat, $lon, $heading);
			
			
			$pointsquery = pg_query($dbconn, "INSERT INTO pointstest VALUES (DEFAULT, '".$name."', '".$docname."', '".$description."', '".$address."', '".$position."', '".$lat."', '".$lon."', '".$heading."', '".$time."', '".$date."', ST_GeometryFromText ( 'POINT ( ".$lon." ".$lat." )', -1 ))") or die ('Inserting Points Failed: ' . pg_last_error());
			$orientationquery = pg_query($dbconn, "INSERT INTO orientations VALUES (DEFAULT, '".$name."', '".$docname."', '".$position."', ST_GeometryFromText ( 'POLYGON (( ".$orientation_polygon." ))', -1 ))") or die ('Inserting Orientation Failed: ' . pg_last_error());

			$index++;
			} else{
				$name = $value->name;
			
				$linestring =  $value->LineString->coordinates;
				$kmlls		=  "<LineString><coordinates>".$linestring."</coordinates></LineString>";
				$lsquery 	=  "INSERT INTO linestrings VALUES (DEFAULT, '".$docname."' , ST_GeomFromKML('".$kmlls."'))";
				$insertls 	= pg_query($dbconn, $lsquery) or die('Inserting Linestring Table Failed: ' . pg_last_error());
			}
		}	
	}
function kml_child_exists($kml, $childpath)
		{
		$result = $kml->xpath($childpath); 
		return (bool) (count($result));
		}
	
function get_extended_data_element($value, $element)
		{
		$extendeddata = $value->ExtendedData->xpath("//SchemaData[@schemaUrl='#VideoGeotagging']");
		$object = $extendeddata[0]->xpath("//SimpleData[@name='".$element."']");
		return $object;
		}
?>