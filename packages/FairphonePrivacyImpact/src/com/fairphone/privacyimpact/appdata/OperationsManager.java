package com.fairphone.privacyimpact.appdata;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tiago Costa on 30/03/15.
 */
public class OperationsManager {
    private static final String TAG = OperationsManager.class.getSimpleName();

    public static Pair<PackageInfo, List<String>> getValidPermissionListForPackage(PackageManager packageManager, String packageName) {
        PackageInfo packageInfo = null;
        List<String> list = null;

        if (!TextUtils.isEmpty(packageName) && packageManager != null) {
            try {
                packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                list = new ArrayList<>();

                //Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    boolean isSystemPermission;
                    for (String perm : requestedPermissions) {
                        try {
                            PermissionInfo info = packageManager.getPermissionInfo(perm, PackageManager.GET_META_DATA);
                            if (isPermissionValid(packageManager, info)) {
                                isSystemPermission = true;
                            } else {
                                isSystemPermission = false;
                                Log.w(TAG, "Permission " + perm + " not added. Reason: Not recognized on AppOps");
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.w(TAG, "Permission " + perm + " not added. Reason: " + e.getLocalizedMessage());
                            isSystemPermission = false;
                        }

                        if (isSystemPermission) {
                            list.add(perm);
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Unable to get package info for " + packageName + ". Reason " + e.getLocalizedMessage());
                list = null;
                packageInfo = null;
            }
        }
        return new Pair<>(packageInfo, list);
    }

    private static boolean isPermissionValid(PackageManager packageManager, PermissionInfo info) {
        String res = info == null ? "" : info.loadLabel(packageManager).toString();
        res = res.toLowerCase().startsWith("com.") ? "" : res;
        return !TextUtils.isEmpty(res);
    }
}


