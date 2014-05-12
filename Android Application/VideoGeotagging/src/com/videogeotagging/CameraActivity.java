package com.videogeotagging;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.videogeotagging.LocationService.MyLocalBinder;


public class CameraActivity extends Activity{
    
	private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private ImageButton captureButton;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    
    public Context ctx = null;
    
    private Intent locationService;
    private LocationService myService;
    boolean isBound = false;
    
    public long videostart;
    private String filename = null;
    private String fname;
    private String description;
    private String locationmsg;
    
    private Handler kml_handler = new Handler();
    private File sdcard = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES +"/VideoGeotagging");
    FrameLayout preview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ctx = getApplication();
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setVisibility(View.GONE);
        registerReceiver(onBroadcast, new IntentFilter("GPS_FIX")); //waiting for GPS fix
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        locationService = new Intent(ctx, LocationService.class); //starting Location Service
    	startService(locationService);
    	getApplicationContext().bindService(locationService, myConnection, BIND_DEBUG_UNBIND); //bind location service for being able to access last position
    	
    	/*Getting filename, description and location*/
    	Intent intent = getIntent();
    	fname = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
    	description = intent.getStringExtra(MainActivity.EXTRA_DESCRIPTION);
    	locationmsg = intent.getStringExtra(MainActivity.EXTRA_LOCATION);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                	
					public void onClick(View v) {
                    	
                        if (isRecording) {
                            
                        	mMediaRecorder.stop();  
                            releaseMediaRecorder(); 
                            mCamera.lock();        
                            // inform the user that recording has stopped
                            captureButton.setImageResource(R.drawable.capture);
                           
                            isRecording = false;
                            
                            
                            try{
                            	kml_handler.removeCallbacks(runnable);
                            	Location location = myService.lastPosition(); 
                    			KML.write_body(filename, location, videostart);
                    			KML.write_bottom(filename);
                    			
                    			File from = new File(sdcard,"dummy.mp4");
                    			File to = new File(sdcard, filename+".mp4");
                    			from.renameTo(to);
                    			
                    			final Handler handler = new Handler();
                    			handler.postDelayed(new Runnable() {
                    			  @Override
                    			  public void run() {
                    			    keepRecording(filename);
                    			  }
                    			}, 100);
                    			
                            	} catch (Exception e){
                                Log.d("DEBUG", "Closing file failed");
                            }
                            
                        	} else {
                            // initialize video camera and start capturing Spatial Video
                            if (prepareVideoRecorder()) {
                                // Camera is available and unlocked, MediaRecorder is prepared
                                
                            	mMediaRecorder.start();
                            	long vs = System.currentTimeMillis();
                            	
                            	setVideostart(vs); //Videostart 
                            	String vs_ = Store.getFileNameTimestamp(videostart);
                                setFilename(vs_);
                                KML.createFile(filename, fname, locationmsg);
                            	Location location = myService.lastPosition(); //getting lastknownlocation from location service
                    			
                    			try {
                    				KML.write_body(filename, location, videostart);
                    			} catch (IOException e) {
                    				// TODO Auto-generated catch block
                    				e.printStackTrace();
                    			}
                    			
                            	kml_handler.postDelayed(runnable, 1000);
                            	
                            	// inform the user that recording has started
                                captureButton.setImageResource(R.drawable.stop);
                                isRecording = true;
                            	
                            	
                            } else {
                                // prepare didn't work, release the camera
                                releaseMediaRecorder();
                                // inform user
                            }
                        }
                    }
                }
       );
    }
    
    
    private void setVideostart(long videostart){
    	this.videostart = videostart;
    }
    
    private void setFilename(String vs){
    	this.filename = this.fname + "_" + vs;
    }
    
    
    /**binding Locationservice to access its methods*/
	private ServiceConnection myConnection = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        MyLocalBinder binder = (MyLocalBinder) service;
	        myService = binder.getService();
	        isBound = true;
	        
	}
	    
	public void onServiceDisconnected(ComponentName arg0) {
	        isBound = false;
	}
	    
	};

    /** A safe way to get instance of Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
        c = Camera.open(); // attempt to get a Camera instance
        if (c != null){
            Camera.Parameters params = c.getParameters();
            c.setParameters(params);
        }
    }
    catch (Exception e){
        Log.d("DEBUG", "Camera did not open");
        // Camera is not available (in use or does not exist)
    }
        return c; // returns null if camera is unavailable
    }

    /**Default settings for camera*/
    private boolean prepareVideoRecorder(){


        mMediaRecorder = new MediaRecorder();
        mCamera.stopPreview();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        CamcorderProfile camprofil = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        mMediaRecorder.setProfile(camprofil);
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("DEBUG", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("DEBUG", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /** Create a file Uri for saving an image or video
    private static Uri getOutputMediaFileUri(int type){
          return Uri.fromFile(getOutputMediaFile(type));
    }*/

    private File getOutputMediaFile(int type){
        

        File mediaStorageDir = sdcard;

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("VideoGeotagging", "failed to create directory");
                return null;
            }
        }

        // Create a media file name with timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + 
            "dummy.mp4");
            
        } else {
            return null;
        }

        return mediaFile;
    }

   
    
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       
        preview.removeView(mPreview);
        releaseCamera();              // release the camera immediately on pause event
        kml_handler.removeCallbacks(runnable); //cancel kml filling
        unregisterReceiver(onBroadcast);
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   
            mMediaRecorder.release(); 
            mMediaRecorder = null;
            mCamera.setPreviewCallback(null);
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            isRecording = false;
            mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	releaseMediaRecorder();
    	releaseCamera();
    	
    	
    	getApplicationContext().unbindService(myConnection);
    	
    	
    	if (isBound) {
            try {
                unregisterReceiver(onBroadcast);
               
            } catch (IllegalArgumentException e) {
                // TODO: handle exception
            }
            isBound = false;
        }
    	
    	stopService(locationService);
    	kml_handler.removeCallbacks(runnable);
    	
    }
    
    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent i) {
        	captureButton.setVisibility(View.VISIBLE);
        }
    };
    
   
    
    //Fill the KML at fixed Interval
    private Runnable runnable = new Runnable(){
		@Override
		public void run() {

			Location location = myService.lastPosition(); 
			try {
				KML.write_body(filename, location, videostart);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			kml_handler.postDelayed(this, 1000);
		}
    	
    };
    
    /*Asking user to keep data*/
    public void keepRecording(final String filename){
 
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

 		alert.setTitle("Save");
 		alert.setMessage("Do you want to keep the recorded Data?");
 		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			String text = "Recording saved.";
 			Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
 			instantUpload(filename);
 			
 		  }
 		});

 		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 			String kml = filename + ".kml";
 			String video = filename + ".mp4";
 			boolean kmlDel = Store.deleteFile(kml);
 			boolean vidDel = Store.deleteFile(video);
 			if (kmlDel == true && vidDel == true){
 				Toast.makeText(ctx, "Recording deleted.", Toast.LENGTH_SHORT).show();
 			}
 			
 			
 		  }
 		});

 		alert.show();
 	}
    
    
    /*Asking for instant upload*/
    public void instantUpload(final String filename){
    	 
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

 		alert.setTitle("Upload");
 		alert.setMessage("Upload Data Now?");

 		
 		//uploading kml and video file
 		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			String kml = filename + ".kml";
 			String video = filename + ".mp4";
 			String filelist[] = new String[2];
 			filelist[0] = video;
 			filelist[1] = kml;
 			UploadTask upload = new UploadTask(filelist);
 			upload.execute(filelist);
 		  }
 		});

 		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 			Log.d("Instant upload", "No");
 		  }
 		});

 		alert.show();
 	}
    
    
}