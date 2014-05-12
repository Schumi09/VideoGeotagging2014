
function replaceSeperator(rpl){
	return rpl.replace(".", ":");
}

function displayMetadata(name, location, date, time, description){
	
	document.getElementById('name').innerHTML = name;
	document.getElementById('location').innerHTML = location;
	document.getElementById('date').innerHTML = date;
	document.getElementById('time').innerHTML = time  + " h";
	document.getElementById('description').innerHTML = description;

	}
	
function removeZ(string){
	string = string.substring(0, string.length - 1);
	return string;
}	

function setKMLurl(name){
	document.getElementById("kml").href = "url/php/uploads/" + name + ".kml";
}

	
