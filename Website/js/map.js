var wfsrooturl = "geoserverurl";
var video = document.getElementById("video");

var lineStringsObj;
var pointsObj;
var selectedPointFeatures;
var selectedPolygonFeatures;
var fov_polygons = L.geoJson(null);
var fov_covered_polygons = L.geoJson(null);
var single_fov_covered_polygons = L.geoJson(null);
var fovResultGeoJson = new L.GeoJSON();
var animatedMarker;
var selectedLine;
var geojsonPolygons;
var spatialFilterFov = L.geoJson(null);
var coveredAreasData;
var visible = false;
var coveredAreasDisplayed = false;
var singleFeatureSelection = false;
var singleFovCovSelection = false;
var wfsloaded = false;
var currentselectedname;
var positionIcon;

var clat;
var clon;
var origin_lat;
var origin_lon;

var search_items = [{name:'Closest Video', value:'closest'}, {name:'Newest Video', value:'latest'}];
var searchoption;

var map = L.map('map', {
	attributionControl: false
}).setView([51.968239838026, 7.6086474814909], 14);

var positionIcon = L.circleMarker([0, 0], positionMarkerOptions);



/*Points of Interest Query Dropdown*/
$.each(search_items, function(){
        $("<option />")
        .attr("value", this.value)
        .html(this.name)
        .appendTo("#search");
    });

$("#search").change(function(){
	searchoption = $(this).val();
});


/*Parameters for Ajax requests*/
var defaultParameters = {
    service: 'WFS',
    version: '1.0.0',
    request: 'GetFeature',
    typeName: 'VideoGeotagging:points',
    outputFormat: 'text/javascript',
    format_options: 'callback: getJsonPts'

};

var defaultFovParameters = {
    service: 'WFS',
    version: '1.0.0',
    request: 'GetFeature',
    typeName: 'VideoGeotagging:orientations',
    outputFormat: 'text/javascript',
    format_options: 'callback: getJsonOrientations'

};
var defaultFovCoveredParameters = {
    service: 'WFS',
    version: '1.0.0',
    request: 'GetFeature',
    typeName: 'VideoGeotagging:fovpolygons',
    outputFormat: 'text/javascript',
    format_options: 'callback: getJsonFovCovered'

};

var defaultLinestringParameters = {
    service: 'WFS',
    version: '1.0.0',
    request: 'GetFeature',
    typeName: 'VideoGeotagging:linestrings',
    outputFormat: 'text/javascript',
    format_options: 'callback: getJsonLs'

};

var parameters = L.Util.extend(defaultParameters);
var linestringParameters = L.Util.extend(defaultLinestringParameters);
var fovParameters = L.Util.extend(defaultFovParameters);
var fovCoveredParameters = L.Util.extend(defaultFovCoveredParameters);

/*Feature Styles*/

var fovPolygonOptions = {
	opacity: 0,
	fillColor: "#007F02",
	zIndexOffset: 0,
	fillOpacity: 0.5
};
var coveredAreasStyle = {
	zIndexOffset: -10,
	color: "#BC6100",
	fillOpacity: 0.4
};

var startMarkerOptions = {
    radius: 6,
    fillColor: "#00B6FF",
    zIndexOffset: 6,
    weight: 1,
    opacity: 1,
    fillOpacity: 0.8
};

var geojsonMarkerOptions = {
    radius: 6,
    fillColor: "#AF003D",
    zIndexOffset: 6,
    weight: 1,
    opacity: 1,
    fillOpacity: 0.8
};

var positionMarkerOptions = {
    radius: 7,
    fillColor: "#00FF50",
    color: "#000",
    zIndexOffset: 200,	
	weight: 1,
    opacity: 1,
    fillOpacity: 0.8
};



/*Layers*/
L.control.attribution({position: 'bottomleft'}).addTo(map);


var wms_ortho = L.tileLayer.wms( "http://www.wms.nrw.de/geobasis/wms_nw_dop40", {layers: 'nw_dop40',
    format: 'image/png',
    transparent: true,
	maxZoom: 18
}); 

