package ariel.evso.places_project.advanced.modal;

import android.net.Uri;
import ariel.evso.places_project.advanced.control.db.DbHelper;
import ariel.evso.places_project.advanced.control.providers.AppProvider;

/**
 * a contract class for the AppProvider<br>
 * <br>
 * <h2>available tables:</h2>
 * <ul>
 * <li>Places : a table to hold the latest search results</li>
 * <li>Favorites : a table to hold user favorites places</li>
 * </ul>
 * 
 * @see AppProvider
 * @see DbHelper
 */

public class PlacesContract {

	/** base  AUTHORITY for the provider */
	private final static String AUTHORITY = "ariel.evso.places_project.advanced.providers.Place";

	/** class that provides string values for the columns of places table<br> 
	 * URI value to build the path to your table and of course the table name.<br>
	 *  use <b>{@code PlacesContract.Places}</b><br> 
	 *  to grab the value you want from that class
	 *  @see {@link PlacesContract}
	 *  */
	public static class Places {
		
		/** 
		 * places table
		 * */
		public final static String TABLE_NAME = "places";

		/** 
		 * URI for the places table<br>
		 * <b>content://ariel.evso.places_project.advanced.providers.Place</b>
		 */
		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME);

		// columns
		/** id (type: INTEGER)*/
		public final static String _ID = "_id";
		/** place's name (type: TEXT)*/
		public final static String NAME = "name";
		/** place's address (type: TEXT)*/
		public final static String ADDRESS = "address";
		/** place's latitude (type: DOUBLE)*/
		public final static String lAT = "latitude";
		/** place's longitude (type: DOUBLE)*/
		public final static String lNG = "longitude";
		/** place's rating (type: DOUBLE)*/
		public final static String RATING = "rating";
		/** place's icon URL (type: TEXT)*/
		public final static String IMG_URL = "imgUrl";

	}
	
	/** class that provides string values for the columns of favorites table<br> 
	 * URI value to build the path to your table and of course the table name.<br>
	 *  use <b>{@code PlacesContract.Favorites}</b><br> 
	 *  to grab the value you want from that class
	 *  @see {@link PlacesContract}
	 *  */
	public static class Favorites {

		/** 
		 * favorites table
		 * */
		public final static String TABLE_NAME = "favorites";

		/** 
		 * URI for the places table<br>
		 * <b>content://ariel.evso.places_project.advanced.providers.Place</b>
		 */
		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME);

		// columns
		/** id (type: INTEGER)*/
		public final static String _ID = "_id";
		/** favorites place's name (type: TEXT)*/
		public final static String NAME = "name";
		/** favorites place's address (type: TEXT)*/
		public final static String ADDRESS = "address";
		/** favorites place's longitude (type: DOUBLE)*/
		public final static String lAT = "latitude";
		/** favorites place's longitude (type: DOUBLE)*/
		public final static String lNG = "longitude";
		/** favorites place's rating (type: DOUBLE)*/
		public final static String RATING = "rating";
	}

}
