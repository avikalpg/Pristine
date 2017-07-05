package com.example.t_avgup.gsm_location_testing_app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LandingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Global Variables:
    TrackingDatabase dbHelper;

    ArrayList<String> listItems = new ArrayList<>();
    ArrayList<Integer> status_listItems = new ArrayList<>();
    ArrayList<String> startingAddr_listItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dbHelper = new TrackingDatabase(this);

        // Activity
        populateListView();
        registerClickCallBack();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.landing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_phone) {
            // Switch to MainActivity
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_tracker) {
            // Switch to LandingActivity
            startActivity(new Intent(this, LandingActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // App functionality begins here

    private class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return listItems.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.track_list_layout, null);

            TextView track_id_view = (TextView) view.findViewById(R.id.trackID_textView);
            TextView status_view = (TextView) view.findViewById(R.id.status_textView);
            TextView startingPoint_view = (TextView) view.findViewById(R.id.address_textView);

            track_id_view.setText(listItems.get(i));
            startingPoint_view.setText(startingAddr_listItems.get(i));
            int statusInt = status_listItems.get(i);
            if (statusInt == 0){
                status_view.setTextColor(Color.GRAY);
                status_view.setText(R.string.trackingStatus_initial);
            } else if (statusInt == 1) {
                status_view.setTextColor(Color.YELLOW);
                status_view.setText(R.string.trackingStatus_started);
            } else if (statusInt == 2) {
                status_view.setTextColor(Color.GREEN);
                status_view.setText(R.string.trackingStatus_completed);
            } else if (statusInt == 10000) {
                status_view.setText(R.string.trackingStatus_newItem);
            } else {
                status_view.setTextColor(Color.RED);
                status_view.setText(R.string.trackingStatus_error);
            }
            return view;
        }
    }

    private void populateListView() {
        // Create List Items - get all the trackIDs from the database and add "Add Item..." at the end
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();

        // TODO: Instead of grouping by track_id, directly only get unique track_ids
        String[] projection = {
                DatabaseContract.CellIdStream.COL_TRACK_ID,
        };

        Cursor cursor = sqlDB.query(
                DatabaseContract.CellIdStream.TABLE_NAME,       // The table to query
                projection,                                     // The columns to return
                null,                                           // The columns for the WHERE clause
                null,                                           // The values for the WHERE clause
                DatabaseContract.CellIdStream.COL_TRACK_ID,     // don't group the rows
                null,                                           // don't filter by row groups
                null                                            // The sort order
        );

        // TODO: Implement methods to systematically get status
        // TODO: Implement methods to systematically get starting address
        while (cursor.moveToNext()) {
            String track_id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.CellIdStream.COL_TRACK_ID));
            listItems.add(track_id);
            status_listItems.add(0);
            startingAddr_listItems.add("Default Starting Address");
        }
        cursor.close();
        listItems.add(getResources().getString(R.string.trackingID_newItem));
        status_listItems.add(10000);
        startingAddr_listItems.add("");

        // Build Adapter
        CustomAdapter adapter = new CustomAdapter();

        // Configure the list view
        ListView trackListView = (ListView) findViewById(R.id.trackingListView);
        trackListView.setAdapter(adapter);
    }

    private void registerClickCallBack() {
        ListView trackListView = (ListView) findViewById(R.id.trackingListView);
        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                RelativeLayout item = (RelativeLayout) view;
                TextView itemTrackID = (TextView) view.findViewById(R.id.trackID_textView);

                if (itemTrackID.getText().toString().equals(getResources().getString(R.string.trackingID_newItem))){
                    // Create a new thread
                    // Launch the SMSTracking Activity with this thread
                    startActivity(new Intent(LandingActivity.this, SMSTrackingActivity.class));
                }

                // Remove this
                Log.d("onItemClick", itemTrackID.getText().toString());
            }
        });
    }

}
