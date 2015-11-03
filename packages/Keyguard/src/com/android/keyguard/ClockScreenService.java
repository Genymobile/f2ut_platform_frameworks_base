package com.android.keyguard;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

//import com.fairphone.clock.widget.ClockWidget;

public class ClockScreenService extends Service
{

	private static final String TAG = ClockScreenService.class.getSimpleName();

	public static final String ACTION_ALARM_CHANGED_V18 = "android.intent.action.ALARM_CHANGED";
	public static final String ACTION_ALARM_CHANGED = "android.app.action.NEXT_ALARM_CLOCK_CHANGED";
	public static final String ACTION_CLOCK_UPDATE = "com.fairphone.clock.widget.ClockWidget.CLOCK_UPDATE";

	public static final long MINUTES_IN_MILIS = 60000L;

	private PendingIntent clockUpdateIntent;
	private BroadcastReceiver mBroadcastReceiver;

	private long mScreenOffTimestamp = -1;

	public ClockScreenService()
	{
		Log.d(TAG, "ClockScreenService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.d(TAG, "onStartCommand");
		super.onStartCommand(intent, flags, startId);

		setupAMPMManager();
		setupBroadcastReceiver();

		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy");
		super.onDestroy();

		unregisterReceiver(mBroadcastReceiver);
		clearAMPMManager();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

// --Commented out by Inspection START (26/08/15 20:57):
//	private static String getBatteryStatusAsString(int status) {
//		String desc = "Unknown: ";
//		switch (status) {
//			case BatteryManager.BATTERY_STATUS_CHARGING:
//				desc = "BATTERY_STATUS_CHARGING";
//				break;
//			case BatteryManager.BATTERY_STATUS_FULL:
//				desc = "BATTERY_STATUS_FULL";
//				break;
//			case BatteryManager.BATTERY_STATUS_DISCHARGING:
//				desc = "BATTERY_STATUS_DISCHARGING";
//				break;
//			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
//				desc = "BATTERY_STATUS_NOT_CHARGING";
//				break;
//			case BatteryManager.BATTERY_STATUS_UNKNOWN:
//				desc = "BATTERY_STATUS_UNKNOWN";
//				break;
//			default:
//				desc += status;
//				break;
//		}
//		return desc;
//	}
// --Commented out by Inspection STOP (26/08/15 20:57)

	private void setupBroadcastReceiver()
	{
		mBroadcastReceiver = new BroadcastReceiver()
		{

			@Override
			public void onReceive(Context context, Intent intent)
			{
				// lets sort this by occurrence
				if (intent == null)
				{
					return;
				}
				String action = intent.getAction();
				if (action.equals(ACTION_CLOCK_UPDATE))
				{
					FairphoneClockData.sendUpdateBroadcast(context);
				}
				else if (action.equals(Intent.ACTION_BATTERY_CHANGED))
				{
					int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
					int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
					int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
					FairphoneClockData.updateBatteryPreferences(context, level, status, scale);
					FairphoneClockData.sendUpdateBroadcast(context);
				}
				else if (action.equals(Intent.ACTION_SCREEN_OFF))
				{
					mScreenOffTimestamp = System.currentTimeMillis();
				}
				else if (action.equals(Intent.ACTION_SCREEN_ON))
				{
					if (mScreenOffTimestamp != -1)
					{
						long pomInMinutes = (System.currentTimeMillis() - mScreenOffTimestamp) / MINUTES_IN_MILIS;
						FairphoneClockData.setPeaceOfMindCurrent(context, pomInMinutes);
						long pomRecord = FairphoneClockData.getPeaceOfMindRecord(context);
						if (pomInMinutes > pomRecord)
						{
							FairphoneClockData.setPeaceOfMindRecord(context, pomInMinutes);
						}
					}
					// always update
					FairphoneClockData.sendUpdateBroadcast(context);
				}
				else if (action.equals(ACTION_ALARM_CHANGED) ||
                                                action.equals(ACTION_ALARM_CHANGED_V18)) {
				}
				else if (action.equals(Intent.ACTION_TIME_CHANGED) ||
						action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                                                action.equals(Intent.ACTION_LOCALE_CHANGED))
				{
					mScreenOffTimestamp = -1; // invalidate peace of mind if time changes
					FairphoneClockData.sendUpdateBroadcast(context);
				}
			}
		};
		IntentFilter actions = new IntentFilter(ACTION_ALARM_CHANGED);
		actions.addAction(ACTION_ALARM_CHANGED_V18);
		actions.addAction(ACTION_CLOCK_UPDATE);
		actions.addAction(Intent.ACTION_TIME_CHANGED);
		actions.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		actions.addAction(Intent.ACTION_LOCALE_CHANGED);
		actions.addAction(Intent.ACTION_BATTERY_CHANGED);
		actions.addAction(Intent.ACTION_SCREEN_OFF);
		actions.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mBroadcastReceiver, actions);
	}

	private PendingIntent getUpdateIntent()
	{
		if (clockUpdateIntent == null)
		{
			Intent intent = new Intent(ACTION_CLOCK_UPDATE);
			clockUpdateIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		return clockUpdateIntent;
	}

	private void setupAMPMManager()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.MINUTE, 60 - calendar.get(Calendar.MINUTE));
		calendar.add(Calendar.SECOND, 60 - calendar.get(Calendar.SECOND));
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 3600000, getUpdateIntent());
	}


	private void clearAMPMManager()
	{
		if (clockUpdateIntent != null)
		{
			AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(getUpdateIntent());
		}
	}
}
