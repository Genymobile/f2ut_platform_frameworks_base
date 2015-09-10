package com.fairphone.programmablebutton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.widget.Toast;

public class CameraButtonReceiver extends BroadcastReceiver {
	public CameraButtonReceiver() {}

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		String intent_action = preferences.getString(ProgrammableButton.CAMERA_BUTTON_INTENT_PREF, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		if (!intent_action.equals(ProgrammableButton.ACTION_IGNORE)){
			Intent launch = new Intent(intent_action);
			launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (intent.resolveActivity(context.getPackageManager()) != null) {
				context.startActivity(launch);
				abortBroadcast();
			}
		}
	}
}
