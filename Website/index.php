<html>
<head>
	<title>VideoGeotagging</title>
	<meta charset="utf-8" />
	<link rel="stylesheet" type="text/css" href="css/style.css">
	<link rel="stylesheet" href="js/leaflet.css" />
	<link rel="stylesheet" href="css/leaflet.contextmenu.css"/>
	<link rel="stylesheet" href="css/l.geosearch.css" />
</head>
<body>
	<div id="header">
	<h1 id="title">VideoGeotagging</h1>
	</div>
	<div id="content">
		<div id="map">
			
		</div>
		
		<div id="metadata">
		
		<table id="tabledata">
			<tr>
			<td><b><font size="+2">Information</font></b></td><td align="right"></td>
			</tr>
			<tr>
				<td><b>Location:</b></td><td id="location"align="left"><td>
			</tr>
			<tr>
				<td><b>Name:</b></td><td id="name"align="left"align="left"><td>
			</tr>
			<tr>
				<td><b>Heading:</b></td><td id="heading"align="left"align="left"align="left"><td>
			</tr>
			<tr>
				<td><b>Date:</b></td><td id="date"align="left"align="left"align="left"align="left"><td>
			</tr>
			<tr>
				<td><b>Time:</b></td><td id="time"align="left"align="left"align="left"align="left"align="left"><td>
			</tr>
			<tr>
				<td valign="top"><b>Description:</b></td><td id="description" align="left"><td>
			</tr>
		</table>
		<video id="video" controls muted></video>
		</div>
		
		<div id="footer">
		
		<button type="button" align="bottom" onclick = 'displayAllRecordings()' id="refresh">Refresh Data</button>	
		<button type="button" align="bottom" onclick = 'displayCoveredAreas()' id="covered">Approx. covered areas</button>
		<button type="button" align="bottom" onclick = 'displayStartPoints()' id="startp">Start Points</button>
		<select id="search" name="Search method" value="Search Option"></select>
		<a id='kml' href="#" >Download KML</a>
		
	</div>
	
	</div>
	
	
		
	

</body>
	<!--Jquery & Leaflet Scripts-->
	
	<script src="js/leaflet.js"></script>
	<script src="http://code.jquery.com/jquery-1.9.0.js"></script>
	<script src="js/videoplayer.js"></script>
	<script src="http://maps.google.com/maps/api/js?v=3&sensor=false"></script>
	<script src="js/Google.js"></script>
	<script src="js/jfunk-0.0.1.js"></script>
	
	
	<!--Scripts-->
	
	<script src="js/map.js"></script>
	
	<script src="js/metadata.js"></script>

<script></script>
</html>
