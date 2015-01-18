package ariel.evso.places_project.advanced.control.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ariel.evso.places_project.advanced.control.providers.AppProvider;
import ariel.evso.places_project.advanced.modal.PlacesContract;

/**
 * a helper class to create / update the database <br>
 * the data itself will be available via the the {@link AppProvider} class<br>
 * @see AppProvider
 * @see PlacesContract
 *
 */

public class DbHelper extends SQLiteOpenHelper {

	/** the current database version */
	private static final int DB_VERSION = 1;
	/** the database file name */
	private static final String DB_NAME = "places.db";

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		createPlacesTable(db);
		crateFavoritsTable(db);

	}

	/**
	 * create favorites table
	 * @param db - {@link SQLiteDatabase}
	 */
	private void crateFavoritsTable(SQLiteDatabase db) {
		// create favorites table:
		String sql = "CREATE TABLE " + PlacesContract.Favorites.TABLE_NAME + "("
				+ PlacesContract.Favorites._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
				+ PlacesContract.Favorites.NAME + " TEXT ,"
				+ PlacesContract.Favorites.ADDRESS + " TEXT ,"
				+ PlacesContract.Favorites.lAT + " DOUBLE ,"
				+ PlacesContract.Favorites.lNG + " DOUBLE ,"
				+ PlacesContract.Favorites.RATING + " DOUBLE "
				+ ")";

		db.execSQL(sql);
	}
	/**
	 * create places table
	 * @param db - {@link SQLiteDatabase}
	 */
	private void createPlacesTable(SQLiteDatabase db) {
		// create places table:
		String sql = "CREATE TABLE " + PlacesContract.Places.TABLE_NAME + "("
				+ PlacesContract.Places._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
				+ PlacesContract.Places.NAME + " TEXT ,"
				+ PlacesContract.Places.ADDRESS + " TEXT ,"
				+ PlacesContract.Places.lAT + " DOUBLE ,"
				+ PlacesContract.Places.lNG + " DOUBLE ,"
				+ PlacesContract.Places.RATING + " DOUBLE ,"
				+ PlacesContract.Places.IMG_URL + " TEXT "
				+ ")";

		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// delete the tables:
		String sql = "DROP TABLE IF EXISTS " + PlacesContract.Places.TABLE_NAME;
		db.execSQL(sql);
		//recreate the tables:
		onCreate(db);

	}
}
