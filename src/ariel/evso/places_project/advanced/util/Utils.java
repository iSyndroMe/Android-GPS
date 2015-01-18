package ariel.evso.places_project.advanced.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import ariel.evso.places_project.advanced.R;

import com.google.android.gms.maps.model.LatLng;
/**
 * helper class that contains static variables and methods<br> 
 * so we can access them from any where in our code 
 *
 */
public final class Utils {
	
	//private constructor, no reason to allow other classes build this class  
	private Utils() {
		
	}
	/** places loader id */
	public static final int PLACES_LOADER_ID = 1;
	/** favorites loader id */
	public static final int FAVORITES_LOADER_ID = 2;
	/** request code for Google API "text" search */
	public static final int REQUEST_CODE_SEARCH = 1;
	/** request code for Google API "near by" search */
	public static final int REQUEST_CODE_NEARBY = 2;
	/** TYPE[Bitmap] store bitmap */
	public static Bitmap iconPlace;
	/** TYPE[LocationManager] */
	public static LocationManager locationManager;
	/** TYPE[String]*/
	public static String provider;
	/** TYPE[boolean] listen to preference change*/
	public static boolean isPrefChagned = false;
	private static final String TAG = "Utils";
	/**
	 * helper method , received a {@link Activity} and check on which device the APP is running
	 * @param a - {@link Activity}
	 * @return true -if we are on phone <br>
	 *  false - if we are not(probably we are on Tablet or TV)
	 */
	public static boolean isInSingleFragment(Activity a) {
			
		View v = a.findViewById(R.id.phoneFragment);
		if (v != null) {
			// found - we are
			return true;
		} else {
			// not found - we are not.
			return false;
		}
	}
	/**
	 * helper method, that calculate distance between two locations on the planet
	 * @param lat1 - first location latitude
	 * @param lng1 - first location longitude
	 * @param lat2 - second location latitude
	 * @param lng2 - second location longitude
	 * @return - the distance between two locations in <b>kilometers</b>
	 */
	public static double haversine(double lat1, double lng1, double lat2,
			double lng2) {
		int r = 6371; // average radius of the earth in km
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = r * c;
		return d;
	}
	/**
	 * saves the last known location of the user
	 * @param ctx - context
	 * @return - last known location of the user(null if there is no such location)
	 */
	public static Location getLastKnowLocation(Context ctx) {

		locationManager = (LocationManager) ctx.getSystemService(
				Context.LOCATION_SERVICE);

		// get the best location-provider that matches a certain criteria:
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		provider = locationManager.getBestProvider(criteria, true);
		// try to get the last known location with the given provider
		Location lastKnownLocation = locationManager
				.getLastKnownLocation(provider);

		return lastKnownLocation;
	}
	/**
	 * get two location calculate distance between them and set the result inside<br>
	 * requested view  
	 * @param location - the user location
	 * @param loc - place location
	 * @param textDistance - view where you want set the text value
	 * @param ctx - context
	 */
	public static void setLocationText(Location location,LatLng loc,
			TextView textDistance , Context ctx) {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);

		String textShow = prefs.getString("distance", "miels");
		
		if (location != null) {
			double distanceKM = haversine(loc.latitude,
					loc.longitude, location.getLatitude(),
					location.getLongitude());
			
			DecimalFormat decimalFormat = new DecimalFormat("##.##");
			String dst;
			try {
				if (textShow.equals("kilometrs")) {
					dst = decimalFormat.format(distanceKM).toString();
					textDistance.setText(dst + " KM from you");
				} else if (textShow.equals("miels")) {
					double miels = distanceKM * 0.621371192;
					dst = decimalFormat.format(miels).toString();
					textDistance.setText(dst + " Miels from you");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				isPrefChagned = true;
			}
		} else {
			textDistance.setText("UNKNOWN LOCATION");
		}
		
	}
	/**
	 * save images bitmap to internal storage
	 * @param bitmapImage - bitmap of the image you want to save
	 * @param ctx - context
	 * @param fileName - describe how the file will save inside the storage
	 * @return - absolute path to the file 
	 */
	public static String saveToInternalSorage(Bitmap bitmapImage , Context ctx , String fileName){
        ContextWrapper cw = new ContextWrapper(ctx);
         // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, fileName + ".jpg");

        FileOutputStream fos = null;
        try {           

            fos = new FileOutputStream(mypath);

       // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }
	/**
	 * get file name, delete it form storage and make toast that the file was deleted<br>
	 * or "something goes wrong" if not
	 * @param n = name of the file you want to delete
	 * @param ctx - context
	 */
	public static void deleteFromStorage(String n , Context ctx) {
		ContextWrapper cw = new ContextWrapper(ctx);
		try {
			File dir = cw.getDir("imageDir", Context.MODE_PRIVATE);
			File file = new File(dir, n + ".jpg");
			boolean deleted = file.delete();
			
			if(deleted){
				Toast.makeText(ctx, "successfuly deleted", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(ctx, "something goes wrong", Toast.LENGTH_SHORT).show();
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
	
	/**
	 * load image bitmap from internal storage
	 * @param path - path to the file folder
	 * @param fileName - file name you want to load
	 * @return - bitmap(null if not found)
	 */
	public static Bitmap loadImageFromStorage(String path , String fileName){
		
	     try {
	        File f = new File(path, fileName + ".jpg");
	        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
	        return b;
	    } 
	    catch (FileNotFoundException e) 
	    {
	        e.printStackTrace();
	    }
		return null;

	}

	/**
	 * get path to a folder and delete it
	 * @param dir - path to the folder you want to delete
	 * @return - true if success to delete folder<br>
	 * false - if not
	 */
	public static boolean deleteDir(File dir) {
		//if any path exists
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}
	/**
	 * check if Internet WiFi/Network connection is available
	 * @param context
	 * @return true - if we got Internet<br>
	 * false - if not
	 */
	public static boolean isInternetAvailable(Context context) {
			
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (info == null) {
			Log.d(TAG, "no internet connection");
			return false;
		} else {
			if (info.isConnected()) {
				Log.d(TAG, " internet connection available...");
				return true;
			} else {
				Log.d(TAG, " internet connection");
				return true;
			}

		}
	}
}
