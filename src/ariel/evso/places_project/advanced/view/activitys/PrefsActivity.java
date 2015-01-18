package ariel.evso.places_project.advanced.view.activitys;

import java.io.File;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import ariel.evso.places_project.advanced.R;
import ariel.evso.places_project.advanced.modal.PlacesContract;
import ariel.evso.places_project.advanced.util.Utils;

public class PrefsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	String textDest;
	String textRad;
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// get the string distance from preference
		prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String textDest = prefs.getString("distance", "miels");
		this.textDest = textDest;
		String textRad = prefs.getString("radius", "50000");
		this.textRad = textRad;
		changeRaduisDisplay(textDest, textRad);
		// find preference
		ListPreference prefDistnace = (ListPreference) findPreference("distance");
		ListPreference prefRadius = (ListPreference) findPreference("radius");
		Preference prefDeleteAll = findPreference("action_deleteAll");
		Preference prefExit = findPreference("action_exit");
		// set summary for text km/miles field
		prefDistnace.setSummary("you choose: " + textDest);

		// set list entries, the text that will be shown to the user
		prefDistnace.setEntries(new String[] { "miels", "kilometrs" });
		// set list values, this is for developer usage, the user dosn't see
		// this values
		prefDistnace.setEntryValues(new String[] { "miels", "kilometrs" });
		// set default value
		prefDistnace.setDefaultValue("miels");
		// radius
		prefRadius.setEntries(new String[] { "5 " + textDest, "10 " + textDest,
				"20 " + textDest, "30 " + textDest, "50 " + textDest });
		prefRadius.setEntryValues(new String[] { "5000", "10000", "20000",
				"30000", "50000" });
		prefRadius.setOnPreferenceChangeListener(this);
		// set preference change listener
		prefDistnace.setOnPreferenceChangeListener(this);
		// set preference click listener
		prefDeleteAll.setOnPreferenceClickListener(this);
		prefExit.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		// get the preference key
		String key = pref.getKey();
		// check if the key equals to distance
		if (key.equals("distance")) {
			// set the new value of distance key as a summary
			pref.setSummary("you choose: " + (String) newValue);
			//set the new value to local field 
			this.textDest = (String) newValue;
			//get the radius preference 
			ListPreference prefRadius = (ListPreference) findPreference("radius");
			//update the value of the entries , cheap solution i know but didn't have much time to build better
			//function's for radius handling
			//and yeah of course i only fool the user here, didn't really change the values of the radius
			//from km to miles just the text show it always will be km, like i said did it on the last minute
			//if had more time i would handle this and make something better
			prefRadius.setEntries(new String[] { "5 " + textDest, "10 " + textDest,
					"20 " + textDest, "30 " + textDest, "50 " + textDest });
			String textRad = prefs.getString("radius", "50000");
			this.textRad = textRad;
			//notify the radius field about the change
			changeRaduisDisplay((String) newValue , textRad);
		} else if (key.equals("radius")) {
			// set the new value of radius and distance key as a summary
			changeRaduisDisplay(textDest, (String) newValue);;
		}
		// handle
		return true;
	}
	/**
	 * helper method that gets user preference distance and radius display<br>
	 * shortens the radius value only for user display<br>
	 * and set that value as a summary to radius field
	 * @param textDest - the preference distance
	 * @param textRad - the preference radius
	 */
	private void changeRaduisDisplay(String textDest , String textRad) {
		
		ListPreference prefRadius = (ListPreference) findPreference("radius");
		//check the value of the radius , and set compatible text
				if(textRad.equals("5000")){textRad = "5";}
				if(textRad.equals("10000")){textRad = "10";}
				if(textRad.equals("20000")){textRad = "20";}
				if(textRad.equals("30000")){textRad = "30";}
				if(textRad.equals("50000")){textRad = "50";}
				//set the summary with the right text from the if statements
		prefRadius.setSummary("your raduis for search: " + (String) textRad + " "
				+ (String) textDest);
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		Uri uri = PlacesContract.Favorites.CONTENT_URI;
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		// get the preference key
		String key = pref.getKey();
		// check if key equals to delete all items
		if (key.equals("action_deleteAll")) {
			//check if there is anything to delete in favorites table
			if(c.getCount() != 0){
				//if there is alert before delete all
			   alertDeleteAll();
			}else{
				Toast.makeText(this, "there is no data in your favorits",
						Toast.LENGTH_SHORT).show();
			}
			//close the cursor
			c.close();
			// check if key equals to exit
		} else if (key.equals("action_exit")) {
			// exit the activity
			finish();
		}
		// handle
		return true;
	}

	private void alertDeleteAll() {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// delete all the favorites from data base
					getContentResolver().delete(
							PlacesContract.Favorites.CONTENT_URI, null, null);
					// delete the image folder
					File dir = getDir("imageDir", Context.MODE_PRIVATE);
					Utils.deleteDir(dir); getApplicationContext();
					
					Toast.makeText(getApplicationContext(), "all favorits deleted",
							Toast.LENGTH_SHORT).show();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// do nothing
					break;
				}
			}

		};

		// the dialog builder:
		Builder builder = new AlertDialog.Builder(this);

		// set the dialog properties:
		builder.setTitle("Wait!").setMessage("You sure, delete all favories?")
				.setPositiveButton("Yes", listener)
				.setNegativeButton("No", listener);

		// create the actual dialog:
		AlertDialog dialog = builder.create();

		// show the dialog:
		dialog.show();

	}

}
