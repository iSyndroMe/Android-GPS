package ariel.evso.places_project.advanced.control.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
/**
 * class that helps to handle {@link SearchPlacesService} errors 
 */
public class ServiceMangementReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//get the action of the calling intent
		String action = intent.getAction();
		//check the actions, and make matched toast to the error type
		if (action
				.equals("ariel.evso.places_project.advanced.action.BROADCAST.LOW.LETTERS")) {
			Toast.makeText(context, "Add at least 2 latters",
					Toast.LENGTH_SHORT).show();
		} else if (action
				.equals("ariel.evso.places_project.advanced.action.BROADCAST.UNKNOW.LOCATION")) {
			Toast.makeText(
					context,
					"Could not find your location , check if your GPS is turned on",
					Toast.LENGTH_SHORT).show();

		} 

	}

}
