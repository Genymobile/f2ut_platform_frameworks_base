package com.android.server.am;

import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IPrivacyImpactService;
import android.util.Log;

public class PrivacyImpactService extends IPrivacyImpactService.Stub {
    private static final String TAG = "PrivacyImpactService";

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
            "android.admin",
            "android.adminhostside",
            "android.animation",
            "android.app",
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
            "android.core.tests.libcore.package.harmony_sql",
            "android.core.tests.libcore.package.jsr166",
            "android.core.tests.libcore.package.libcore",
            "android.core.tests.libcore.package.okhttp",
            "android.core.tests.libcore.package.org",
            "android.core.tests.libcore.package.sun",
            "android.core.tests.libcore.package.tests",
            "android.core.vm-tests-tf",
            "android.database",
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
            "android.text",
            "android.textureview",
            "android.theme",
            "android.tv",
            "android.uiautomation",
            "android.uirendering",
            "android.usb",
            "android.util",
            "android.view",
            "android.webkit",
            "android.widget",
            "com.android.cts.browserbench",
            "com.android.cts.dram",
            "com.android.cts.filesystemperf",
            "com.android.cts.jank",
            "com.android.cts.opengl",
            "com.android.cts.simplecpu",
            "com.android.cts.tvproviderperf",
            "com.android.cts.ui",
            "com.android.cts.uihost",
            "com.android.cts.videoperf",
            "com.drawelements.deqp.gles3",
            "com.drawelements.deqp.gles31",
            "zzz.android.monkey"
        }
    );
    private final SQLiteOpenHelper mHelper;

    public PrivacyImpactService(Context context) {
        super();
        mContext = context;
        Log.wtf(TAG, "Starting PrivacyImpactService");
        
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
        Log.wtf(TAG, "Started PrivacyImpactService");
    }


    public boolean showPackagePrivacy(String packageName) {
        Log.wtf(TAG, "Querying Package name: " + packageName);
        boolean show = false;

        Log.wtf(TAG, "checking if it is in whitelist "+sWhitelist.contains(packageName));
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
        Log.wtf(TAG, "Adding Package name: " + packageName);
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_PACKAGE_NAME, packageName);

        db.insert(TABLE_NAME, null, values);
    }
}