wms_ortho.addTo(map);
var wms_nw_dgk5 = L.tileLayer.wms( "http://www.wms.nrw.de/geobasis/wms_nw_dgk5", {layers: 'nw_dgk5_grundriss',
    format: 'image/png',
    transparent: true,
	maxZoom: 18
}); 

var osm = new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png');
var ggl = new L.Google();
var ggl2 = new L.Google('TERRAIN');




var baseLayers = {
			"DOP40 NRW": wms_ortho,
			
			"OSM": osm,
			
			"DGK5 NRW": wms_nw_dgk5,

			"Google Aerial": ggl,

			"Google Terrain": ggl2


			};
		
			
L.control.layers(baseLayers).addTo(map);





/*moving the position marker by given latlon*/
function setPositionmarker(lon, lat){
			map.removeLayer(positionIcon);
			positionIcon = L.circleMarker([lat, lon], positionMarkerOptions); 
			map.addLayer(positionIcon, true);
			positionIcon.on('contextmenu', function (e) {
			map.removeLayer(positionIcon);
		});
}


	


var video = document.getElementsByTagName('video')[0];



/*Performed for each selection of a GPS Point*/		
function onEachFeature(feature, layer) {
   
   layer.on('click', function (e) {
		  
   		var positionms = feature.properties.position;
		var position = positionms/1000;
		  
		singleFeatureSelection = true; 
		handleSelection(feature, position);
			});
	layer.on('contextmenu', function (e) {				   
		currentselectedname = feature.properties.name;
		singleCoveredArea(feature.properties.name);
		singleFeatureSelection = true; 
		displaySingleCoveredArea();
	});
}


/*Setting metadata, playback position, Selecting matching featues*/
function handleSelection(feature, position){

		  var arrayid = feature.properties.file_id - 1;
		  var name = feature.properties.name;
		  var date = feature.properties.date;
		  date = removeZ(date);		 
		  var time = feature.properties.time;
		  time = removeZ(time);
		  var location = feature.properties.location;
		  var description = feature.properties.description;
		  
			var bounds = L.geoJson(coveredAreasData,{
		  						filter: function (feature, latlng) {
				
				return feature.properties.name == name;
			}
		  }).getBounds();
		 

		  if (currentselectedname != name){
		  //clearMap();
		  linesLayer.clearLayers();
		  linesLayer = L.geoJson(lineStringsObj,{		
		  		filter: function(feature, layer) {
							
						if (feature.properties.name == name){
							return true;
						}else return false;
			}							  			
		  	}).addTo(map);
		  map.fitBounds(bounds);
		  //map.zoomOut();
		  pointsLayer.clearLayers();
		  pointsLayer = L.geoJson(pointsObj, {
			onEachFeature: onEachFeature,
			filter: function (feature, latlng) {
				
				return feature.properties.name == name;
			},
			pointToLayer: function (feature, latlng) {
				
				if(feature.properties.position == 0){return L.circleMarker(latlng, startMarkerOptions);}else{
				
				return L.circleMarker(latlng, geojsonMarkerOptions);
				}
			}
			}).addTo(map);	
	
		  }

		  currentselectedname = name;
		  singleFovCovSelection = true;
		  var vsource = video.currentSrc;
		  var newsrc = "url" + name + ".mp4";
		  changeVideo(vsource, newsrc, position);
		  setKMLurl(name);  	
		  selectedPointFeatures = jF("*[name="+name+"]",pointsObj).get();
		  setPositionmarker(feature.properties.longitude, feature.properties.latitude);	
		
		  displayMetadata(name, location, date, time, description);
}


/*Eventlistener for video player*/
video.addEventListener("timeupdate", function () {
				moveMarker();
			}); 

video.addEventListener("seeking", function () {
				moveMarker();
			}); 

var pointsLayer;			
var linesLayer = L.geoJson();

$(document).ready(function()
{
	displayAllRecordings(); 

});		


