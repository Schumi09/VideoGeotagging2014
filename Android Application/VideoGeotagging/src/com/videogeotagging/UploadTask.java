package com.videogeotagging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

/*Uploading given File List*/
public class UploadTask extends AsyncTask<String, Void, String> {
	
	String fpath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES +"/VideoGeotagging").getPath();
	private String[] names;
	int current=0;
	
	
	public UploadTask(String[] names){
		super();
		this.names = names;
	}
	
	//Uploading KMLs and MP4s
	@Override
	protected String doInBackground(String... aname) {
		int rows = aname.length;
 		
		while(current < rows)
        {
			
	 		try{
	 		    if (Store.isKML(names[current])){
	 		    	new Fileupload().execute(names[current], "kml");
	 		    	current++;
	 		    	
	 		    }else if (Store.isMP4(names[current])){
	 		    	new FtpVideoUpload().execute(names[current]);
	 		    	current++;
	 		    
	 		    	//ignoring wrong file types
	 		    }else if (Store.isMP4(names[current]) == false && Store.isKML(names[current]) == false){
	 		    	current++;
	 		    }
	 		}catch (Exception e) {}
		
        }
      
		return null;
	};
	
	
	@Override
    protected void onPostExecute(String result) {
      //Log.d("KML UPLOAD", result);
    }
	
	
	
}
