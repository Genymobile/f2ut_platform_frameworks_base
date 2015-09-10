package com.fairphone.programmablebutton;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class ProgrammableButton extends Activity {

	public static final String ACTION_IGNORE = "com.fairphone.programmablebutton.ACTION_IGNORE";
	public static final String CAMERA_BUTTON_INTENT_PREF = "CameraButtonIntent";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		String intentAction = preferences.getString(CAMERA_BUTTON_INTENT_PREF, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);

		TextView currentIntent = (TextView) findViewById(R.id.currentIntent);
		currentIntent.setText(intentAction);

		ListView intentList = (ListView) findViewById(R.id.intentList);

		String[] items = {
				ACTION_IGNORE,
				MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA,
				MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE,
				MediaStore.INTENT_ACTION_VIDEO_CAMERA,
				Settings.ACTION_SETTINGS,
				Settings.ACTION_WIRELESS_SETTINGS,
				Settings.ACTION_AIRPLANE_MODE_SETTINGS,
				Settings.ACTION_WIFI_SETTINGS,
				Settings.ACTION_APN_SETTINGS,
				Settings.ACTION_BLUETOOTH_SETTINGS,
				Settings.ACTION_DATE_SETTINGS,
				Settings.ACTION_LOCALE_SETTINGS,
				Settings.ACTION_INPUT_METHOD_SETTINGS,
				Settings.ACTION_DISPLAY_SETTINGS,
				Settings.ACTION_SECURITY_SETTINGS,
				Settings.ACTION_LOCATION_SOURCE_SETTINGS,
				Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
				Settings.ACTION_MEMORY_CARD_SETTINGS,
		};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				R.layout.intent_list_item, items);

		intentList.setAdapter(adapter);
		intentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String intentAction = ((String)parent.getItemAtPosition(position));
				TextView currentIntent = (TextView) findViewById(R.id.currentIntent);
				SharedPreferences.Editor preferencesEditor = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit();
				preferencesEditor.putString(CAMERA_BUTTON_INTENT_PREF, ((String) parent.getItemAtPosition(position)));
				preferencesEditor.commit();
				currentIntent.setText(intentAction);
			}
		});
	}

}
