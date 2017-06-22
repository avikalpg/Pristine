package com.example.t_avgup.gsm_location_testing_app;

import android.provider.BaseColumns;

/**
 * Created by t-avgup on 6/18/2017.
 */
public final class DatabaseContract {

    private DatabaseContract() {}

    public static abstract class CellIdStream implements BaseColumns {
        public static final String TABLE_NAME = "cellIdStream";
        public static final String COL_TRACK_ID = "trackID";
        public static final String COL_TIMESTAMP = "TimeStamp";
        public static final String COL_MCC = "MCC";
        public static final String COL_MNC = "MNC";
        public static final String COL_LAC = "LAC";
        public static final String COL_CELL_ID = "CELL_ID";
    }

    public static abstract class TrackingInfo implements BaseColumns {
        public static final String TABLE_NAME = "trackingInfo";
        public static final String COL_TRACK_ID = "trackID";
        public static final String COL_TIMESTAMP = "TimeStamp";
        public static final String COL_LAT = "Lat";
        public static final String COL_LONG = "Long";
        public static final String COL_ACCURACY = "accuracy";
    }
}
