package com.videogeotagging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
/*Helper functions for files/names*/
public class Store {
		
	private static File sdcard = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES +"/VideoGeotagging");
	private static boolean deleted = false;
	static Context context;
	static String name = "";
	
	 /*returning Timestamp for Filenames*/
	 @SuppressLint("SimpleDateFormat")    
	 public static String getFileNameTimestamp(long time)
	    {
		

		// Timestamp format 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

		// Current timestamp
		String timestamp = sdf.format(time);

		return timestamp;
	    }
	 
	 	/*Returns date as dd/MM/yyyy*/
	 	public static String getDateString(long time)
	    {
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String date = sdf.format(time);

		return date;
	    }
	 	
	 	
	 	/*Returns time as HH:mm:ss*/
	 	public static String getTimeString(long time)
	    {
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String date = sdf.format(time);

		return date;
	    }
	 	
	 	/*returns current time*/
	 	public static Date getTime(){
	 		Calendar cal = Calendar.getInstance();
	 		Date time = cal.getTime();
	 		return time;
	 		
	 	}
	 	
	 	/*Starts Upload Task with Filelist*/
	 	public static void uploadAll(){
	 		String[] filelist = Store.getFileList();
 			UploadTask upload = new UploadTask(filelist);
 			upload.execute(filelist);
	 		
	 	}
	 	
	 	
	 	/*Getting Filelist*/
	 	public static String[] getFileList(){
	 		File file[] = sdcard.listFiles();
	 		int length = file.length;
	 		String names[] = new String[length];
	 		Log.d("Files", "Size: "+ length);
	 		for (int i=0; i < length; i++)
	 		{
	 		   names[i] = file[i].getName().toString();
	 		   Log.d("Name:", names[i]);
	 		}
	 		
	 		return names;
	 	}
	 	
	 	/*Check File Type*/
	 	public static boolean isKML(String file){
	 		if (file.endsWith(".kml")){
	 			return true;
	 		}else return false;
	 			
	 	}
	 	
	 	/*Check File Type*/
	 	public static boolean isMP4(String file){
	 		if (file.endsWith(".mp4")){
	 			return true;
	 		}else return false;
	 			
	 	}
	 	
	 	
	 	/*Deleting file with given name*/
	 	public static boolean  deleteFile(String filename){
	 		File file = new File(sdcard.getPath() +File.separator+ filename);
	 		file.setWritable(true);
 			deleted = file.delete();
 			return deleted;
	 	}
	 	
	 	/*Deleting all files*/
	 	public static  void deleteAll(String[] files){
	 		Log.d("Files to delete", files.length+"");
	 		int i = 0;
	 		int length = files.length;
	 		int stop = files.length;
	 		
	 		while (length >= 0 && i < stop){
	 			
		 		boolean deleted = deleteFile(files[i]);
		 		if(deleted == true){
			 		i++;
			 		length--;
			 	}
	 				
	 		}
	 		
	 	}
}
