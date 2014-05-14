package com.videogeotagging;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;



public class LocationService extends Service implements SensorEventListener{
	
	private Context ctx;
	//Location
	private LocationManager locationManager;
	private VgLocationListener vgLocationListener;
	public Location location;
	public Bundle location_extra_data = new Bundle();
	
	//Service-Binder
	private final IBinder myBinder = new MyLocalBinder();
	
	//helper variables
	public boolean status = false;
	public boolean startfix = false;
	public Location prevLocation;
	public boolean standing = false;
	private final float locationcritera = 0.6f; //speed in m/s

	
	
	//Orientationsensor
	private static SensorManager sensorService;
	private Sensor accelerometer, magnetometer;
	static final float ALPHA = 0.25f;
	public double heading;
	
	//matrices for calculating the orientation
	float gravity[] = new float[3]; 
	float R_out[] = new float[9]; 
	float R_in[] = new float[9];
	float hO[] = new float[3];
	float geomagneticfield[] = new float[3];
	
	  
	/*returning last location*/
	public Location lastPosition(){
		return location;
	}
	
	/*setting location*/
	public void setLocation(Location location){
		this.location = location;
	}
	
	
	/*Registering Location and Sensor Managers for requesting sensor updates*/
	@Override
    public void onCreate()
    {
	super.onCreate();

	// Initialize attributes
	ctx = getApplicationContext();
	
	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	vgLocationListener = new VgLocationListener();
	
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, vgLocationListener);
	Toast.makeText(ctx, "Waiting for GPS FIX...This could take a while", Toast.LENGTH_SHORT).show();

	sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    
    accelerometer = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    magnetometer = sensorService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    sensorService.registerListener(this, accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL);
    sensorService.registerListener(this, magnetometer,
            SensorManager.SENSOR_DELAY_NORMAL);
    }
	
	private class VgLocationListener implements LocationListener
    {
	
		
		
	/*Processing Location*/	
	@Override
	public void onLocationChanged(Location location)
	{
		
		long time = System.currentTimeMillis();
		float current_heading = (float) heading; 
		location.setTime(time);
		
		float accuracy = location.getAccuracy();
		
		
		location.setTime(time);
		
		if (startfix == false){
			prevLocation = new Location(location);
			prevLocation.set(location);
			
			prevLocation.setTime(time);
			startfix = true;
			
		}

		/*Waiting for GPS Fix for allowing Video Capture*/
		if (accuracy < 150){
			broadcastGPSIntent();
			location = getBetterLocation(prevLocation, location);
			prevLocation.set(location);
			prevLocation.setTime(time);
			}
			else{
				location.set(prevLocation);
				location.setTime(time);
				
			}
			
		location.setTime(time);
		
		location.setBearing(current_heading);
		setLocation(location);
		
	}

	
	/*Check whether the new location is more or less accurate*/
	public Location getBetterLocation(Location prevLocation, Location location){
		
		
	    
		int accuracyDelta = (int) (location.getAccuracy() - prevLocation.getAccuracy());
	    float speed = location.getSpeed();
	    
	    boolean isMoreAccurate = accuracyDelta < 5; //allowing 5m accuracy overlap
	    
	    
	    if(isMoreAccurate){
	    	
	    	return location;
	    }else {
	    	if (speed < locationcritera){
	    		return prevLocation;
	    	}else{
	    		return location;
	    	}
	    }
		
	}
	
	
	
	@Override
	public void onProviderDisabled(String provider)
	{
	    Toast.makeText(ctx, provider + " disabled", Toast.LENGTH_LONG)
		    .show();
	    
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	    Toast.makeText(ctx, provider + " enabled", Toast.LENGTH_LONG)
		    .show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	    String providerStatus = "";

	    switch (status)
	    {
	    case LocationProvider.AVAILABLE:
		providerStatus = "available";
		break;
	    case LocationProvider.OUT_OF_SERVICE:
		providerStatus = "out of service";
		break;
	    case LocationProvider.TEMPORARILY_UNAVAILABLE:
		providerStatus = "temporarily unavailable";
		break;
	    default:
		break;
	    }

		}
    }

	/*Providing Binder for CameraActivity*/
	public class MyLocalBinder extends Binder {
		LocationService getService() {
            return LocationService.this;
        }
	}
	@Override
	public IBinder onBind(Intent intent) {
		
		return myBinder;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        
    	locationManager.removeUpdates(vgLocationListener);
        locationManager = null;
        sensorService.unregisterListener(this);
    }
	
    /*Allowing Video Capture*/
    public void broadcastGPSIntent()
    {
       try {
		Intent gpsfix = new Intent("GPS_FIX");
		   sendBroadcast(gpsfix);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /*Calculating device heading by Accelerometer and Magnetic Field Sensor*/
    @Override
    public void onSensorChanged(SensorEvent event) {
    	/*Getting orientation of the Device and Magnetic Field Strength*/	
	    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	    gravity = lowPass(event.values.clone(), gravity);
	    }
	    
	    else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
	        geomagneticfield = lowPass(event.values.clone(), geomagneticfield);
	    }
	    /*Remapping Coordinate System for assuming that device is always tilted vertical and camera facing away*/ 	
	    if (geomagneticfield != null && gravity != null) {
	        if (SensorManager.getRotationMatrix (R_in, null, gravity, geomagneticfield)) {               
	            SensorManager.remapCoordinateSystem (R_in, SensorManager.AXIS_X, SensorManager.AXIS_Z, R_out);
	            SensorManager.getOrientation (R_out, hO);
	        }
	    }
	    float headinghelp = hO[0]; //in radians
	    heading = headinghelp; //convertHeading did not take parameter otherwise
	    heading = convertHeading(heading);	 //to degrees   
    }
    
    /*Low Pass Filter for smoothing Sensor Data*/
    protected float[] lowPass( float[] input, float[] output ) {
	    if ( output == null ) return input;     
	    for ( int i=0; i<input.length; i++ ) {
	        output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
	} 
  
    /* returning a 360 degree angle
     * orientation function returns values between -180° and + 180°*/
    public double convertHeading(double value){
    	value = Math.toDegrees(value); //converting Radians to Degrees
    	
    	if (value < 0){
    		value = 360 + value;
    		return value;
    	}
    		else return value;
    }

}
