package com.fairphone.privacyimpact.database;

import android.content.Context;
import android.os.ServiceManager;
import android.os.IPrivacyImpactService;
import android.util.Log;


public class AppSettingsDatabaseHelper {

    private static final String TAG = AppSettingsDatabaseHelper.class.getName();

    public static boolean isPackageEnable(String packageName){
        boolean result = true;
        try {
            IPrivacyImpactService pis = IPrivacyImpactService.Stub.asInterface(ServiceManager.getService("PrivacyImpact"));
            result = !pis.showPackagePrivacy(packageName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get package "+packageName+" info from service", e);
        }
        return result;
    }

    public static void addPackageName(String packageName){
        try {
            IPrivacyImpactService pis = IPrivacyImpactService.Stub.asInterface(ServiceManager.getService("PrivacyImpact"));
            pis.disablePackagePrivacy(packageName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set package "+packageName+" from service", e);
        }
    }
    
    public static void resetPrivacyDatabase(){
        try {
            IPrivacyImpactService pis = IPrivacyImpactService.Stub.asInterface(ServiceManager.getService("PrivacyImpact"));
            pis.clearPackagePrivacyData();
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear privacy impact database", e);
        }
    }
    
    public static boolean isPrivacyImpactEnabled(){
        boolean result = false;
        try {
            IPrivacyImpactService pis = IPrivacyImpactService.Stub.asInterface(ServiceManager.getService("PrivacyImpact"));
            result = !pis.isPrivacyImpactEnabled();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get privacy impact enable status. Fallback to disabled.", e);
        }
        return result;
    }
    
    public static void setPrivacyImpactStatus(boolean enabled){
        try {
            IPrivacyImpactService pis = IPrivacyImpactService.Stub.asInterface(ServiceManager.getService("PrivacyImpact"));
            pis.setPrivacyImpactStatus(enabled);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get privacy impact enable status. Fallback to disabled.", e);
        }
    }
}

