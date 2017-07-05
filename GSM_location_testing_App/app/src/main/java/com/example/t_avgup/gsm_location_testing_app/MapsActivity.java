package com.example.t_avgup.gsm_location_testing_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Global Variables:
    TrackingDatabase dbHelper;

    // TODO: Extract this track id based on the active SMSTrackingActivity which called this Activity
    String track_id = "dummy01";
    ArrayList<LatLng> pointSequence = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Functionality begins here
        dbHelper = new TrackingDatabase(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        updateMap();
        for (LatLng point : pointSequence){
            mMap.addMarker(new MarkerOptions().position(point));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pointSequence.get(pointSequence.size() - 1)));
    }

    private void updateMap() {
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();

        String[] projection = new String[]{
                DatabaseContract.TrackingInfo._ID,
                DatabaseContract.TrackingInfo.COL_TIMESTAMP,
                DatabaseContract.TrackingInfo.COL_TRACK_ID,
                DatabaseContract.TrackingInfo.COL_LAT,
                DatabaseContract.TrackingInfo.COL_LONG
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = DatabaseContract.TrackingInfo.COL_TRACK_ID + " = ?";
        String[] selectionArgs = { track_id };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.TrackingInfo.COL_TIMESTAMP;

        Cursor cursor = sqlDB.query(
                DatabaseContract.TrackingInfo.TABLE_NAME, // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        while (cursor.moveToNext()) {
            String latS = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TrackingInfo.COL_LAT));
            String lonS = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.TrackingInfo.COL_LONG));
            Double lat = Double.parseDouble(latS);
            Double lon = Double.parseDouble(lonS);
            LatLng newPoint = new LatLng(lat, lon);
            pointSequence.add(newPoint);
        }

        cursor.close();
        sqlDB.close();

    }
}
