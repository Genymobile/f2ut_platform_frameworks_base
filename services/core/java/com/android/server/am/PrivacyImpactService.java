package com.android.server.am;

import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IPrivacyImpactService;
import android.util.Log;

public class PrivacyImpactService extends IPrivacyImpactService.Stub {
    private static final String TAG = "PrivacyImpactService";

    public static final String PREFS_NAME           = "privacy_prefs";
    public static final String PREFS_ENABLED           = "privacy_prefs";
    
    public static final String TABLE_NAME           = "popup";
    public static final String COLUMN_ID            = "_id";
    public static final String COLUMN_PACKAGE_NAME  = "name";
    private static final String DB_NAME             = "fairphone_privacy_impact.db";
    private static final int DB_VERSION             = 1;

    private final Context mContext;
    private final static List<String> sWhitelist = Arrays.asList(
        new String[] {
            "android.JobScheduler",
            "android.aadb",
            "android.acceleration",
            "android.accessibility",
            "android.accessibilityservice",
            "android.accounts",
            "android.accounts.cts",
            "android.admin",
            "android.adminhostside",
            "android.animation",
            "android.app",
            "android.app.cts.uiautomation",
            "android.app.usage.cts",
            "android.appwidget",
            "android.bionic",
            "android.bluetooth",
            "android.calendarcommon",
            "android.content",
            "android.core.tests.libcore.package.com",
            "android.core.tests.libcore.package.conscrypt",
            "android.core.tests.libcore.package.dalvik",
            "android.core.tests.libcore.package.harmony_annotation",
            "android.core.tests.libcore.package.harmony_beans",
            "android.core.tests.libcore.package.harmony_java_io",
            "android.core.tests.libcore.package.harmony_java_lang",
            "android.core.tests.libcore.package.harmony_java_math",
            "android.core.tests.libcore.package.harmony_java_net",
            "android.core.tests.libcore.package.harmony_java_nio",
            "android.core.tests.libcore.package.harmony_java_text",
            "android.core.tests.libcore.package.harmony_java_util",
            "android.core.tests.libcore.package.harmony_javax_security",
            "android.core.tests.libcore.package.harmony_logging",
            "android.core.tests.libcore.package.harmony_prefs",
            "android.core.tests.libcore.package.harmony_sql",
            "android.core.tests.libcore.package.jsr166",
            "android.core.tests.libcore.package.libcore",
            "android.core.tests.libcore.package.okhttp",
            "android.core.tests.libcore.package.org",
            "android.core.tests.libcore.package.sun",
            "android.core.tests.libcore.package.tests",
            "android.core.tests.runner",
            "android.core.vm-tests-tf",
            "android.cts.appwidget",
            "android.database",
            "android.deviceadmin.cts",
            "android.display",
            "android.dpi",
            "android.dreams",
            "android.drm",
            "android.effect",
            "android.gesture",
            "android.graphics",
            "android.graphics2",
            "android.hardware",
            "android.host.dumpsys",
            "android.host.security",
            "android.host.theme",
            "android.jdwp",
            "android.jni",
            "android.jobscheduler.cts.deviceside",
            "android.keystore",
            "android.location",
            "android.location2",
            "android.media",
            "android.mediastress",
            "android.nativemedia.sl",
            "android.nativemedia.xa",
            "android.nativeopengl",
            "android.ndef",
            "android.net",
            "android.net.hostsidenetwork",
            "android.opengl",
            "android.openglperf",
            "android.os",
            "android.permission",
            "android.permission2",
            "android.preference",
            "android.preference2",
            "android.print",
            "android.provider",
            "android.renderscript",
            "android.renderscriptlegacy",
            "android.rscpp",
            "android.sax",
            "android.security",
            "android.signature",
            "android.speech",
            "android.telephony",
            "android.tests.appsecurity",
            "android.tests.devicesetup",
            "android.text",
            "android.textureview",
            "android.theme",
            "android.theme.app",
            "android.tv",
            "android.uiautomation",
            "android.uirendering",
            "android.usb",
            "android.util",
            "android.view",
            "android.view.accessibility.services",
            "android.view.cts.accessibility",
            "android.webgl.cts",
            "android.webkit",
            "android.widget",
            "com.android.cts.acceleration",
            "com.android.cts.acceleration.stub",
            "com.android.cts.accessibilityservice",
            "com.android.cts.admin",
            "com.android.cts.animation",
            "com.android.cts.app",
            "com.android.cts.app.stub",
            "com.android.cts.appaccessdata",
            "com.android.cts.appwithdata",
            "com.android.cts.bluetooth",
            "com.android.cts.browser",
            "com.android.cts.browserbench",
            "com.android.cts.calendarcommon2",
            "com.android.cts.content",
            "com.android.cts.database",
            "com.android.cts.deviceowner",
            "com.android.cts.display",
            "com.android.cts.documentclient",
            "com.android.cts.documentprovider",
            "com.android.cts.dpi",
            "com.android.cts.dpi2",
            "com.android.cts.dram",
            "com.android.cts.dreams",
            "com.android.cts.drm",
            "com.android.cts.effect",
            "com.android.cts.externalstorageapp",
            "com.android.cts.filesystemperf",
            "com.android.cts.gesture",
            "com.android.cts.graphics",
            "com.android.cts.graphics2",
            "com.android.cts.hardware",
            "com.android.cts.instrumentationdiffcertapp",
            "com.android.cts.intent.receiver",
            "com.android.cts.intent.sender",
            "com.android.cts.jank",
            "com.android.cts.jni",
            "com.android.cts.keysets",
            "com.android.cts.keysets.testapp",
            "com.android.cts.keysets_permdef",
            "com.android.cts.keystore",
            "com.android.cts.launcherapps.simpleapp",
            "com.android.cts.launchertests",
            "com.android.cts.launchertests.support",
            "com.android.cts.location",
            "com.android.cts.location2",
            "com.android.cts.managedprofile",
            "com.android.cts.media",
            "com.android.cts.mediastress",
            "com.android.cts.monkey",
            "com.android.cts.monkey2",
            "com.android.cts.multiuserstorageapp",
            "com.android.cts.ndef",
            "com.android.cts.net",
            "com.android.cts.net.hostside",
            "com.android.cts.opengl",
            "com.android.cts.openglperf",
            "com.android.cts.os",
            "com.android.cts.permission",
            "com.android.cts.permission2",
            "com.android.cts.permissiondeclareapp",
            "com.android.cts.permissiondeclareappcompat",
            "com.android.cts.preference",
            "com.android.cts.preference2",
            "com.android.cts.print",
            "com.android.cts.provider",
            "com.android.cts.readexternalstorageapp",
            "com.android.cts.renderscript",
            "com.android.cts.renderscriptlegacy",
            "com.android.cts.rscpp",
            "com.android.cts.sax",
            "com.android.cts.security",
            "com.android.cts.shareuidinstall",
            "com.android.cts.shareuidinstalldiffcert",
            "com.android.cts.simpleappinstall",
            "com.android.cts.simplecpu",
            "com.android.cts.speech",
            "com.android.cts.splitapp",
            "com.android.cts.targetinstrumentationapp",
            "com.android.cts.taskswitching.appa",
            "com.android.cts.taskswitching.appb",
            "com.android.cts.taskswitching.control",
            "com.android.cts.telephony",
            "com.android.cts.text",
            "com.android.cts.textureview",
            "com.android.cts.theme",
            "com.android.cts.tv",
            "com.android.cts.tvproviderperf",
            "com.android.cts.ui",
            "com.android.cts.uiautomator",
            "com.android.cts.uihost",
            "com.android.cts.uirendering",
            "com.android.cts.usb.serialtest",
            "com.android.cts.usespermissiondiffcertapp",
            "com.android.cts.util",
            "com.android.cts.videoperf",
            "com.android.cts.view",
            "com.android.cts.webkit",
            "com.android.cts.widget",
            "com.android.cts.writeexternalstorageapp",
            "com.android.opengl.cts",
            "com.drawelements.deqp",
            "com.drawelements.deqp.gles3",
            "com.drawelements.deqp.gles31",
            "com.replica.replicaisland",
            "zzz.android.monkey"
        }
    );
    private final SQLiteOpenHelper mHelper;
    private final SharedPreferences mPrefs;

