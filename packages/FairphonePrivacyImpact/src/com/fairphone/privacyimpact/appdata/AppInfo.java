package com.fairphone.privacyimpact.appdata;

import android.graphics.drawable.Drawable;

/**
 * Created by Tiago Costa on 20/03/15.
 */
public class AppInfo {


    private final String mAppName;
    private final String mPackageName;
    private final String mVersionName;
    private final String mClassName;

    private int mVersionCode = 0;

    private final Drawable mIcon;

    public AppInfo( String appName, String packageName, String className, Drawable icon, String versionName, int versionCode ){
        mAppName = appName;
        mPackageName = packageName;
        mClassName = className;
        mVersionName = versionName;
        mVersionCode = versionCode;
        mIcon = icon;
    }

    public String getAppName(){
        return mAppName;
    }

    public String getPackageName(){
        return mPackageName;
    }

    public String getVersionName(){
        return mVersionName;
    }

    public String getClassName(){
        return mClassName;
    }

    public Drawable getIcon(){
        return mIcon;
    }

    @Override
    public String toString() {
        return mAppName + "\t" + mPackageName + "\t" + mClassName + "\t" + mVersionName + "\t" + mVersionCode ;
    }

}
