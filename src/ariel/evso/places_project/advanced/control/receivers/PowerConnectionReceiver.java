package ariel.evso.places_project.advanced.control.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;
/**
 * helper class , listen to changes of the device power connection
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//get the action of the calling intent
		String action = intent.getAction();

		//we charging???
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		//how we charging
		int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		//check if we are charging and make toast
		if (isCharging) {
			if (usbCharge) {
				Toast.makeText(context, "YAY! we are charging through USB :)",
						Toast.LENGTH_SHORT).show();
			} else if (acCharge) {
				Toast.makeText(context, "YAY! we are charging through ACC :)",
						Toast.LENGTH_SHORT).show();
			}
		}

		//we are disconnected form charge :(
		if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
			Toast.makeText(context, "You took my power i'm weak :(",
					Toast.LENGTH_SHORT).show();
		}
	}
}
