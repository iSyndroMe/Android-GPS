package ariel.evso.places_project.advanced.view.activitys;

import com.google.analytics.tracking.android.EasyTracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import ariel.evso.places_project.advanced.R;
import ariel.evso.places_project.advanced.view.fragments.FragFavorites;

public class FavoriteActivity extends ActionBarActivity {

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite);
		//check if bundle is not empty
		if (savedInstanceState == null) {
			//create favorites fragment and set it to the container
			Fragment fragment = FragFavorites.newInstance();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragContainer, fragment, "listFavorites").commit();
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//inflate menu XML
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
			//open the FavoriteActivity
			intent = new Intent(this, FavoriteActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
		return true;
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
}
