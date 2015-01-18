package ariel.evso.places_project.advanced.view.fragments;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import ariel.evso.places_project.advanced.R;
import ariel.evso.places_project.advanced.modal.Place;
import ariel.evso.places_project.advanced.modal.PlacesContract;
import ariel.evso.places_project.advanced.util.Utils;

import com.google.android.gms.maps.model.LatLng;

public class FragFavorites extends Fragment implements LoaderCallbacks<Cursor>,
		LocationListener {

	FavoritesAdapter adapter;
	private Location location;

	/**
	 * factory method to create fragment
	 * 
	 * @return - favorites fragment list
	 */
	public static FragFavorites newInstance() {

		FragFavorites f = new FragFavorites();

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_favorite, container, false);
		// get the loader manager and set to it your loader id
		getLoaderManager().initLoader(Utils.FAVORITES_LOADER_ID, null, this);
		// set last know location to local variable location
		this.location = Utils.getLastKnowLocation(getActivity());
		// create the favorites table adapter
		adapter = new FavoritesAdapter(getActivity(), null);
		// get list view
		ListView lv = (ListView) v.findViewById(R.id.listViewFav);
		// set the adapter to the list
		lv.setAdapter(adapter);
		// register the list for context menu
		registerForContextMenu(lv);
		// return the view
		return v;
	}

	/**
	 * class that get values from data base and set them to views<br>
	 * his main purpose is simple to drawing the view
	 */
	class FavoritesAdapter extends CursorAdapter {

		public FavoritesAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public void bindView(View v, Context ctx, Cursor c) {
			// get the place data base id from favorites table
			long id = c.getLong(c.getColumnIndex(PlacesContract.Favorites._ID));
			// get standard table name
			String tableName = PlacesContract.Favorites.TABLE_NAME;
			// use build place object method to build a place object
			Place place = Place.buildPlaceObject(id, ctx, tableName);
			
			LatLng loc = null;
			String name = null;
			String address = null;
			try {
				// get the location
				loc = place.getLocation();
				// get the name
				name = place.getName();
				// get the address
				address = place.getAddress();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// find views
			TextView textName = (TextView) v.findViewById(R.id.textName);
			TextView textAddress = (TextView) v.findViewById(R.id.textAddres);
			TextView textDistance = (TextView) v
					.findViewById(R.id.textDistance);
			ImageView imgView = (ImageView) v.findViewById(R.id.imageViewIcon);

			RatingBar rb = (RatingBar) v.findViewById(R.id.ratingBar);
			// check if the rating is not null
			try {
				if (String.valueOf(place.getRating()) != null) {
					// if is not set it to the view
					rb.setRating((float) place.getRating());
				}
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// create file
			File file = getActivity().getDir("imageDir", Context.MODE_PRIVATE);
			// convert file to path string
			String path = file.toString();
			// load place icon image from storage
			Bitmap bitmap = null;
			try {
				bitmap = Utils.loadImageFromStorage(path, place.getName());
				// set the distance text
				Utils.setLocationText(location, loc, textDistance, ctx);
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// set some other text's
			textName.setText(name);
			textAddress.setText(address);
			imgView.setImageBitmap(bitmap);

		}

		@Override
		public View newView(Context ctx, Cursor c, ViewGroup parent) {
			// inflate the layout
			return getActivity().getLayoutInflater().inflate(
					R.layout.places_list, parent, false);
		}

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
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		//create a loader:
		return new CursorLoader(getActivity(),
				PlacesContract.Favorites.CONTENT_URI, null, null, null, null);
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
	public void onLocationChanged(Location location) {
		this.location = location;
		// notify adapter to the location change so he draw the view again
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

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		//inflate context menu
		getActivity().getMenuInflater().inflate(R.menu.context_menu_favorites,
				menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//get the adapter context menu info
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		//get id from that info
		long id = info.id;
		//build place object
		Place p = Place.buildPlaceObject(id, getActivity(), PlacesContract.Favorites.TABLE_NAME);
		//URI favorites table
		Uri uri = PlacesContract.Favorites.CONTENT_URI;
		//select place id from favorites table
		String where = PlacesContract.Favorites._ID + " =?";
		//check if the item id is equals to data base place id 
		String[] selectionArgs = new String[] { String.valueOf(id) };
		

		switch (item.getItemId()) {
		case R.id.action_delete:
			//delete selected item from data base
			getActivity().getContentResolver()
					.delete(uri, where, selectionArgs);
			Utils.deleteFromStorage(p.getName(), getActivity());
			break;

		}
		//handled
		return true;
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//remove the updates from the location manager
		Utils.locationManager.removeUpdates(this);
	}

}
