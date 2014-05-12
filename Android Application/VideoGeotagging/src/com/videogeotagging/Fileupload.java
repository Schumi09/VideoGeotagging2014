package com.videogeotagging;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

/*KML Upload*/
class Fileupload extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String filename = params[0];
        String existingFileName = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES +"/VideoGeotagging").getPath() + File.separator + filename;
        Log.d("Trying to upload", existingFileName);
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;
        String responseFromServer = "";
        String urlString = "url/php/receivekml.php";
        try
        {
        Log.d("Uploading", filename); 
        	//------------------ CLIENT REQUEST
        FileInputStream fileInputStream = new FileInputStream(new File(existingFileName) );
         URL url = new URL(urlString);
         conn = (HttpURLConnection) url.openConnection();
         conn.setDoInput(true);
         conn.setChunkedStreamingMode(1024);
         conn.setDoOutput(false);
         conn.setUseCaches(false);
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Connection", "Keep-Alive");
         conn.setRequestProperty("Accept-Encoding",
        		 "musixmatch");
         conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
         conn.setChunkedStreamingMode(1024);
         if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) { conn.setRequestProperty("Connection", "close"); }
         
         dos = new DataOutputStream( conn.getOutputStream() );
         dos.writeBytes(twoHyphens + boundary + lineEnd);
         dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
         dos.writeBytes("Content-Type: multipart" + lineEnd);
         dos.writeBytes(lineEnd);
         bytesAvailable = fileInputStream.available();
         bufferSize = Math.min(bytesAvailable, maxBufferSize);
         buffer = new byte[bufferSize];
         bytesRead = fileInputStream.read(buffer, 0, bufferSize);
         while (bytesRead > 0){
        	    bufferSize = Math.min(bytesAvailable, maxBufferSize);
        	    byte byt[]=new byte[bufferSize];
        	    fileInputStream.read(byt);
        	    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        	    dos.write(buffer, 0, bufferSize);
        	}
         dos.writeBytes(lineEnd);
         dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
         // close streams
         Log.e("Debug","File is written");
         fileInputStream.close();
         dos.flush();
         dos.close();
        
        }
        catch (MalformedURLException ex)
        {
             Log.e("Debug", "error: " + ex.getMessage(), ex);
        }
        catch (IOException ioe)
        {
             Log.e("Debug", "error: " + ioe.getMessage(), ioe);
        }
        //------------------ read the SERVER RESPONSE
        
        try {
              inStream = new DataInputStream ( conn.getInputStream() );
              
             
              while (( responseFromServer = inStream.readLine()) != null)
              {
                   Log.e("Debug","Server Response "+responseFromServer);
                   //conn.disconnect();
                  
              }
              inStream.close();
              

        }
        catch (IOException ioex){
             Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }
        String code;
		try {
			code = String.valueOf(conn.getResponseCode());
			Log.d("Server Code", code);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return responseFromServer;
	};
	
	
	@Override
    protected void onPostExecute(String result) {
      //Log.d("KML UPLOAD", result);
    }
	
	
}