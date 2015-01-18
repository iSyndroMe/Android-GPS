package ariel.evso.places_project.advanced.view.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import ariel.evso.places_project.advanced.R;
import ariel.evso.places_project.advanced.control.receivers.ServiceMangementReciver;
import ariel.evso.places_project.advanced.modal.Place;
import ariel.evso.places_project.advanced.modal.PlacesContract;
import ariel.evso.places_project.advanced.util.GoogleAccess.TaskGetIcon;
import ariel.evso.places_project.advanced.util.Utils;

import com.google.android.gms.maps.model.LatLng;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class FragList extends Fragment implements OnRefreshListener<ListView>,
		OnItemClickListener, OnClickListener, LoaderCallbacks<Cursor>,
		LocationListener, OnCheckedChangeListener, OnEditorActionListener {

	private String TAG = "FragLists";

	private PlacesAdapter adapter;
	private Location location;
	private boolean isChecked;
	private boolean onStart = true;
	private int f = 0;

	ListFragmentListener listener;

	/**
	 * interface that provide to you a complete place object
	 */
	public interface ListFragmentListener {
		void onPlaceSelected(Place place);
	}

	/**
	 * factory method to create fragment
	 * 
	 * @return - places fragment list
	 */
	public static FragList newInstance() {

		FragList f = new FragList();

		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// check if the activity really implements ListFragmentListener
		try {
			listener = (ListFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("activity " + activity.toString()
					+ "must implement ListFragmentListener!");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate the view
		View v = inflater.inflate(R.layout.frag_list, container, false);
		// get the loader manager and set to it your loader id
		getLoaderManager().initLoader(Utils.PLACES_LOADER_ID, null, this);
		// find the the text view
		EditText et = (EditText) v.findViewById(R.id.editSearch);
		// set last know location to local variable location
		this.location = Utils.getLastKnowLocation(getActivity());
		// create the places table adapter
		adapter = new PlacesAdapter(getActivity(), null);
		// find button search view
		Button btnSearch = (Button) v.findViewById(R.id.btnSearch);
		// find check box view
		CheckBox checkSearch = (CheckBox) v.findViewById(R.id.checkBoxSearch);

		Log.d(TAG, location + "");
		// find the list view
		PullToRefreshListView lv = (PullToRefreshListView) v
				.findViewById(R.id.listViewPlaces);
		// set the adapter to the list view
		lv.setAdapter(adapter);
		// set listeners
		lv.setOnRefreshListener(this);
		lv.setOnItemClickListener(this);
		btnSearch.setOnClickListener(this);
		checkSearch.setOnCheckedChangeListener(this);
		et.setOnEditorActionListener(this);
		// register the list for context menu
		registerForContextMenu(lv.getRefreshableView());
		// return the view
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		// save local variable flag to instance state, so we later can know
		// if activity created first time or from orientation change
		outState.putInt("firstRun", f);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// find check box view
		CheckBox checkSearch = (CheckBox) getActivity().findViewById(
				R.id.checkBoxSearch);

		Log.d(TAG, "oncreate value of flag" + f + "");

		// check if there is any values to grab inside the bundle
		if (savedInstanceState != null) {
			// if there is, take the flag "firsRun"
			int flag = savedInstanceState.getInt("firstRun");
			// and override our local flag with the flag form the bundle
			f = flag;
		}

		// if the flag equals to 0 , meaning we are on first run and we want run
		// auto search by location
		if (f == 0) {
			// set isChecked to true , because this variable define how we going
			// to search(nearby or everywhere);
			// true = nearby
			// false = everywhere
			isChecked = true;
			// set check box to checked just for be synchronized with what we
			// doing and what we showing to the user
			checkSearch.setChecked(true);
			// do the search with no keyword because we want only location
			// search
			doTheSearch(null);
			// set flag to 1 so the next time when activity destroy's from
			// orientation
			// change we will skip this statement
			f = 1;
		}
	}

	@Override
	public void onClick(View v) {
		// find the edit text view
		EditText et = (EditText) getView().findViewById(R.id.editSearch);
		// get the text from it
		String query = et.getText().toString();
		// save it to shared preferences
		saveToSharedPref(query);

		switch (v.getId()) {
		case R.id.btnSearch:
			// start the search
			doTheSearch(query);
			break;
		}

	}

	/**
	 * helper method<br>
	 * checks on which one of the Google API do the search <b>near by</b><br>
	 * or <b>text</b> , according to if check box is checked or not
	 * <ul>
	 * <li>checked - Google API <b>near by</b> search</li>
	 * <li>unchecked - Google API <b>text</b> search</li>
	 * </ul>
	 * then will start the search service with the given query<br>
	 * 
	 * @param query
	 *            - text for search
	 */

	private void doTheSearch(String query) {
		Intent intent;
		// check if Internet connection is available
		if (Utils.isInternetAvailable(getActivity())) {
			// if check box not checked
			if (!isChecked) {
				// set action
				intent = new Intent(
						"ariel.evso.places_project.advanced.action.SERACH_PLACE");
				// put search text as extra
				intent.putExtra("query", query);
				// start service with given action
				getActivity().startService(intent);
			} else {
				// set action
				intent = new Intent(
						"ariel.evso.places_project.advanced.action.SERACH_NEARBY");
				// put user location as extra
				intent.putExtra("location", location);
				// put search text as extra
				intent.putExtra("query", query);
				// put boolean if we are run on start or not
				intent.putExtra("onStart", onStart);
				// start service with given action
				getActivity().startService(intent);
				Log.d(TAG, "the value of location and keyword:" + location
						+ query);
			}
		} else {
			// make toast no Internet
			Toast.makeText(getActivity(), "No Internet Connection",
					Toast.LENGTH_SHORT).show();
			return;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		// place table name from places database
		String tableName = PlacesContract.Places.TABLE_NAME;
		// build place object from places table
		Place place = Place.buildPlaceObject(id, getActivity(), tableName);
		// pass that place to the interface
		listener.onPlaceSelected(place);

	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		// if i didn't make this check, it crash's here when no Internet don't
		// really know why
		// check if Internet connection is available
		if (Utils.isInternetAvailable(getActivity())) {
			// start the task
			new TaskRefresh().execute();
		} else {
			// make toast no Internet
			Toast.makeText(getActivity(), "No Internet Connection",
					Toast.LENGTH_SHORT).show();
			// stop refreshing
			refreshView.onRefreshComplete();
			return;
		}
	}

	private class TaskRefresh extends AsyncTask<Void, Void, Void> {
		// get the list view
		PullToRefreshListView lv = (PullToRefreshListView) getView()
				.findViewById(R.id.listViewPlaces);
		// get shared preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		// get the search text name query
		String q = prefs.getString("query", null);

		@Override
		protected void onPreExecute() {
			// start refreshing
			lv.setRefreshing();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// start search after checks that there is some value for search
			if (q != null || q != "") {
				doTheSearch(q);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// stop refreshing
			lv.onRefreshComplete();
		}

	}

	/**
	 * class that get values from data base and set them to views<br>
	 * his main purpose is simple to drawing the view
	 */
	class PlacesAdapter extends CursorAdapter {

		public PlacesAdapter(Context context, Cursor c) {
			super(context, c, 0);

		}

		@Override
		public void bindView(View v, Context ctx, Cursor c) {
			// had some problems to use the code below it worked fine
			// but for some search's it crash's why?
			// long id = c.getLong(c.getColumnIndex(PlacesContract.Places._ID));
			// String tableName = PlacesContract.Places.TABLE_NAME;

			/*
			 * Place place = Place.buildPlaceObject(id, ctx, tableName);
			 * Log.d("Value of ID" , "This is id!!!!" + id + place.getName());
			 * LatLng loc = place.getLocation();
			 */

			// get all columns
			String name = c.getString(c
					.getColumnIndex(PlacesContract.Places.NAME));
			String address = c.getString(c
					.getColumnIndex(PlacesContract.Places.ADDRESS));
			double lat = c.getDouble(c
					.getColumnIndex(PlacesContract.Places.lAT));
			double lng = c.getDouble(c
					.getColumnIndex(PlacesContract.Places.lNG));
			double rating = c.getDouble(c
					.getColumnIndex(PlacesContract.Places.RATING));
			String icon = c.getString(c
					.getColumnIndex(PlacesContract.Places.IMG_URL));
			
			RatingBar rb = (RatingBar) v.findViewById(R.id.ratingBar);
			// check if the rating is not null
			try {
				if (rating != 0) {
					// if is not set it to the view
					rb.setRating((float) rating);
				}
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// create place object
			Place place = new Place(name, address, new LatLng(lat, lng),
					rating, icon);
			// find some views
			TextView textName = (TextView) v.findViewById(R.id.textName);
			TextView textAddress = (TextView) v.findViewById(R.id.textAddres);
			TextView textDistance = (TextView) v
					.findViewById(R.id.textDistance);
			ImageView imgView = (ImageView) v.findViewById(R.id.imageViewIcon);
			// set tag for the view
			v.setTag(place);
			// start icon download task
			new TaskGetIcon(v, place).execute();
			// hide the image view
			imgView.setVisibility(View.INVISIBLE);
			// set the distance text
			Utils.setLocationText(location, new LatLng(lat, lng), textDistance,
					ctx);
			// set some other text's
			textName.setText(name);
			textAddress.setText(address);

		}

		@Override
		public View newView(Context ctx, Cursor c, ViewGroup parent) {
			// inflate the layout
			LayoutInflater inflater = LayoutInflater.from(ctx);
			View v = inflater.inflate(R.layout.places_list, parent, false);
			return v;
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// inflate context menu
		getActivity().getMenuInflater().inflate(R.menu.contex_menu_list, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// get the adapter context menu info
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		// get id from that info
		long id = info.id;

		Log.d(TAG, "value of: " + id);
		// the table name
		String tableName = PlacesContract.Places.TABLE_NAME;
		// build place object with given id
		Place place = Place.buildPlaceObject(id, getActivity(), tableName);

		switch (item.getItemId()) {
		case R.id.action_share:
			// start share
			shareIntent(place);
			break;
		case R.id.action_addFavorite:
			// start add to favorites
			addToFavorites(place);
			break;

		}
		// handled
		return true;
	}

	/**
	 * receive {@link Place} object<br>
	 * and open android share intent
	 * 
	 * @param p
	 *            - {@link Place} object , it needs only place <b>name</b> and
	 *            <b>address</b> so you don't have to pass complete place object
	 */
	private void shareIntent(Place p) {
		// create intent
		Intent sendIntent = new Intent();
		// set action send
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, p.getName());
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, p.getAddress());
		sendIntent.setType("text/plain");
		// start activity
		startActivity(sendIntent);

	}

	/**
	 * receive complete {@link Place} object and insert it to favorites table
	 * 
	 * @param place
	 *            - complete place object
	 */
	private void addToFavorites(Place place) {
		// get APP context
		Context appContext = getActivity().getApplicationContext();
		// get place location
		LatLng location = place.getLocation();
		Uri uri = PlacesContract.Favorites.CONTENT_URI;
		String tableName = PlacesContract.Favorites.TABLE_NAME;
		Place p = null;

		try {
			Cursor c = getActivity().getContentResolver().query(uri, null,
					null, null, null);
			long id = -1;
			while (c.moveToNext()) {
				id = c.getLong(c.getColumnIndex(PlacesContract.Favorites._ID));
				p = Place.buildPlaceObject(id, getActivity(), tableName);
				if (place.isEquals(p)) {
					Toast.makeText(getActivity(),
							"this place already in your favorites",
							Toast.LENGTH_SHORT).show();
					return;
				}
			} 
			c.close();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// build values from the received place object
		ContentValues val = new ContentValues();
		val.put(PlacesContract.Favorites.NAME, place.getName());
		val.put(PlacesContract.Favorites.ADDRESS, place.getAddress());
		val.put(PlacesContract.Favorites.lAT, location.latitude);
		val.put(PlacesContract.Favorites.lNG, location.longitude);
		val.put(PlacesContract.Favorites.RATING, place.getRating());

		// insert the values to database
		getActivity().getContentResolver().insert(
				PlacesContract.Favorites.CONTENT_URI, val);
		// save icon to internal storage
		Utils.saveToInternalSorage(Utils.iconPlace, appContext, place.getName());
		Toast.makeText(getActivity(), place.getName() + " Added to favorite",
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		// create a loader:
		return new CursorLoader(getActivity(),
				PlacesContract.Places.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		// cursor in ready
		// swap it into the adapter:
		adapter.swapCursor(c);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// cursor is going to be reset
		// remove it from the adapter:
		adapter.swapCursor(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		// listen to location change minimum every meter or minimum 5 seconds
		Utils.locationManager.requestLocationUpdates(Utils.provider, 5000, 1,
				this);
		// check if preference changed
		if (Utils.isPrefChagned) {
			// if dose notify adapter to the change
			adapter.notifyDataSetChanged();
		}
		// check if we are on first run
		if (onStart) {
			// if we are so it can't be true any more set it to false
			onStart = false;
		}

		// create receiver
		ServiceMangementReciver receiver = new ServiceMangementReciver();
		// create filter for miss match letters
		IntentFilter missMatchLetters = new IntentFilter(
				"ariel.evso.places_project.advanced.action.BROADCAST.LOW.LETTERS");
		// create filter for no user location
		IntentFilter unknowLocation = new IntentFilter(
				"ariel.evso.places_project.advanced.action.BROADCAST.UNKNOW.LOCATION");
		// create local broadcast manager
		LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
				.getInstance(getActivity());
		// register receiver
		localBroadcastManager.registerReceiver(receiver, missMatchLetters);
		localBroadcastManager.registerReceiver(receiver, unknowLocation);

	}

	@Override
	public void onPause() {
		super.onPause();
		// remove the updates from the location manager
		Utils.locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// set the new location from update to local variable location
		this.location = location;
		// notify the adapter about that change(so he can draw new view)
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// set the new boolean isChecked value to local variable isChecked, for
		// later use
		this.isChecked = isChecked;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// find edit text view
		EditText et = (EditText) getView().findViewById(R.id.editSearch);
		// get the text from it
		String query = et.getText().toString();
		// save it to shared preferences
		saveToSharedPref(query);

		switch (v.getId()) {
		case R.id.editSearch:
			// start the search
			doTheSearch(query);
			return true;
		}
		return false;
	}

	/**
	 * save the given string to {@link SharedPreferences}
	 * 
	 * @param query
	 *            - text that user enter for search
	 */

	private void saveToSharedPref(String query) {
		// create shared preferences
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		// create editor
		Editor editor = sp.edit();
		// put the search string to field called "query"
		editor.putString("query", query);
		// and now do it
		editor.commit();

	}

}
