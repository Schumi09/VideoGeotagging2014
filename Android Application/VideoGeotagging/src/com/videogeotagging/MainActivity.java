package com.videogeotagging;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.videogeotagging.MESSAGE";
	public final static String EXTRA_LOCATION = "com.videogeotagging.LOCATION";
	public final static String EXTRA_DESCRIPTION = "com.videogeotagging.DESCRIPTION";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		      StrictMode.setThreadPolicy(policy);
		    }
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    switch (item.getItemId())
	    {
	        case R.id.menu_settings:
	        {
	            startActivity(new Intent(this, SettingsActivity.class));
	            
	            break;
	        }
	    }
	    return true;
	}
	
	
	
	
	
	/** Called when the user clicks the Send button */
	public void openCameraActivity(View view) {
		Intent intent = new Intent(this, CameraActivity.class);
		EditText editText = (EditText) findViewById(R.id.edit_message);
	    String name = editText.getText().toString();
	    EditText editLocation = (EditText) findViewById(R.id.edit_location);
	    String location = editLocation.getText().toString();
	    EditText editDescription = (EditText) findViewById(R.id.edit_description);
	    String description = editDescription.getText().toString();
	    
	    if (name.length() > 0){
	    intent.putExtra(EXTRA_MESSAGE, name);
	    intent.putExtra(EXTRA_LOCATION, location);
	    intent.putExtra(EXTRA_DESCRIPTION, description);
		startActivity(intent);
	    }	else{
	    	Toast.makeText(getBaseContext(), "Please enter at least a name", Toast.LENGTH_SHORT).show();
	    	
	    }
	}

}
