package ariel.evso.places_project.advanced.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import ariel.evso.places_project.advanced.R;
import ariel.evso.places_project.advanced.modal.Place;

public class GoogleAccess {
	/**
	 * a helper class to perform google API requests over a network.
	 *
	 */
	private final static String API_TEXT = "https://maps.googleapis.com/maps/api/place/textsearch/json";
	private final static String API_NEARBY = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
	private final static String APP_KEY = "AIzaSyAKlr98Fafk6Tj9HsXQy4VFHLRH51Ym9RA";

	/**
	 * build a query string for Google "text" API search <br>
	 * this will use {@link goTotheInternet} method<br>
	 * to send a request to the Google Places API with a search string<br>
	 * and return the result JSON string.<br>
	 * 
	 * @see goTotheInternet
	 * @param q
	 *            - the search string
	 * @return a JSON string with the results from the API<br>
	 *         (or null if something went wrong)
	 */

	public static String searchPlace(String q) {

		String json = null;
		try {
			String queryString = "";
			queryString += "?query=" + URLEncoder.encode(q, "utf-8");
			queryString += "&sensor=false";
			queryString += "&key=" + APP_KEY;

			json = goTotheInternet(API_TEXT, queryString);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;

	}

	/**
	 * build a query string for Google "near by" API search <br>
	 * this will use {@link goTotheInternet} method<br>
	 * to send a request to the Google Places API with a search string<br>
	 * and return the result JSON string.<br>
	 * 
	 * @see goTotheInternet
	 * @param location
	 *            - the user location
	 * @param keyword
	 *            - the search string
	 * @return a JSON string with the results from the API<br>
	 *         (or null if something went wrong)
	 */

	public static String searchNearby(Location location, String keyword , String textRad) {
		
		String json = null;
		try {
			String queryString = "";
			queryString += "?radius=" + textRad;
			if (keyword != null) {
				queryString += "&keyword="
						+ URLEncoder.encode(keyword, "utf-8");
			}
			queryString += "&location=" + location.getLatitude() + ","
					+ location.getLongitude();
			queryString += "&key=" + APP_KEY;

			Log.d("BS no way!!!!!", queryString);

			json = goTotheInternet(API_NEARBY, queryString);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;

	}

	/**
	 * performs a Places API search request<br>
	 * this will send a request to the Google Places API with a search string<br>
	 * and return the result JSON string.<br>
	 * 
	 * @param queryString
	 *            - the search string
	 * @param api
	 *            - the API string
	 * @return a JSON string with the results from the API<br>
	 *         (or null if something went wrong)
	 */

	private static String goTotheInternet(String api, String queryString) {

		BufferedReader input = null;
		HttpURLConnection connection = null;
		StringBuilder response = new StringBuilder();

		try {

			URL url = new URL(api + queryString);

			connection = (HttpURLConnection) url.openConnection();

			// check the result status of the conection:
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// not good
				return null;
			}

			// get the input stream from the connection
			// and make it into a buffered char stream.
			input = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			// read from the buffered stream line by line:
			String line = "";
			while ((line = input.readLine()) != null) {
				// append to the string builder:
				response.append(line + "\n");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			// close the stream if it exists:
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// close the connectin if it exists:
			if (connection != null) {
				connection.disconnect();
			}
		}

		return response.toString();
	}

	/**
	 * send a request to the Internet and get the image from URL<br>
	 * that have been provide to it<br>
	 * 
	 * @param imgUrl
	 *            - the image URL string
	 * @return bitmap (null if there is no image on provided URL)
	 */
	private static Bitmap getIcon(String imgUrl) {

		String address = imgUrl;

		HttpURLConnection connection = null;
		InputStream stream = null;
		ByteArrayOutputStream outputStream = null;

		// the bitmap will go here:
		Bitmap b = null;

		try {
			// build the URL:
			URL url = new URL(address);
			// open a connection:
			connection = (HttpURLConnection) url.openConnection();

			// check the connection response code:
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// not good..
				return null;
			}

			// the input stream:
			stream = connection.getInputStream();

			// get the length:
			int length = connection.getContentLength();
			// if you have a progress dialog - :
			// tell the progress dialog the length:
			// this CAN (!!) be modified outside the UI thread !!!
			// progressDialog.setMax(length);

			// a stream to hold the read bytes.
			// (like the StringBuilder we used before)
			outputStream = new ByteArrayOutputStream();

			// a byte buffer for reading the stream in 1024 bytes chunks:
			byte[] buffer = new byte[1024];

			int totalBytesRead = 0;
			int bytesRead = 0;

			// read the bytes from the stream
			while ((bytesRead = stream.read(buffer, 0, buffer.length)) != -1) {
				totalBytesRead += bytesRead;
				outputStream.write(buffer, 0, bytesRead);

				// if you want - you can notify
				// the UI thread on the progress so far:
				// publishProgress(totalBytesRead);
				Log.d("TAG", "progress: " + totalBytesRead + " / " + length);
			}

			// flush the output stream - write all the pending bytes in its
			// internal buffer.
			outputStream.flush();

			// get a byte array out of the outputStream
			// theses are the bitmap bytes
			byte[] imageBytes = outputStream.toByteArray();

			// use the BitmapFactory to convert it to a bitmap
			b = BitmapFactory.decodeByteArray(imageBytes, 0, length);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				// close connection:
				connection.disconnect();
			}
			if (outputStream != null) {
				try {
					// close output stream:
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return b;
	}

	/**
	 * asynchronous class that uses {@link getIcon} method to get images in
	 * asynchronous way<br>
	 * <strong>*NOTE*</strong> when calling it the constructor will ask for
	 * {@link Place} object<br>
	 * and {@link View} , this is for inside usage to set images to the right
	 * image view
	 * 
	 * @see getIcon
	 * @see Place
	 */

	public static class TaskGetIcon extends AsyncTask<Void, Void, Bitmap> {

		private Place place;
		private View v;

		public TaskGetIcon(View v, Place place) {
			this.v = v;
			this.place = place;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap b = GoogleAccess.getIcon(place.getIcon());
			return b;
		}

		@Override
		protected void onPostExecute(Bitmap result) {

			ImageView imageView = (ImageView) v
					.findViewById(R.id.imageViewIcon);

			Object tag = v.getTag();
			if (tag != null && tag.equals(place)) {

				// hide progress, show image
				imageView.setVisibility(View.VISIBLE);

				// display image (if it's null - display a default image).
				if (result != null) {
					imageView.setImageBitmap(result);
					Utils.iconPlace = result;
				} else {
					// well... nothing for now
				}
			}

		}
	}

}
