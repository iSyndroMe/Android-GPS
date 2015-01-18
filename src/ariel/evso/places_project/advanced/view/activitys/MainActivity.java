package ariel.evso.places_project.advanced.view.activitys;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import ariel.evso.places_project.advanced.R;
import ariel.evso.places_project.advanced.control.receivers.PowerConnectionReceiver;
import ariel.evso.places_project.advanced.modal.Place;
import ariel.evso.places_project.advanced.util.Utils;
import ariel.evso.places_project.advanced.view.fragments.FragList;
import ariel.evso.places_project.advanced.view.fragments.FragList.ListFragmentListener;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;

public class MainActivity extends ActionBarActivity implements
		ListFragmentListener {
	
	PowerConnectionReceiver receiver;

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//prepare boolean that checks running on phone or not
		boolean singleFragment = Utils.isInSingleFragment(this);
		Fragment fragment;
		//check if the bundle is not empty and if we running on phone or not
		if (singleFragment && savedInstanceState == null) {
			//create fragment list 
			fragment = FragList.newInstance();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragContainer, fragment, "list").commit();
		} else {
			//we are not on phone, so just check if the bundle is not empty
			if (savedInstanceState == null) {
				//create fragment list
				fragment = FragList.newInstance();
				getSupportFragmentManager().beginTransaction()
						.add(R.id.fragmnet_container_list, fragment, "list")
						.commit();
			}
		}
	}
	
	@Override
	public void onPlaceSelected(Place place) {
		//get fragment manager
		FragmentManager fm = getSupportFragmentManager();
		//prepare boolean that checks running on phone or not
		boolean singleFragment = Utils.isInSingleFragment(this);
		//check if we running on phone or not
		if (singleFragment) {
			//begin fragment transaction
			FragmentTransaction ft = fm.beginTransaction();
			//create options for the map
			GoogleMapOptions options = new GoogleMapOptions();
			options.mapType(GoogleMap.MAP_TYPE_NORMAL);

			// - initial location - the place
			options.camera(CameraPosition.fromLatLngZoom(place.getLocation(),
					12));

			// now - create the map fragment with the options:
			// we're using the newInstance(options) method - it's a factory
			// method for creating a mapFragmnet.
			SupportMapFragment mapFragment = SupportMapFragment
					.newInstance(options);
			//set the map fragment to the container
			ft.replace(R.id.fragContainer, mapFragment, "map");
			ft.addToBackStack(null);
			ft.commit();
		} else {
			Fragment testDetails = fm.findFragmentByTag("map");

			if (testDetails == null) {

				FragmentTransaction ft = fm.beginTransaction();
				// options for the created map fragment:
				GoogleMapOptions options = new GoogleMapOptions();

				// - map type
				options.mapType(GoogleMap.MAP_TYPE_NORMAL);

				// - initial location - the place
				options.camera(CameraPosition.fromLatLngZoom(
						place.getLocation(), 12));

				// now - create the map fragment with the options:
				// we're using the newInstance(options) method - it's a factory
				// method for creating a mapFragmnet.
				SupportMapFragment mapFragment = SupportMapFragment
						.newInstance(options);
				//set map animation
				ft.setCustomAnimations(android.R.anim.fade_in,
						android.R.anim.fade_out);
				//set the map fragment to the container
				ft.replace(R.id.fragmnet_container_map, mapFragment, "map");
				ft.commit();
				
			} else {
				SupportMapFragment mapFragment = (SupportMapFragment) fm
						.findFragmentByTag("map");

				// get the map object from the map fragment:
				GoogleMap map = mapFragment.getMap();

				// the map camera update:
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
						place.getLocation(), 12);
				map.animateCamera(update);
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		//create new charging receiver, its here and not inside the fragment list because
		//i want listen to changes inside the map too 
		receiver = new PowerConnectionReceiver();
		//create filter to listen to battery changes
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		//register the receiver
		registerReceiver(receiver, ifilter);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	//the option menu here, also because i want it inside the map 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.action_settings:
			//open the PrefsActivity
			intent = new Intent(this, PrefsActivity.class);
			startActivity(intent);

			break;

		case R.id.action_favorite:
			//open FavoriteActivity
			intent = new Intent(this, FavoriteActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
		return true;
	}

}
