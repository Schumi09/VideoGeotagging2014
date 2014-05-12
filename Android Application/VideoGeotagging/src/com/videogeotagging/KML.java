package com.videogeotagging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

public class KML {
	
	private static String kml_schema = 	"<Schema name=\"VideoGeotagging\" id=\"VideoGeotagging\">\n" +
										"<SimpleField name=\"Position\" type=\"int\" />\n" +
										"<SimpleField name=\"Date\" type=\"string\" />\n" +
										"<SimpleField name=\"Time\" type=\"string\" />\n" +
										"<SimpleField name=\"Lat\" type=\"double\" />\n" +
										"<SimpleField name=\"Lon\" type=\"double\" />\n" +
										"<SimpleField name=\"Accuracy\" type=\"double\" />\n" +
										"<SimpleField name=\"Speed\" type=\"double\" />\n" +
										"<SimpleField name=\"Heading\" type=\"double\" />\n" +
										"</Schema>\n";
	private static String kml_bottom = "</Document>\n</kml>";

	private static File sdcard = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES +"/VideoGeotagging/");

	static String line = "";
	private static int id = 1;
	
    
	/*Creating KML-File with Header*/
	public static void createFile(String filename, String description, String location){
		String kml_head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" + "<kml>" +  "\n" + "<Document>\n<name>"+filename+"</name>\n<address>"+location+"</address>\n<description>"+description+"</description>\n";
		String fn = filename + ".kml";
		File kml = new File(sdcard ,fn);
		Log.d("File Creation Methoded called at", String.valueOf(System.currentTimeMillis()));
		try {
			kml.createNewFile();
			FileOutputStream fOut = new FileOutputStream(kml, true);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(kml_head);
			myOutWriter.append(kml_schema);
			myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("Created File at", String.valueOf(System.currentTimeMillis()));
		
	}
	
	/*Writing Sensor values to KML's body*/
	public static void write_body(String filename, Location location, long videostart) throws IOException{
		String fn = filename + ".kml";
		File kml = new File(sdcard ,fn);

		double lat = location.getLatitude();
		double lon = location.getLongitude();
		float heading = location.getBearing();
		long ltime = location.getTime();
		float accuracy = location.getAccuracy();
		float speed = location.getSpeed();
		
		
		
		Calendar cal = Calendar.getInstance();
		
		long position = ltime - videostart;
		if (position <= 30){
			position = 0;
			cal.setTimeInMillis(videostart);
		} else{
			cal.setTimeInMillis(ltime);
		}
		
		//Filling extended data scheme
		String content = "<Placemark id=\"" + id + "\">\n"+
				"<name>"+id+"</name>\n"+
				get_point_string(lat, lon)+
				"<ExtendedData>\n	<SchemaData schemaUrl=\"#VideoGeotagging\">\n"+
				get_content_string("Position", String.valueOf(position))+
				get_content_string("Date", Store.getDateString(ltime))+
				get_content_string("Time", Store.getTimeString(ltime))+
				get_content_string("Lat", String.valueOf(lat))+
				get_content_string("Lon", String.valueOf(lon))+
				get_content_string("Accuracy", String.valueOf(accuracy))+
				get_content_string("Speed", String.valueOf(speed))+
				get_content_string("Heading", String.valueOf(heading))+
				"	</SchemaData>\n</ExtendedData>\n</Placemark>\n";
		
		FileOutputStream fOut;
		id++;
		try {
			fOut = new FileOutputStream(kml, true);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(content);
			myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		extendLineString(lat, lon);
	}
	
	/*Closing KML
	 * Inserting LineString*/
	public static void write_bottom(String filename) throws IOException{
		String fn = filename + ".kml";
		File kml = new File(sdcard ,fn);
		FileOutputStream fOut;
		String ls = "<Placemark id=\"0\">\n<name>"+filename+"</name>\n" + "<LineString>\n<coordinates>" + line + "</coordinates>\n</LineString>\n</Placemark>\n";
		try {
			fOut = new FileOutputStream(kml, true);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(ls);
			myOutWriter.append(kml_bottom);
			myOutWriter.flush();
			myOutWriter.close();

			line = "";
			fOut.flush();
			fOut.close();
			
			fOut = null;
			System.gc();
			id = 1;
		} catch (IOException e) {  
			
			e.printStackTrace();
		}
		
	}
	
	/*Point String for KML File*/
	public static String get_point_string(double lat, double lon){
		String coordinates = "<Point>\n<coordinates>" + lon + "," + lat + "</coordinates>\n</Point>\n";
		return coordinates;
	}
	
	/*Extending LineString for KML File*/
	public static void extendLineString(double lat, double lon){
		
		line = line  + lon + "," + lat + " ";
		
		
	}
	
	/*Template for SimpleData Element within Extended Data Scheme*/
	public static String get_content_string(String type, String value){
		String content_string = "   		<SimpleData name=\"" + type + "\">" + value + "</SimpleData>\n";
		return content_string;
	}
	
	
}
