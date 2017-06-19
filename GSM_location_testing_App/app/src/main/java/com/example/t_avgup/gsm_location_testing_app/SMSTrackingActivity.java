package com.example.t_avgup.gsm_location_testing_app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class SMSTrackingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Global Variables:
    TrackingDatabase dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smstracking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Obtaining permission to send SMS
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
        // Obtaining permission to read SMS
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
        }

        dbHelper = new TrackingDatabase(SMSTrackingActivity.this);

        // TODO: Remove this code segment after testing reading inbox
        readMessages();


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
        getMenuInflater().inflate(R.menu.smstracking, menu);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    // App Functionality begins here

    public void startTracking(View view){
        TextView log = (TextView) findViewById(R.id.logView);
        EditText phoneNumberView = (EditText) findViewById(R.id.editPhone);
        Button button = (Button) findViewById(R.id.startButton);

        Boolean state = ( button.getText().toString() == getResources().getString(R.string.startButton) );
        if (state) {
            String phoneNumber = phoneNumberView.getText().toString();
            phoneNumberView.setFocusable(false);
            phoneNumberView.setFocusableInTouchMode(false);
            button.setText(getResources().getString(R.string.stopButton));
            String logContent = (String) log.getText();
            logContent += "Tracking Started - " + phoneNumberView.getText() + "\n";
            log.setText(logContent);

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, "Test SMS message", null, null);
            }
        }
        else {
            // TODO: add code to stop sending messages
            phoneNumberView.setFocusable(true);
            phoneNumberView.setFocusableInTouchMode(true);
            button.setText(getResources().getString(R.string.startButton));
        }
    }

    private Map<String, String> parseMessageBody (String body) {
        String[] interim = (body.split("\\?")[1]).split("&");
        Map<String, String> output = new HashMap<>();
        for (String expression:interim){
            String[] pair = expression.split("=");
            output.put(pair[0], pair[1]);
        }
        return output;
    }

    private void readMessages(){

        TextView log = (TextView) findViewById(R.id.logView);
        String logContent = log.getText().toString();

        // making log scrollable
        log.setMovementMethod(new ScrollingMovementMethod());

        ContentResolver contentResolver = getContentResolver();
//        Cursor cursor = contentResolver.query(Uri.parse( "content://sms/inbox" ), null, null, null, null);

        String smsNo = "9580264736";
        // cursor
        Cursor cursor = contentResolver.query( Uri.parse("content://sms/inbox" ), null, null, null, null);

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            logContent += cursor.getColumnNames()[i] + ", ";
        }
        logContent += "\n";
        log.setText(logContent);
//        Context context = getApplicationContext();
        int indexBody = cursor.getColumnIndex("body");
        int indexAddr = cursor.getColumnIndex("address");
        int date = cursor.getColumnIndex("date");

        if ( indexBody < 0 || !cursor.moveToFirst() ) return;
        do {
            if (cursor.getString(indexAddr).contains(smsNo) ) {
//                String str = "Sender: " + cursor.getString(indexAddr) + "\n" + cursor.getString(indexBody);
                String body = cursor.getString(indexBody);
                String str = "";
                if (body.contains("cellid")){
                    Map<String, String> info = parseMessageBody(body);

                    SQLiteDatabase sqlDb = dbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put(DatabaseContract.CellIdStream.COL_TRACK_ID, "dummy01");
                    values.put(DatabaseContract.CellIdStream.COL_TIMESTAMP, Long.parseLong(cursor.getString(date), 10));
                    values.put(DatabaseContract.CellIdStream.COL_MCC, info.get("mcc"));
                    values.put(DatabaseContract.CellIdStream.COL_MNC, info.get("mnc"));
                    values.put(DatabaseContract.CellIdStream.COL_LAC, info.get("lac"));
                    values.put(DatabaseContract.CellIdStream.COL_CELL_ID, info.get("cellid"));
                    // Insert the new row, returning the primary key value of the new row
                    long newRowId = sqlDb.insert(DatabaseContract.CellIdStream.TABLE_NAME, null, values);
                    str += Long.toString(newRowId) + "th entry -> " + cursor.getString(date) + " :: " + info.toString();

                    sqlDb.close();
                }
                logContent += str + "\n";
            }
        }
        while( cursor.moveToNext() );
        cursor.close();

        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                DatabaseContract.CellIdStream._ID,
                DatabaseContract.CellIdStream.COL_TIMESTAMP,
                DatabaseContract.CellIdStream.COL_TRACK_ID,
                DatabaseContract.CellIdStream.COL_LAC,
                DatabaseContract.CellIdStream.COL_CELL_ID
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = DatabaseContract.CellIdStream.COL_MCC + " = ?";
        String[] selectionArgs = { "405" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.CellIdStream.COL_TIMESTAMP + " DESC";

        Cursor cursor2 = sqlDB.query(
                DatabaseContract.CellIdStream.TABLE_NAME, // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        logContent += "\n---------------------------------------\nOUTPUT FROM THE DATABASE READ:\n";
        while (cursor2.moveToNext()) {
            long ts = cursor2.getLong(cursor2.getColumnIndexOrThrow(DatabaseContract.CellIdStream.COL_TIMESTAMP));
            String lac = cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseContract.CellIdStream.COL_LAC));
            String cellid = cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseContract.CellIdStream.COL_CELL_ID));
            logContent += "time:" + Long.toString(ts) + " LAC:" + lac + " Cell ID:" + cellid + "\n";
        }

        cursor2.close();
        sqlDB.close();

        log.setText(logContent);
    }
}
