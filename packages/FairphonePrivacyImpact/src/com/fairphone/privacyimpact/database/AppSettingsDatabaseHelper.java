package com.fairphone.privacyimpact.database;

import android.content.Context;
import android.os.ServiceManager;
import android.os.IPrivacyImpactService;
import android.util.Log;


public class AppSettingsDatabaseHelper {

    private static final String TAG = AppSettingsDatabaseHelper.class.getName();

    private final Context mContext;

    public AppSettingsDatabaseHelper(Context context) {
        mContext = context;
    }

    public boolean isPackageEnable(String packageName){
        boolean result = true;
        try {
            IPrivacyImpactService pis = IPrivacyImpactService.Stub.asInterface(ServiceManager.getService("PrivacyImpact"));
            result = !pis.showPackagePrivacy(packageName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get package "+packageName+" info from service", e);
        }
        return result;
    }

    public void addPackageName(String packageName){
        try {
            IPrivacyImpactService pis = IPrivacyImpactService.Stub.asInterface(ServiceManager.getService("PrivacyImpact"));
            pis.disablePackagePrivacy(packageName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set package "+packageName+" from service", e);
        }
    }
}

