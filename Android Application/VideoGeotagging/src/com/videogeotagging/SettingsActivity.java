package com.videogeotagging;




import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/*Settings Menu*/
public class SettingsActivity extends PreferenceActivity {
	
	private static final int UPLOAD_ALL_DIALOG = 10;
	private static final int DELETE_ALL_DIALOG = 20;
	private Preference uploadall;
	private Preference deleteall;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		addPreferencesFromResource(R.xml.preferences);
		uploadall = (Preference) findPreference("upload_all");
		uploadall
			.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
			    
				@Override
			    public boolean onPreferenceClick(Preference preference)
			    {
			    	
			    	showDialog(UPLOAD_ALL_DIALOG);
		 			
				return true;
			    }
			});
		
		deleteall = (Preference) findPreference("delete_all");
		
		deleteall
			.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
			    @Override
			    public boolean onPreferenceClick(Preference preference)
			    {
			    	showDialog(DELETE_ALL_DIALOG);

				return true;
			    }
			});
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id)
    {
	
	Builder builder = new AlertDialog.Builder(this);
	switch (id)
	{
	case UPLOAD_ALL_DIALOG:
	    // Create custom Delete Database Dialog
		Log.d("Upload", "all");
		builder.setMessage("Upload all Records?");
	    builder.setCancelable(true);
	    builder.setPositiveButton("Yes",
		    new UploadDialogOkOnClickListener());
	    builder.setNegativeButton("No", new CancelOnClickListener());
	    AlertDialog uploadDialog = builder.create();
	    return uploadDialog;
	    
	case DELETE_ALL_DIALOG:
	    // Create custom Export Database Dialog
	    builder.setMessage("Do you really want to delete all Records?");
	    builder.setCancelable(true);
	    builder.setPositiveButton("Yes",
		    new DeleteOnClickListener());
	    builder.setNegativeButton("No", new CancelOnClickListener());
	    AlertDialog deletedDialog = builder.create();
	    
	    return deletedDialog;
	
	}

	return null;
    }
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		return false;
	
    }
	
	private final class UploadDialogOkOnClickListener implements
    DialogInterface.OnClickListener
	{
	public void onClick(DialogInterface dialog, int which)
		{
	    // Upload all data
	   Store.uploadAll();
		}
	}
	
	private final class DeleteOnClickListener implements
    DialogInterface.OnClickListener
	{
	public void onClick(DialogInterface dialog, int which)
		{
		String[] files = Store.getFileList();
		if(files.length != 0){
		Store.deleteAll(files);
		Toast.makeText(getApplicationContext(), "Data deleted.", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getApplicationContext(), "No Data to remove.", Toast.LENGTH_SHORT).show();
		}
	
		
		}
	}
	private final class CancelOnClickListener implements
    DialogInterface.OnClickListener
	{
	public void onClick(DialogInterface dialog, int which)
		{
	    //Do nothing
	   
		}
	}
}
