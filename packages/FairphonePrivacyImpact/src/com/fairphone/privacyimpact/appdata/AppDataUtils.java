package com.fairphone.privacyimpact.appdata;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tiago Costa on 20/03/15.
 */
public class AppDataUtils {

    private static final String TAG = AppDataUtils.class.getSimpleName();

    public static List<AppInfo> getAllInstalledApps(Context context, boolean getSystemApps) {
        ArrayList<AppInfo> apps = getInstalledApps(context, getSystemApps); /* false = no system packages */
        final int max = apps.size();

        for (int i=0; i<max; i++) {
            Log.d(TAG, apps.get(i).toString());
        }

        return apps;
    }

    private static ArrayList<AppInfo> getInstalledApps(Context context, boolean getSysPackages) {
        ArrayList<AppInfo> res = new ArrayList<>();
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);

        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            if ((!getSysPackages) && (p.versionName == null)) {
                continue ;
            }

            List<ResolveInfo> activitiesForPackage = findActivitiesForPackage(context, p.packageName);

            for(ResolveInfo info : activitiesForPackage){

                res.add(generateAppInfo(context, p, info));
            }

        }
        return res;
    }

    private static AppInfo generateAppInfo(Context context, PackageInfo p, ResolveInfo info){
        String appName = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
        String packageName = p.packageName;
        String versionName = p.versionName;
        int versionCode = p.versionCode;
        String className = "";
        if(info != null) {
            className = info.activityInfo.name;
        }
        Drawable icon = p.applicationInfo.loadIcon(context.getPackageManager());

        return new AppInfo(appName, packageName, className, icon, versionName, versionCode);
    }

    private static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        return apps != null ? apps : new ArrayList<ResolveInfo>();
    }

    public static AppInfo getAppInformation(Context context, String packageName) {
        AppInfo info = null;

        try {
            info = generateAppInfo(context, context.getPackageManager().getPackageInfo(packageName, 0), null);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }
}
