package ariel.evso.places_project.advanced.control.providers;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import ariel.evso.places_project.advanced.control.db.DbHelper;
import ariel.evso.places_project.advanced.modal.PlacesContract;

/**
 * content provider for the application's data
 * @see PlacesContract
 */
public class AppProvider extends ContentProvider {

	DbHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		if(dbHelper != null){
			return true;
		}
		return false;
	}
	
	protected String getTableName(Uri uri){
		List<String> pathSegments = uri.getPathSegments();
		return pathSegments.get(0);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
		
		//register the cursor to fire notifications if data is changed:
		//this will ensure the cursor will be re-queried if the data is changed
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long id = db.insertWithOnConflict(getTableName(uri), null, values,SQLiteDatabase.CONFLICT_REPLACE);
		
		/*
		 * NOTE : even though it's a getWritableDatabase - we won't close it here
		 * if we do, we'll raise errors later when trying to re-query the data.
		 * it's OK. it works. 
		 * the db will get closed the provider is closed. 
		 */
		
		// notify the change
		getContext().getContentResolver().notifyChange(uri, null);
		
		// this methods has to return the inserted row's Uri
		// that uri is the given uri (the table uri - content://com.example.demo_providers.provider)
		// and return the specific uri (content://com.example.demo_providers.provider/id)
		// which is just the same uri but with an appended id.
		
		if (id>0){
			return ContentUris.withAppendedId(uri, id);
		}else{
			//or null if nothing was inserted
			return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int result = db.delete(getTableName(uri), selection, selectionArgs);

		/*
		 * NOTE : even though it's a getWritableDatabase - we won't close it here
		 * if we do, we'll raise errors later when trying to re-query the data.
		 * it's OK. it works. 
		 * the db will get closed the provider is closed. 
		 */
		
		// notify the change
		getContext().getContentResolver().notifyChange(uri, null);
		
		//return the number of rows deleted
		//it's what we got from the db.delete
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		//the provided is requested to update data - so we'll update it in the db:
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				int result = db.update(getTableName(uri), values, selection, selectionArgs);

				/*
				 * NOTE : even though it's a getWritableDatabase - we won't close it here
				 * if we do, we'll raise errors later when trying to re-query the data.
				 * it's OK. it works. 
				 * the db will get closed the provider is closed. 
				 */
				
				// notify the change
				getContext().getContentResolver().notifyChange(uri, null);
				
				//return the number of rows changed
				//it's what we got from the db.update. 
				return result;
	}
}
