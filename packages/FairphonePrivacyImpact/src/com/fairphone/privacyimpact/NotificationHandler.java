package com.fairphone.privacyimpact;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by jpascoal on 15/07/2015.
 */
public class NotificationHandler {

    private static final String TAG = NotificationHandler.class.getSimpleName();
    private Class<?> iNotificationManager;
    private Object iNotificationManagerObject;

    public NotificationHandler(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Method getServiceMethod = null;
        iNotificationManagerObject = null;
        iNotificationManager = null;
        try {
            getServiceMethod = notificationManager.getClass().getMethod("getService");
            iNotificationManagerObject = getServiceMethod.invoke(notificationManager, null);
            iNotificationManager = Class.forName("android.app.INotificationManager");
        } catch (Exception e) {
            Log.w(TAG, "Error calling NotificationHandler constructor:", e);
        }
//        static INotificationManager sINM = INotificationManager.Stub.asInterface(
//                ServiceManager.getService(Context.NOTIFICATION_SERVICE));
    }

    public boolean setNotificationsBanned(String pkg, int uid, boolean banned) {
        try {
            Method setNotificationsEnabledForPackage = iNotificationManager.getMethod("setNotificationsEnabledForPackage", String.class, Integer.TYPE, Boolean.TYPE);
            setNotificationsEnabledForPackage.invoke(iNotificationManagerObject, pkg, uid, !banned);
//            sINM.setNotificationsEnabledForPackage(pkg, uid, !banned);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling setNotificationsBanned:", e);
            return false;
        }
    }

    public boolean getNotificationsBanned(String pkg, int uid) {
        try {
            Method areNotificationsEnabledForPackage = iNotificationManager.getMethod("areNotificationsEnabledForPackage", String.class, Integer.TYPE);
            Object enabled = areNotificationsEnabledForPackage.invoke(iNotificationManagerObject, pkg, uid);
            //final boolean enabled = sINM.areNotificationsEnabledForPackage(pkg, uid);
            return !((Boolean) enabled).booleanValue();
        } catch (Exception e) {
            Log.w(TAG, "Error calling getNotificationsBanned:", e);
            return false;
        }
    }

    public boolean getHighPriority(String pkg, int uid) {
        try {
            Method getPackagePriority = iNotificationManager.getDeclaredMethod("getPackagePriority", String.class, Integer.TYPE);
            Object enabled = getPackagePriority.invoke(iNotificationManagerObject, pkg, uid);
            return ((Integer) enabled).intValue() == Notification.PRIORITY_MAX;
//            return sINM.getPackagePriority(pkg, uid) == Notification.PRIORITY_MAX;
        } catch (Exception e) {
            Log.w(TAG, "Error calling getHighPriority:", e);
            return false;
        }
    }

    public boolean setHighPriority(String pkg, int uid, boolean highPriority) {
        try {
            Method setPackagePriority = iNotificationManager.getMethod("setPackagePriority", String.class, Integer.TYPE, Integer.TYPE);
            setPackagePriority.invoke(iNotificationManagerObject, pkg, uid, highPriority ? Notification.PRIORITY_MAX : Notification.PRIORITY_DEFAULT);
//            sINM.setPackagePriority(pkg, uid, highPriority ? Notification.PRIORITY_MAX : Notification.PRIORITY_DEFAULT);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling setHighPriority:", e);
            return false;
        }
    }

    public boolean getSensitive(String pkg, int uid) {
        try {
            Method getPackageVisibilityOverride = iNotificationManager.getMethod("getPackageVisibilityOverride", String.class, Integer.TYPE);
            Object enabled = getPackageVisibilityOverride.invoke(iNotificationManagerObject, pkg, uid);
            return ((Integer) enabled).intValue() == Notification.VISIBILITY_PRIVATE;
//            return sINM.getPackageVisibilityOverride(pkg, uid) == Notification.VISIBILITY_PRIVATE;
        } catch (Exception e) {
            Log.w(TAG, "Error calling getSensitive:", e);
            return false;
        }
    }

    public boolean setSensitive(String pkg, int uid, boolean sensitive) {
        try {
            Method setPackageVisibilityOverride = iNotificationManager.getMethod("setPackageVisibilityOverride", String.class, Integer.TYPE, Integer.TYPE);
            //setPackageVisibilityOverride.invoke(iNotificationManagerObject, new Object[]{pkg, uid, sensitive ? Notification.VISIBILITY_PRIVATE : NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE});
//            sINM.setPackageVisibilityOverride(pkg, uid, sensitive ? Notification.VISIBILITY_PRIVATE : NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling setSensitive:", e);
            return false;
        }
    }
}
