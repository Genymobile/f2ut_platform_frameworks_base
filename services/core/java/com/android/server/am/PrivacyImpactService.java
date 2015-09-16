package com.android.server.am;

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
