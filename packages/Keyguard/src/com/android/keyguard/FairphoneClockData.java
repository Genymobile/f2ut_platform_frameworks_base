package com.android.keyguard;

import com.android.internal.app.IBatteryStats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryStats;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by rrocha on 8/25/15.
 */
public class FairphoneClockData
{
	private static final String TAG = FairphoneClockData.class.getSimpleName();
	
	private static final String FAIRPHONE_CLOCK_PREFERENCES = "com.fairphone.clock.FAIRPHONE_CLOCK_PREFERENCES";
	private static final String PREFERENCE_BATTERY_LEVEL = "com.fairphone.clock.PREFERENCE_BATTERY_LEVEL";
	private static final String PREFERENCE_ACTIVE_LAYOUT = "com.fairphone.clock.PREFERENCE_ACTIVE_LAYOUT";
	private static final String PREFERENCE_BATTERY_STATUS = "com.fairphone.clock.PREFERENCE_BATTERY_STATUS";
	private static final String PREFERENCE_POM_CURRENT = "com.fairphone.clock.PREFERENCE_POM_CURRENT";
	private static final String PREFERENCE_POM_RECORD = "com.fairphone.clock.PREFERENCE_POM_RECORD";
	private static final String PREFERENCE_YOUR_FAIRPHONE_SINCE = "com.fairphone.clock.PREFERENCE_YOUR_FAIRPHONE_SINCE";
	private static final String PREFERENCE_BATTERY_CHANGED_TIMESTAMP = "com.fairphone.clock.PREFERENCE_BATTERY_CHANGED_TIMESTAMP";
	private static final String PREFERENCE_BATTERY_TIME_UNTIL_DISCHARGED = "com.fairphone.clock.PREFERENCE_BATTERY_TIME_UNTIL_DISCHARGED";
	private static final String PREFERENCE_BATTERY_TIME_UNTIL_CHARGED = "com.fairphone.clock.PREFERENCE_BATTERY_TIME_UNTIL_CHARGED";

	public static final String VIEW_UPDATE = "com.fairphone.fairphoneclock.UPDATE";

	private static SharedPreferences getSharedPrefs(Context context)
	{
		return context.getSharedPreferences(FAIRPHONE_CLOCK_PREFERENCES, Context.MODE_PRIVATE);
	}

	public static long getPeaceOfMindCurrent(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_POM_CURRENT, 0);
	}

	public static void setPeaceOfMindCurrent(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_POM_CURRENT, value).commit();
	}

	public static long getPeaceOfMindRecord(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_POM_RECORD, 0);
	}

	public static void setPeaceOfMindRecord(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_POM_RECORD, value).commit();
	}

	public static int getBatteryLevel(Context context)
	{
		return getSharedPrefs(context).getInt(PREFERENCE_BATTERY_LEVEL, 0);
	}

	public static void setBatteryLevel(Context context, int value)
	{
		getSharedPrefs(context).edit().putInt(PREFERENCE_BATTERY_LEVEL, value).commit();
	}

	public static int getBatteryStatus(Context context)
	{
		return getSharedPrefs(context).getInt(PREFERENCE_BATTERY_STATUS, 0);
	}

	public static void setBatteryStatus(Context context, int value)
	{
		getSharedPrefs(context).edit().putInt(PREFERENCE_BATTERY_STATUS, value).commit();
	}

	public static long getBatteryTimeUntilCharged(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_BATTERY_TIME_UNTIL_CHARGED, 0);
	}

	public static void setBatteryTimeUntilCharged(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_BATTERY_TIME_UNTIL_CHARGED, value).commit();
	}

	public static long getBatteryTimeUntilDischarged(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_BATTERY_TIME_UNTIL_DISCHARGED, 0);
	}

	public static void setBatteryTimeUntilDischarged(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_BATTERY_TIME_UNTIL_DISCHARGED, value).commit();
	}

	public static int getCurrentLayoutIdx(Context context)
	{
		return getSharedPrefs(context).getInt(PREFERENCE_ACTIVE_LAYOUT, 0);
	}

	public static void setCurrentLayoutIdx(Context context, int value)
	{
		getSharedPrefs(context).edit().putInt(PREFERENCE_ACTIVE_LAYOUT, value).commit();
	}

	public static long getFairphoneSince(Context context)
	{
		return getSharedPrefs(context).getLong(PREFERENCE_YOUR_FAIRPHONE_SINCE, 0);
	}

	public static void setFairphoneSince(Context context, long value)
	{
		getSharedPrefs(context).edit().putLong(PREFERENCE_YOUR_FAIRPHONE_SINCE, value).commit();
	}

    private static IBatteryStats sBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
    public static void updateBatteryPreferences(Context context, int level, int status, int scale)
    {
		setBatteryStatus(context.getApplicationContext(), status);
		setBatteryLevel(context.getApplicationContext(), level);
		try {
			long chargeTimeRemaining = sBatteryInfo.computeChargeTimeRemaining();
			if (chargeTimeRemaining >= 0) {
			setBatteryTimeUntilCharged(context.getApplicationContext(), chargeTimeRemaining);
			}
			long batteryTimeRemaining = sBatteryInfo.computeBatteryTimeRemaining();
			if (batteryTimeRemaining >= 0) {
				setBatteryTimeUntilDischarged(context.getApplicationContext(), batteryTimeRemaining);	
			}
	        Log.d(TAG, "updateBatteryPreferences setBatteryTimeUntilCharged "+chargeTimeRemaining);
	        Log.d(TAG, "updateBatteryPreferences setBatteryTimeUntilDischarged "+batteryTimeRemaining);
		} catch (RemoteException e) {
			Log.e(TAG, "failed to updateBatteryPreferences", e);
		}
    }

	public static void sendLastLongerBroadcast(Context context)
	{
		FairphoneClockView.dismissKeyguardOnNextActivity();
		context.startActivity(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	public static void sendUpdateBroadcast(Context context)
	{
		context.sendBroadcast(new Intent(VIEW_UPDATE));
	}
}
