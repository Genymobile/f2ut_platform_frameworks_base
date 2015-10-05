package com.android.keyguard;

import com.android.internal.app.IBatteryStats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryStats;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.util.Log;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by rrocha on 8/25/15.
 */
public class FairphoneClockData
{
	private static final String TAG = FairphoneClockData.class.getSimpleName();
	private static final String BOARD_DATE_FILE = "/persist/board_date.bin";
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
		long startTime = getSharedPrefs(context).getLong(PREFERENCE_YOUR_FAIRPHONE_SINCE, 0);

		byte[] boardFile = getBoardDateFileInByteArray();

        if(!byteArrayCheck(boardFile))
        {
            String strDate = bytesToHex(boardFile);
        	startTime = getStartTimeInMilliseconds(strDate, startTime);
        }

		return startTime;
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
//	    int currentLevel = getBatteryLevel(context);
//	    int currentStatus = getBatteryStatus(context);
//
//	    if (currentLevel != level || currentStatus != status)
//	    {
//		    setBatteryLevel(context, level);
//		    setBatteryStatus(context, status);
//		    updateBatteryDurationTimes(context, level, scale);
//	    }
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

	private static byte[] getBoardDateFileInByteArray()
	{
        File file = new File(BOARD_DATE_FILE);
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            dis.read(fileData);
            dis.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG,"BoardDateFile not found", e);
        } catch (IOException e) {
            Log.e(TAG, "BoardDateFile IOException", e);
        }
        finally {
            return fileData;
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
	private static boolean byteArrayCheck(final byte[] array) {
        int sum = 0;
        for (byte b : array) {
            sum |= b;
        }
        return (sum == 0);
    }

    private static long getStartTimeInMilliseconds(String date, long sharePrefTime)
	{
        Calendar cal = Calendar.getInstance();

        try {
            cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
            cal.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(6, 8)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(date.substring(8, 10)));
            cal.set(Calendar.MINUTE, Integer.parseInt(date.substring(10, 12)));
            cal.set(Calendar.SECOND, Integer.parseInt(date.substring(12, 14)));
        }
        catch (NumberFormatException e) {
            Log.e(TAG, "Parse Exception", e);
			return sharePrefTime;
        }
		return cal.getTimeInMillis();
    }
}