/*Showing all available GPS tracks/Video*/
function displayAllRecordings(){

function lines(){	
			
			return	$.ajax({
					url: wfsrooturl + L.Util.getParamString(defaultLinestringParameters),
					
					dataType: 'jsonp',
					jsonpCallback: 'getJsonLs',
					
					success: function (lines_data){}
				});
			}

 
function points(){
			return 	$.ajax({
					url: wfsrooturl + L.Util.getParamString(parameters),
					
					dataType: 'jsonp',
					jsonpCallback: 'getJsonPts',
					
					success: function(points_data){}
				});
			}

function orientations(){
			return 	$.ajax({
					url: wfsrooturl + L.Util.getParamString(fovParameters),
					
					dataType: 'jsonp',
					jsonpCallback: 'getJsonOrientations',
					
					success: function(fov_data){}
				});
			}			

function fov_covered(){
			return 	$.ajax({
					url: wfsrooturl + L.Util.getParamString(fovCoveredParameters),
					
					dataType: 'jsonp',
					jsonpCallback: 'getJsonFovCovered',
					
					success: function(fov_cov_data){}
				});
			}			

		
$.when(lines(), points(), orientations(), fov_covered()).done(function(lines_data, points_data, fov_data, fov_cov_data) {
		
		if (wfsloaded == true){
				clearMap();
				fov_polygons = L.geoJson(null);
				fov_covered_polygons = L.geoJson(null);
				linesLayer = L.geoJson(null);
			}
		//setting Features from data	
		lineStringsObj = lines_data[0];
		linesLayer.addData(lineStringsObj);
		linesLayer.addTo(map);
		pointsObj = points_data[0];
		coveredAreasData = fov_cov_data[0];
		fov_polygons = fov_polygons.addData(coveredAreasData);
		geojsonPolygons = (fov_data[0]);
		pointsLayer = L.geoJson(points_data[0], {
			onEachFeature: onEachFeature,
			
			pointToLayer: function (feature, latlng) {
				
				if(feature.properties.position == 0){return L.circleMarker(latlng, startMarkerOptions);}else{
				
				return L.circleMarker(latlng, geojsonMarkerOptions);
				}
			}
		}).addTo(map);
		
		wfsloaded = true;
});
}



/*Displaying a Polygon that covers the approx. area that is covered by the videos*/
function displayCoveredAreas(){
		singleCoveredArea("none");

		if (coveredAreasDisplayed == false){
					fov_covered_polygons = L.geoJson(coveredAreasData, {style: coveredAreasStyle, onEachFeature: coveredAreasClick}); fov_covered_polygons.addTo(map, true);
				
					
					fov_covered_polygons.bringToBack();
					coveredAreasDisplayed = true;
			}else{
					fov_covered_polygons.clearLayers();
					coveredAreasDisplayed = false;
					
				}
			
	}

/*Displaying a Polygon that covers the approx. area that is covered the selected the Video*/
function singleCoveredArea(name){
		
				map.removeLayer(single_fov_covered_polygons);
				single_fov_covered_polygons = L.geoJson(coveredAreasData, {style: coveredAreasStyle, filter: function (feature, layer) {
				
				return feature.properties.name == name;
				}, 
				onEachFeature: coveredAreasClick}); 	
			
	}


/*Setting/Removing single covered areas*/
function displaySingleCoveredArea(){
	if (singleFovCovSelection == false){
		single_fov_covered_polygons.addTo(map);
		single_fov_covered_polygons.bringToBack();
		singleFovCovSelection = true;
	}else{
		
		singleFovCovSelection = false;
	}		

}


/*performing video search*/
function coveredAreasClick(feature, layer){
	layer.on('click', function(e){
		if (singleFovCovSelection == false){
		displayCoveredAreas();}
		searchVideo(e);
		
		
	});
}	

/*Displaying only the Starting Points of a Track*/
function displayStartPoints(){
			


			if(singleFeatureSelection == false){
			clearMap();
			
			pointsLayer = L.geoJson(pointsObj, {
			onEachFeature: onEachFeature,
			
			filter: function (feature, latlng) {
				
				return feature.properties.position == 0;
			},
			pointToLayer: function (feature, latlng) {
				return L.circleMarker(latlng, startMarkerOptions);
			}

		});
		pointsLayer.setStyle(startMarkerOptions);
		pointsLayer.addTo(map);
		singleFeatureSelection = true;
	}else{
			clearMap();
			pointsLayer = L.geoJson(pointsObj, {
			onEachFeature: onEachFeature,
			
			filter: function (feature, latlng) {
				
				return feature.properties.position == 0;
			},
			pointToLayer: function (feature, latlng) {
				return L.circleMarker(latlng, startMarkerOptions);
			}

		}).addTo(map);
			singleFeatureSelection = false;
		
	}	
}


