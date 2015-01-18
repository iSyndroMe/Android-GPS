package ariel.evso.places_project.advanced.control.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import ariel.evso.places_project.advanced.modal.Place;
import ariel.evso.places_project.advanced.modal.PlacesContract;
import ariel.evso.places_project.advanced.util.GoogleAccess;
import ariel.evso.places_project.advanced.util.Utils;

/**
 * a Service for searching a place and saving the results<br>
 * this will perform a search in the background and store the results in the
 * appProvider.<br>
 * (Previous results will be deleted!) <br>
 * <br>
 * start this service with two possible actions: <b>
 * {@code "ariel.evso.places_project.advanced.action.SERACH_PLACE"} </b><br>
 * <b>OR</b> <br>
 * <b>{@code "ariel.evso.places_project.advanced.action.SERACH_NEARBY"} </b> <br>
 * expected extras if <b>action.SERACH_PLACE</b> :<br>
 * <ul>
 * <li><b>"query"</b> : the search String.</li>
 * </ul>
 * expected extras if <b>action.SERACH_NEARBY</b> :<br>
 * <ul>
 * <li><b>"query"</b> : the search String.</li>
 * <li><b>"location"</b> : the user GPS/NETWORK location.</li>
 * </ul>
 */

public class SearchPlacesService extends IntentService {

	private Intent resultsIntent;

	public SearchPlacesService() {
		super("SearchPlacesService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//get the action of the calling intent
		String action = intent.getAction();
		//delete all data from the provider (places table)
		getContentResolver().delete(PlacesContract.Places.CONTENT_URI, null,
				null);
		//get the text search 
		String q = intent.getStringExtra("query");
		//get the user location
		Location loc = (Location) intent.getParcelableExtra("location");
		//get extra on start
		boolean onStart = intent.getBooleanExtra("onStart", false);
		//create local broadcast manager 
		LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
				.getInstance(this);
		//check if APP run from first launch and not from orientation change
		if (!onStart) {
			//check if search text is not null or lower then two letters
			if (q == null || q.length() < 2) {
				//send error broadcast miss match letters number
				resultsIntent = new Intent(
						"ariel.evso.places_project.advanced.action.BROADCAST.LOW.LETTERS");
				localBroadcastManager.sendBroadcast(resultsIntent);
				return;
			}
		}
		//check the actions and start service with matched action
		if (action
				.equals("ariel.evso.places_project.advanced.action.SERACH_PLACE")) {
			getPlaceTextSearch(q);
		} else if (action
				.equals("ariel.evso.places_project.advanced.action.SERACH_NEARBY")) {
			if (loc != null) {
				getPlaceNearbySearch(loc, q);
			} else {
				//send error broadcast unknow location
				resultsIntent = new Intent(
						"ariel.evso.places_project.advanced.action.BROADCAST.UNKNOW.LOCATION");
				localBroadcastManager.sendBroadcast(resultsIntent);
				return;
			}
		}
	}

	/**
	 * iterate over a JSON array result from Google Places API "text" search<br>
	 * and insert every place object to database<br>  
	 * @param q - the search string
	 */
	private void getPlaceTextSearch(String q) {
		//get the JSON string result
		String resultString = GoogleAccess.searchPlace(q);
		try {
			//get the JSON object
			JSONObject resultJSON = new JSONObject(resultString);
			//get the array inside that object
			JSONArray resultsArray = resultJSON.getJSONArray("results");
			//iterate over that array
			for (int i = 0; i < resultsArray.length(); i++) {
				//get the values of each object
				ContentValues val = Place.toContentVal(
						resultsArray.getJSONObject(i),
						Utils.REQUEST_CODE_SEARCH);
				//insert the values to places table
				getContentResolver().insert(PlacesContract.Places.CONTENT_URI,
						val);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * iterate over a JSON array result from Google Places API "near by" search<br>
	 * and insert every place object to database<br>  
	 * @param loc - the user location
	 * @param q - the search string
	 */
	private void getPlaceNearbySearch(Location loc, String q) {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String textRad = prefs.getString("radius", "50000");
		
		//get the JSON string result
		String resultString = GoogleAccess.searchNearby(loc, q ,textRad);

		try {
			//get the JSON object
			JSONObject resultJSON = new JSONObject(resultString);
			//get the array inside that object
			JSONArray resultsArray = resultJSON.getJSONArray("results");
			//iterate over that array
			for (int i = 0; i < resultsArray.length(); i++) {
				//get the values of each object
				ContentValues val = Place.toContentVal(
						resultsArray.getJSONObject(i),
						Utils.REQUEST_CODE_NEARBY);
				//insert the values to places table
				getContentResolver().insert(PlacesContract.Places.CONTENT_URI,
						val);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
