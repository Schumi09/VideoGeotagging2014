package com.videogeotagging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/*FTP Upload*/
public class FtpVideoUpload extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... params) {
		
		String server = "url";
		int port = 21;
		String filename = params[0];
		boolean error = false;
		boolean success = false;
		
		FTPClient client = new FTPClient();
		try {
			Log.d("Uploading", filename);
			int reply;
			client.connect(server, port);
			reply = client.getReplyCode();
			client.login("user", "pw");
			if(!FTPReply.isPositiveCompletion(reply)) {
		        client.disconnect();
		        Log.d("FTP Connection", "Refused");
		      }
			client.setFileType(FTP.BINARY_FILE_TYPE);
			
			client.enterLocalPassiveMode();
			// Prepare file to be uploaded to FTP Server
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES +"/VideoGeotagging"), filename);
            
            FileInputStream ifile = new FileInputStream(file);
           
            client.sendNoOp();
            client.setBufferSize(1024000);
            if(client.listFiles(filename).length == 0){
            success = client.storeFile(filename, ifile);
            publishProgress();
            }
            if(success){
            	Log.d("Video Upload", "Success");
            	ifile.close();
            	return String.valueOf(success);
            } else{
            	Log.d("Video Upload", "Fail");
            	
            }
            //client.logout();
            client.disconnect();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			error = true;
			Log.d("FTP", String.valueOf(error));
			e.printStackTrace();
		}
		
		return String.valueOf(success);
		
	}
	
	@Override
    protected void onPostExecute(String result) {
      Log.d("FTP UPLOAD TASK", result);
      
  
    }
	
	protected void onProgressUpdate() {
	     Log.d("UI", "Alive");
	 }
	
	protected void onPreExecute(){
		
	}
}