/*Removing all Layers from Mapview*/
function clearMap(){
	
	try{
		map.removeLayer(positionIcon);
	}catch (e) {
    //
    }
	video.pause();
	linesLayer.clearLayers();
	pointsLayer.clearLayers();
	
	singleFeatureSelection = false;
	singleCoveredArea("none");
	coveredAreasDisplayed = true; //fake
	displayCoveredAreas();
	fov_polygons.clearLayers();
}

/*Moving the GPS Position Marker in dependence of the Video-Playback
Setting the matching fov polygon
*/	
function moveMarker(){
	for(var i = selectedPointFeatures.length-1; i >= 0; i--)
					
		{
		  if((selectedPointFeatures[i].position)/1000 <= video.currentTime)
		  {
			
			setPositionmarker(selectedPointFeatures[i].longitude, selectedPointFeatures[i].latitude);
			document.getElementById('heading').innerHTML = selectedPointFeatures[i].heading+'°';
			
			fov_polygons = L.geoJson(geojsonPolygons, 
				{
					onEachFeature: coveredAreasClick,
					style: fovPolygonOptions,
					filter: function(feature, layer) {
						fov_polygons.clearLayers();	
						if (feature.properties.name == selectedPointFeatures[i].name && feature.properties.position == selectedPointFeatures[i].position){ return true;} else{
						
						return false;}
					}
				});
			
			fov_polygons.addTo(map);
			fov_polygons.bringToBack();
			return;
		  }
		}
		document.getElementById('heading').innerHTML = position;
		fov_polygons.on('contextmenu', searchVideo);
}


/*Filtering Videos by clicking at the Map */

function searchVideo(e) {
			
	clat =  e.latlng.lat;
	clon = e.latlng.lng;
	singleCoveredArea("none");
	singleFeatureSelection = true;			

	//clearMap();
			


	function handleJson(data) {
  
		var origins = data.features;
		var length = origins.length;



		var origin = null;
		var closest_point_id = 0;
		var lowest_distance = 100000000000000000;
		var latest; 
		//checking for available sequence.
		if (length == 0){
			alert("No matching Video available");
			return;
		}

		//finding latest video
		for (var i = 0; i<length; i++){
			latest = origins[i].properties.name;
		}

		//searching closest one
		for (var i = 0; i<length; i++){
		
			//	
			if(searchoption == 'latest'){
				if(latest != origins[i].properties.name){
					continue;
				}
			}

			if(singleFeatureSelection == true){
				if(currentselectedname != origins[i].properties.name){
					continue;
				}
		}	

		origin_lat = origins[i].properties.origin.coordinates[1];
		origin_lon = origins[i].properties.origin.coordinates[0];
		origin = new L.latLng(origin_lat, origin_lon);
		current_distance = e.latlng.distanceTo(origin);

		for (var a = 0; a<length; a++){
			if(current_distance <= lowest_distance){
				lowest_distance = current_distance;
				closest_point_id = i;
			}
		}

		}
		currentselectedname = "";
		var name = origins[closest_point_id].properties.name;
		var position = (origins[closest_point_id].properties.position)/1000;

		handleSelection(origins[closest_point_id], position);
		video.pause();
		video.play();
		video.pause();
}

	/*Spatial Query*/
	$.ajax({
	  url : "url:8080/geoserver/VideoGeotagging/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=VideoGeotagging%3Asearch&outputFormat=text/javascript&format_options=callback:getJson&cql_filter=contains(geom,%20POINT("+clon+"%20"+clat+"))",
	  dataType : 'jsonp',
	  jsonpCallback: 'getJson',
	  success: handleJson
	});	
}
map.on('contextmenu', searchVideo);


