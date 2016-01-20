package com.fairphone.privacyimpact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.net.Uri;

import com.fairphone.privacyimpact.database.AppSettingsDatabaseHelper;

/**
 * Created by kwamecorp on 1/12/16.
 */
public class PackageRemovedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
	if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) &&
   !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)){
            PackageManager pm = context.getPackageManager();
//	    String[] packageNames = pm.getPackagesForUid(intent.getIntExtra(Intent.EXTRA_UID, 0));
	    String packageName = getPackageName(intent);

	if(packageName != null)
            {
		AppSettingsDatabaseHelper.removePackageName(packageName);
        }
    }
}    
   private String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        String pkg = uri != null ? uri.getSchemeSpecificPart() : null;
	return pkg;
    }


}
