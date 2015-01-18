package ariel.evso.places_project.advanced.modal;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import ariel.evso.places_project.advanced.util.Utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * 
 * helper class that describe a place<br>
 * also can build place object from table inside database using
 * {@link buildPlaceObject} method<br>
 * or parse JSON data and put them inside {@link ContentValues} using
 * {@link toContentVal} <h2>constructor expect to get:</h2>
 * <ul>
 * <li>name type(String)</li>
 * <li>address type(String)</li>
 * <li>location type(LatLng)</li>
 * <li>rating type(double)</li>
 * <li>icon type(String)</li>
 * </ul>
 *
 */
public class Place {

	private long id;
	private String name;
	private String address;
	private LatLng location;
	private double rating;
	private String icon;

	/**
	 * place constructor , build for you a place object
	 * 
	 * @param name
	 *            - place name type(String)
	 * @param address
	 *            - place address type(String)
	 * @param location
	 *            - place location type(LatLng)
	 * @param rating
	 *            - place rating type(double)
	 * @param icon
	 *            - place icon type(String)
	 */
	public Place(String name, String address, LatLng location, double rating,
			String icon) {
		this.setName(name);
		this.setAddress(address);
		this.setLocation(location);
		this.setRating(rating);
		this.setIcon(icon);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * helper method that define how we want to compare place object<br>
	 * it simple check's if places have same name , address , and location.<br>
	 * nothing so special
	 * 
	 * @param obj
	 *            - place object
	 * @return - return true if the places are equals<br>
	 *         false if not
	 */
	public boolean isEquals(Object obj) {
		if (obj instanceof Place){
			Place otherPlace = (Place)obj;
			boolean nameMatch = this.name.equalsIgnoreCase(otherPlace.name);
			boolean addressMatch = this.address.equalsIgnoreCase(otherPlace.address);
			//dosne't work with the location check, but place name and address should be good enough
			//boolean locationMatch = this.location == otherPlace.location;
			return (nameMatch && addressMatch); 
		}else{
			return false;
		}
	}

	/**
	 * receive JSON object, parse it and put them inside {@link ContentValeus}<br>
	 * after all that you will get back values of <b>one</b> place object back
	 * <h2>REQUEST CODE OPTEIONS:</2>
	 * <ul>
	 * <li>{@code Utils.REQUEST_CODE_SEARCH}</li>
	 * <li>{@code Utils.REQUEST_CODE_NEARBY}</li>
	 * </ul>
	 * 
	 * @see {@link Utils}
	 * @param obj
	 *            - one JSON object
	 * @param typeCode
	 *            - the request type code for the search type
	 * @return {@link ContentValeus} of the received object, that match the
	 *         (null if noting was found)
	 */
	public static ContentValues toContentVal(JSONObject obj, int typeCode) {

		String name;
		String address;
		String imgUrl;
		double lat;
		double lng;
		double rat;
		ContentValues values = new ContentValues();

		try {
			name = obj.getString("name");

			if (typeCode == Utils.REQUEST_CODE_SEARCH) {
				try {
					address = obj.getString("formatted_address");
					values.put(PlacesContract.Places.ADDRESS, address);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (typeCode == Utils.REQUEST_CODE_NEARBY) {
				try {
					address = obj.getString("vicinity");
					values.put(PlacesContract.Places.ADDRESS, address);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				rat = obj.getDouble("rating");
				values.put(PlacesContract.Places.RATING, rat);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			imgUrl = obj.getString("icon");
			lat = obj.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lat");
			lng = obj.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lng");

			values.put(PlacesContract.Places.NAME, name);
			values.put(PlacesContract.Places.lAT, lat);
			values.put(PlacesContract.Places.lNG, lng);
			values.put(PlacesContract.Places.IMG_URL, imgUrl);

			return values;
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return null;

	}

	/**
	 * helper method that build for you complete place object from the requested database table<br>
	 * @param id
	 *            - the id of the requested place object you want to build
	 * @param ctx
	 *            - context
	 * @param whichTable
	 *            - name of the table that you want build object from, use one
	 *            of the following<br>
	 *            tables that provided by {@link PlacesContract}
	 * @see {@link PlacesContract}
	 * @return complete {@link Place} object (null if it not found)
	 */
	public static Place buildPlaceObject(long id, Context ctx, String whichTable) {
		if (whichTable.equals(PlacesContract.Places.TABLE_NAME)) {
			Place p = fromPlacesTable(id, ctx);
			return p;
		} else if (whichTable.equals(PlacesContract.Favorites.TABLE_NAME)) {
			Place p = fromFavorietsTable(id, ctx);
			return p;
		}
		return null;
	}

	/**
	 * helper method , that build parameters for {@link Place} object form
	 * <b>favorites</b> table
	 * 
	 * @see {@link PlacesContract}
	 * @param id
	 *            - the id of the requested place object you want to build
	 * @param ctx
	 *            - context
	 * @return complete {@link Place} object
	 */
	private static Place fromFavorietsTable(long id, Context ctx) {
		Uri uri = PlacesContract.Favorites.CONTENT_URI;

		String sel = PlacesContract.Favorites._ID;

		String[] params = { PlacesContract.Favorites.NAME,
				PlacesContract.Favorites.ADDRESS, PlacesContract.Favorites.lAT,
				PlacesContract.Favorites.lNG, PlacesContract.Favorites.RATING,
				null };

		Place p = getTable(id, ctx, uri, sel, params);
		return p;
	}

	/**
	 * helper method , that build parameters for {@link Place} object form
	 * <b>places</b> table
	 * 
	 * @see {@link PlacesContract}
	 * @param id
	 *            - the id of the requested place object you want to build
	 * @param ctx
	 *            - context
	 * @return complete {@link Place} object
	 */
	private static Place fromPlacesTable(long id, Context ctx) {

		Uri uri = PlacesContract.Places.CONTENT_URI;

		String sel = PlacesContract.Places._ID;

		String[] params = { PlacesContract.Places.NAME,
				PlacesContract.Places.ADDRESS, PlacesContract.Places.lAT,
				PlacesContract.Places.lNG, PlacesContract.Favorites.RATING,
				PlacesContract.Places.IMG_URL };

		Place p = getTable(id, ctx, uri, sel, params);
		return p;
	}

	/**
	 * helper method , that goes to the data base and build you place object
	 * with the requested parameters
	 * 
	 * @param id
	 *            - the id of the requested place object you want to build
	 * @param ctx
	 *            - context
	 * @param uri
	 *            - the URI path for the requested table
	 * @param sel
	 *            - selection, it should be the place id in the data base <br>
	 *            example:<br>
	 *            <b>{@code PlacesContract.Places._ID}</b> or <b>
	 *            {@code PlacesContract.Favorites._ID}</b>
	 * @param params
	 *            - the columns string values <br>
	 *            <b>initialize parameters in the same pattern as following
	 *            code:</b><br>
	 *            <b>{@code String[] params = name, address,
	 *            latitude,longitude,imgUrl };</b><br>
	 *            of course use the string name's provided by
	 *            {@link PlacesContract}<br>
	 *            <b>*NOTE* for the favorites table send "null" as a imgUrl
	 *            value </b>
	 * @return complete {@link Place} object (null if not found)
	 */
	private static Place getTable(long id, Context ctx, Uri uri, String sel,
			String[] params) {

		// check that we received valid id number, no one can have negative
		// value of id
		if (id != -1) {

			// build the selection with the data base object id
			String selection = sel + " = ?";
			// check if the id we received equals to the id in our data base
			String[] selectionArgs = new String[] { String.valueOf(id) };

			// build cursor
			Cursor c = ctx.getContentResolver().query(uri, null, selection,
					selectionArgs, null);

			// run over all the parameters
			if (c.moveToNext()) {
				String icon = null;
				double rating = 0;
				// get all columns
				String name = c.getString(c.getColumnIndex(params[0]));
				String address = c.getString(c.getColumnIndex(params[1]));
				double lat = c.getDouble(c.getColumnIndex(params[2]));
				double lng = c.getDouble(c.getColumnIndex(params[3]));
				try {
					rating = c.getDouble(c.getColumnIndex(params[4]));
					icon = c.getString(c.getColumnIndex(params[5]));
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				// convert latitude,longitude to location object
				LatLng location = new LatLng(lat, lng);
				// create full place object
				Place place = new Place(name, address, location, rating, icon);
				place.setId(id);
				// Bubble up to the activity/fragment who ever call us...
				return place;
			}

			// close the cursor
			c.close();
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

}
