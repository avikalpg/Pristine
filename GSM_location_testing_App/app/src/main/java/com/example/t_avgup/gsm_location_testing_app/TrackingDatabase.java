package com.example.t_avgup.gsm_location_testing_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by t-avgup on 6/19/2017.
 */
public class TrackingDatabase extends SQLiteOpenHelper {

    private static final String SQL_CREATE_CELL_ID_STREAM =
            "CREATE TABLE " + DatabaseContract.CellIdStream.TABLE_NAME + " (" +
                    DatabaseContract.CellIdStream._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.CellIdStream.COL_TRACK_ID + " TINYTEXT," +
                    DatabaseContract.CellIdStream.COL_TIMESTAMP + " BIGINT," +
                    DatabaseContract.CellIdStream.COL_MCC + " TINYTEXT," +
                    DatabaseContract.CellIdStream.COL_MNC + " TINYTEXT," +
                    DatabaseContract.CellIdStream.COL_LAC + " TINYTEXT," +
                    DatabaseContract.CellIdStream.COL_CELL_ID + " TINYTEXT )";

    private static final String SQL_DELETE_CELL_ID_STREAM =
            "DROP TABLE IF EXISTS " + DatabaseContract.CellIdStream.TABLE_NAME;

    private static final String SQL_CREATE_TRACKING_INFO =
            "CREATE TABLE " + DatabaseContract.TrackingInfo.TABLE_NAME + " (" +
                    DatabaseContract.TrackingInfo._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.TrackingInfo.COL_TRACK_ID + " TINYTEXT," +
                    DatabaseContract.TrackingInfo.COL_TIMESTAMP + " BIGINT," +
                    DatabaseContract.TrackingInfo.COL_LAT + " FLOAT," +
                    DatabaseContract.TrackingInfo.COL_LONG + " FLOAT," +
                    DatabaseContract.TrackingInfo.COL_ACCURACY + " FLOAT )";

    private static final String SQL_DELETE_TRACKING_INFO =
            "DROP TABLE IF EXISTS " + DatabaseContract.TrackingInfo.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "tracking.db";

    public TrackingDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CELL_ID_STREAM);
        db.execSQL(SQL_CREATE_TRACKING_INFO);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_CELL_ID_STREAM);
        db.execSQL(SQL_DELETE_TRACKING_INFO);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
