<?php
	
	
	function calculateOrientationPolygon($lat, $lon, $theta){
			$lat = (string)$lat;
			$lat = doubleval($lat);
			$lon = (string)$lon;
			$lon = doubleval($lon);
			
			$theta = (string)$theta;
			$theta =  doubleval($theta);
			
			
			
			$distance = 0.04; //km
			$fov_angle = 60; //degrees
			$angle_clockwise = $theta + ($fov_angle*0.5);
			$angle_counterclockwise = $theta - ($fov_angle*0.5);
			$point_origin = getStartPoint($lat, $lon);
			$point_heading = getFinalPoint($lat, $lon, $distance, $theta);
			$point_clockwise = getFinalPoint($lat, $lon, $distance, $angle_clockwise);
			$point_counterclockwise = getFinalPoint($lat, $lon, $distance, $angle_counterclockwise);
			
			$query = $point_origin.",".$point_clockwise.",".$point_heading.",".$point_counterclockwise.",".$point_origin;
			
					return $query;
		}
	

	
	function getFinalPoint($start_lat, $start_lon, $distance, $theta){
			$radius = 6371;
			$lat1 = toRad($start_lat);
			$lon1 = toRad($start_lon);
			$distance = $distance/6371.01; //Earth's radius in km
			$theta = toRad($theta);
			
			//explanation http://www.movable-type.co.uk/scripts/latlong.html
			$lat2 = asin( sin($lat1)*cos($distance) +
                  cos($lat1)*sin($distance)*cos($theta) );
			$lon2 = $lon1 + atan2(sin($theta)*sin($distance)*cos($lat1),
                          cos($distance)-sin($lat1)*sin($lat2));
			$lon2 = fmod(($lon2+3*pi()),(2*pi())) - pi();
			
			$lat2 = toDeg($lat2);
			$lon2 = toDeg($lon2);	
			
			$point_text = $lon2." ".$lat2;
		
				return $point_text;
	}
	
	function toRad($deg){
				return $deg * pi() / 180;
	}
	
	function toDeg($rad){
				return $rad * 180 / pi();
	}
	
	function getStartPoint($start_lat, $start_lon){
				$point_text = $start_lon." ".$start_lat;
				
				return $point_text;
	}
	
	
	
?>