    public PrivacyImpactService(Context context) {
        super();
        mContext = context;
        Log.i(TAG, "Starting PrivacyImpactService");
        
        mHelper = new SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(
                    "create table "
                    + TABLE_NAME            + "("
                    + COLUMN_ID             + " integer primary key autoincrement, "
                    + COLUMN_PACKAGE_NAME   + " text not null"
                    + ");"
                );
            }
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
            }
        };
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "Started PrivacyImpactService");
    }


    public boolean showPackagePrivacy(String packageName) {
        Log.i(TAG, "Querying Package name: " + packageName);
        boolean show = false;

        Log.i(TAG, "checking if it is in whitelist "+sWhitelist.contains(packageName));
        // Don't show Privacy Impact for whitelisted apps
        if (!sWhitelist.contains(packageName)) {
            // Don't show Privacy Impact for system apps
            boolean isSystemApp = false;
            try {
                isSystemApp = (mContext.getPackageManager().getApplicationInfo(packageName, 0).flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Can't determine if "+packageName+" is a system app.", e);
            }
    
            if (!isSystemApp) {
                SQLiteDatabase db = mHelper.getReadableDatabase();
                Cursor cursor = db.query(
                    TABLE_NAME,
                    new String[] { COLUMN_ID, COLUMN_PACKAGE_NAME },
                    COLUMN_PACKAGE_NAME + " =?",
                    new String[]{ packageName },
                    null,
                    null,
                    null
                );
    
                cursor.moveToFirst();
                show = cursor.getCount() == 0;
                cursor.close();
            }
        }
        return show;
    }

    public void disablePackagePrivacy(String packageName) {
        Log.i(TAG, "Adding Package name: " + packageName);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_PACKAGE_NAME, packageName);

        db.insert(TABLE_NAME, null, values);
    }

    public void enablePackagePrivacy(String packageName) {
        Log.i(TAG, "Removing Package name: " + packageName);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        int result = db.delete(TABLE_NAME, COLUMN_PACKAGE_NAME + " = ?", new String[]{packageName});
    }

    
    public void clearPackagePrivacyData() {
        Log.i(TAG, "Clearing Privacy Impact database");
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(PREFS_ENABLED);
        editor.apply();
    }
    
    public boolean isPrivacyImpactEnabled() {
        boolean result = mPrefs.getBoolean(PREFS_ENABLED, true);
        Log.i(TAG, "isPrivacyImpactEnabled() = "+result);
        return result;
    }
    
    public void setPrivacyImpactStatus(boolean enabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(PREFS_ENABLED, enabled);
        editor.apply();
        Log.i(TAG, "setPrivacyImpactStatus("+enabled+")");
    }
}
