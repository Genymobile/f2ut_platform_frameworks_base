package com.fairphone.privacyimpact.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Tiago Costa on 16/03/15.
 */
public class AppSettingsDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = AppSettingsDatabaseHelper.class.getName();
    private static final String DATABASE_NAME = "FairphoneAppSettings.db";
    private static final int DATABASE_VERSION = 1;

    public AppSettingsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppPermissionsTable.SQL_STATEMENT_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(AppPermissionsTable.SQL_STATEMENT_DROP_TABLE);

        onCreate(db);
    }

    public boolean isPackageEnable(String packageName){
        Log.wtf(TAG, "Querying Package name : " + packageName);

        SQLiteDatabase db = getReadableDatabase();
        Cursor packageCursor = db.query(AppPermissionsTable.TABLE_NAME,
                new String[] { AppPermissionsTable.COLUMN_ID, AppPermissionsTable.COLUMN_PACKAGE_NAME },
                AppPermissionsTable.COLUMN_PACKAGE_NAME + " =?", new String[]{ packageName }, null, null, null);

        packageCursor.moveToFirst();

        boolean isEnabled = packageCursor.getCount() > 0;
        packageCursor.close();
        return isEnabled;
    }

    public void addPackageName(String packageName){
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(AppPermissionsTable.COLUMN_PACKAGE_NAME, packageName);

        db.insert(AppPermissionsTable.TABLE_NAME, null, values);
    }
}
