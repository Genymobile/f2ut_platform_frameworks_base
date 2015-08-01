package com.fairphone.privacyimpact.database;

/**
 * Created by Tiago Costa on 16/03/15.
 */
class AppPermissionsTable {
    public static final String TABLE_NAME           = "AppPermissions";
    public static final String COLUMN_ID            = "_id";
    public static final String COLUMN_PACKAGE_NAME  = "name";


    public static final String SQL_STATEMENT_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    // Table creation sql statement
    public static final String SQL_STATEMENT_CREATE_TABLE = "create table "
            + TABLE_NAME            + "("
            + COLUMN_ID             + " integer primary key autoincrement, "
            + COLUMN_PACKAGE_NAME   + " text not null"
            +                       ");";


}
