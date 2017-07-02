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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

public class SMSTrackingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Global Variables:
    TrackingDatabase dbHelper;

    Handler handler;
    TextView log;
    Boolean keepTracking = false;
    trackingThread bgThread;

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

        log = (TextView) findViewById(R.id.logView);
        bgThread = new trackingThread();
        bgThread.start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String logContent = "" + log.getText();
                logContent += "\nLoop running. Arg sent: " + msg.toString() + "\n";
                log.setText(logContent);
            }
        };

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
        log = (TextView) findViewById(R.id.logView);
        EditText phoneNumberView = (EditText) findViewById(R.id.editPhone);
        Button button = (Button) findViewById(R.id.startButton);

        Boolean state = ( button.getText().toString() == getResources().getString(R.string.startButton) );
        if (state) {
            final String phoneNumber = phoneNumberView.getText().toString();
            bgThread.setPhoneNumber(phoneNumber);
            phoneNumberView.setFocusable(false);
            phoneNumberView.setFocusableInTouchMode(false);
            button.setText(getResources().getString(R.string.stopButton));
            String logContent = "" + log.getText();
            logContent += "Tracking Started - " + phoneNumberView.getText() + "\n";
            log.setText(logContent);
            keepTracking = true;

        }
        else {
            // TODO: add code to stop sending messages
//            phoneNumberView.setFocusable(true);
//            phoneNumberView.setFocusableInTouchMode(true);
            button.setText(getResources().getString(R.string.startButton));
            keepTracking = false;
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

    private long saveCellId (String track_id, String timestamp, String mcc, String mnc, String lac, String cell_id) {

        SQLiteDatabase sqlDb = dbHelper.getWritableDatabase();

        // Check whether this entry already exists in the database
        String[] projection = {
                DatabaseContract.CellIdStream._ID,
                DatabaseContract.CellIdStream.COL_TIMESTAMP,
                DatabaseContract.CellIdStream.COL_TRACK_ID,
                DatabaseContract.CellIdStream.COL_LAC,
                DatabaseContract.CellIdStream.COL_CELL_ID
        };

        String selection = DatabaseContract.CellIdStream.COL_TIMESTAMP + " = ? AND " + DatabaseContract.CellIdStream.COL_TRACK_ID + " = ? AND " +
                DatabaseContract.CellIdStream.COL_LAC + " = ? AND " + DatabaseContract.CellIdStream.COL_CELL_ID + " = ?";
        String[] selectionArgs = { timestamp, track_id, lac, cell_id };

        String sortOrder =
                DatabaseContract.CellIdStream.COL_TIMESTAMP + " DESC";

        Cursor cursor = sqlDb.query(
                DatabaseContract.CellIdStream.TABLE_NAME, // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if (cursor.getCount() > 1) {
            Log.w("saveCellId", "Data point already exists in the database:\n" +
                    track_id + ", " + timestamp + ", " + mcc + ":" + mnc + ":" + lac + ":" + cell_id);
            return -1;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CellIdStream.COL_TRACK_ID, track_id);
        values.put(DatabaseContract.CellIdStream.COL_TIMESTAMP, Long.parseLong(timestamp, 10));
        values.put(DatabaseContract.CellIdStream.COL_MCC, mcc);
        values.put(DatabaseContract.CellIdStream.COL_MNC, mnc);
        values.put(DatabaseContract.CellIdStream.COL_LAC, lac);
        values.put(DatabaseContract.CellIdStream.COL_CELL_ID, cell_id);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = sqlDb.insert(DatabaseContract.CellIdStream.TABLE_NAME, null, values);

        sqlDb.close();

        getLatLong(mcc, mnc, lac, cell_id, track_id, Long.parseLong(timestamp, 10));

        return newRowId;
    }

    private long saveLatLong (String track_id, long timestamp, Float Lat, Float Long, Float accuracy) {

        SQLiteDatabase sqlDb = dbHelper.getWritableDatabase();

        // Check whether this entry already exists in the database
        String[] projection = {
                DatabaseContract.TrackingInfo._ID,
                DatabaseContract.TrackingInfo.COL_TRACK_ID,
                DatabaseContract.TrackingInfo.COL_TIMESTAMP,
                DatabaseContract.TrackingInfo.COL_LAT,
                DatabaseContract.TrackingInfo.COL_LONG,
                DatabaseContract.TrackingInfo.COL_ACCURACY
        };

        String selection = DatabaseContract.TrackingInfo.COL_TIMESTAMP + " = ? AND " + DatabaseContract.TrackingInfo.COL_TRACK_ID + " = ? AND " +
                DatabaseContract.TrackingInfo.COL_LAT + " = ? AND " + DatabaseContract.TrackingInfo.COL_LONG + " = ?";
        String[] selectionArgs = { String.valueOf(timestamp), track_id, Lat.toString(), Long.toString() };

        String sortOrder =
                DatabaseContract.TrackingInfo.COL_TIMESTAMP + " DESC";

        Cursor cursor = sqlDb.query(
                DatabaseContract.TrackingInfo.TABLE_NAME, // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if (cursor.getCount() > 0) {
            Log.w("saveCellId", "Lat-Long point already exists in database:\n" +
                    track_id + ", " + timestamp + ", " + Lat + ":" + Long );
            return -1;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TrackingInfo.COL_TRACK_ID, track_id);
        values.put(DatabaseContract.TrackingInfo.COL_TIMESTAMP, timestamp);
        values.put(DatabaseContract.TrackingInfo.COL_LAT, Lat);
        values.put(DatabaseContract.TrackingInfo.COL_LONG, Long);
        values.put(DatabaseContract.TrackingInfo.COL_ACCURACY, accuracy);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = sqlDb.insert(DatabaseContract.TrackingInfo.TABLE_NAME, null, values);
        Log.d("SaveLatLong", "Data point (" + track_id + ", " + timestamp + ", " + Lat + ", " + Long + ", " + accuracy + ") was added to the TrackingInfo table");

        sqlDb.close();

        return newRowId;
    }

    private void getLatLong (String mcc, String mnc, String lac, String cell_id, final String track_id, final long timestamp ) {
//        HttpResponse<String> response = Unirest.post("https://ap1.unwiredlabs.com/v2/process.php")
//                .body("{\"token\": \"99cc232e3b9fae\",\"radio\": \"gsm\",\"mcc\": "+info.get("mcc")+",\"mnc\": "+info.get("mnc")+",\"cells\": [{\"lac\": "+info.get("lac")+",\"cid\": "+info.get("cellid")+"}],\"address\": 1}")
//                .asString();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://ap1.unwiredlabs.com/v2/process.php";

        // Request a string response from the provided URL.
        try {
            JSONObject jsonBody = new JSONObject("{\"token\": \"99cc232e3b9fae\",\"radio\": \"gsm\",\"mcc\": " + mcc + ",\"mnc\": " + mnc + ",\"cells\": [{\"lac\": " + lac + ",\"cid\": " + cell_id + "}],\"address\": 1}");
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,jsonBody,
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                new Response.Listener<String>() {
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Response is: "+ response.toString(),
                                    Toast.LENGTH_LONG
                            ).show();
                            try {
                                if (response.getString("status").equals("ok")) {
                                    saveLatLong(
                                            track_id,
                                            timestamp,
                                            (float)response.getDouble("lat"),
                                            (float)response.getDouble("lon"),
                                            (float)response.getDouble("accuracy")
                                    );
                                } else {
                                    Log.d("OpenCellID", "STATUS not OK in HTTP response");
                                    Toast.makeText(getApplicationContext(), "No Lat-Long received", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException je) {
                                Log.d("JSONObjectRequest", "JSON response is not alright");
                                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "That didn't work!", Toast.LENGTH_SHORT).show();
                }
            });
// Add the request to the RequestQueue.
            queue.add(request);

        } catch (JSONException je) {
            Log.d("GetLatLong Function:::", "JSON Object is not well formed (throws JSONException)");
            Toast.makeText(getApplicationContext(), "JSONException: JSONObject not well formed", Toast.LENGTH_SHORT).show();
        }
    }

    private void readMessages(){

        log = (TextView) findViewById(R.id.logView);
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
                    long newRowId = saveCellId(
                            "dummy01",
                            cursor.getString(date),
                            info.get("mcc"),
                            info.get("mnc"),
                            info.get("lac"),
                            info.get("cellid")
                    );
                    str += Long.toString(newRowId) + "th entry -> " + cursor.getString(date) + " :: " + info.toString();
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

        // Define a projection that specifies which columns from the database you will actually use after this query.
        projection = new String[]{
                DatabaseContract.TrackingInfo._ID,
                DatabaseContract.TrackingInfo.COL_TIMESTAMP,
                DatabaseContract.TrackingInfo.COL_TRACK_ID,
                DatabaseContract.TrackingInfo.COL_LAT,
                DatabaseContract.TrackingInfo.COL_LONG
        };

        cursor2 = sqlDB.query(
                DatabaseContract.TrackingInfo.TABLE_NAME, // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        logContent += "\n---------------------------------------\nOUTPUT FROM THE TABLE 2 READ:\n" + String.valueOf(cursor2.getCount()) + "\n";
        while (cursor2.moveToNext()) {
            long ts = cursor2.getLong(cursor2.getColumnIndexOrThrow(DatabaseContract.TrackingInfo.COL_TIMESTAMP));
            String lat = cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseContract.TrackingInfo.COL_LAT));
            String lon = cursor2.getString(cursor2.getColumnIndexOrThrow(DatabaseContract.TrackingInfo.COL_LONG));
            logContent += "time:" + Long.toString(ts) + " LAT:" + lat + " Long:" + lon + "\n";
        }

        cursor2.close();
        sqlDB.close();

        log.setText(logContent);
    }

    private class trackingThread extends Thread {

        private trackingThread() {
        }

        private trackingThread(String phone_number) {
            setPhoneNumber(phone_number);
        }

        private String phoneNumber = "";

        private void setPhoneNumber(String phone_number) {
            phoneNumber = phone_number;
        }

        @Override
        public void run() {
//            Looper.prepare();
//            handler = new Handler() {
//                public void handleMessage(Message msg) {
//                    // process incoming messages here
//                    log = (TextView) findViewById(R.id.logView);
//                    String logContent = "" + log.getText();
//
//                    while (keepTracking) {
//                        try {
//                            Log.d(">>>>>>>>>", "About to fall asleep...");
//                            Thread.sleep(100);
//                        } catch (InterruptedException e) {
//                            Log.w("Looping:: ", "Interrupt Exception while sleeping");
//                            Toast.makeText(getApplicationContext(), "Sleep interrupted", Toast.LENGTH_SHORT).show();
//                        }
//                        Log.d(">>>>>>>>>", "keepTracking is true - repeating operation :)");
//                        logContent += "...\n";
//                        log.setText(logContent);
//                    }
//                }
//            };
//            Looper.loop();
            while (true) {
                if (keepTracking) {
                    Log.d(">>>>>>>>>", "keepTracking is true - repeating operation :)");

                    // TODO: This string condition has not yet been tested
                    if (phoneNumber.isEmpty() || (phoneNumber == null)){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                EditText phoneNumberView = (EditText) findViewById(R.id.editPhone);
                                Button button = (Button) findViewById(R.id.startButton);

                                Toast.makeText(SMSTrackingActivity.this, "Please enter a phone number to track", Toast.LENGTH_LONG).show();
                                Log.w("Start Tracking-bgThread", "Attempting to start tracking without a phone number");
                                phoneNumberView.setFocusable(true);
                                phoneNumberView.setFocusableInTouchMode(true);
                                button.setText(getResources().getString(R.string.startButton));
                                keepTracking = false;
                            }
                        });
                    } else {
                        Long trackingStartTime = System.currentTimeMillis();

                        Message msg = Message.obtain();
                        msg.arg1 = 1;
                        handler.sendMessage(msg);

                        // Sending SMS to the tracker
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (ContextCompat.checkSelfPermission(SMSTrackingActivity.this,
                                        Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(phoneNumber, null, "6660000", null, null);
                                }
                            }
                        });

                        // Checking for response SMSs from the tracking device
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ContentResolver contentResolver = getContentResolver();
//        Cursor cursor = contentResolver.query(Uri.parse( "content://sms/inbox" ), null, null, null, null);

                                // cursor
                                Cursor cursor = contentResolver.query( Uri.parse("content://sms/inbox" ), null, null, null, null);
                                int indexBody = cursor.getColumnIndex("body");
                                int indexAddr = cursor.getColumnIndex("address");
                                int date = cursor.getColumnIndex("date");

                                if ( indexBody < 0 || !cursor.moveToFirst() ) return;
                                do {
                                    if (cursor.getString(indexAddr).contains(phoneNumber) ) {
                                        String body = cursor.getString(indexBody);
                                        if (body.contains("cellid")){
                                            Map<String, String> info = parseMessageBody(body);
                                            long newRowId = saveCellId(
                                                    "session_01_" + phoneNumber,
                                                    cursor.getString(date),
                                                    info.get("mcc"),
                                                    info.get("mnc"),
                                                    info.get("lac"),
                                                    info.get("cellid")
                                            );
                                            Toast.makeText(SMSTrackingActivity.this, "Row no." + Long.toString(newRowId) + " added", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                while( cursor.moveToNext() );
                                cursor.close();
                            }
                        });
                    }
                } else {
                    Log.d(">>>>>>>>>", "keepTracking is false - no operation");
                }
                // Sleeping
                try {
                    Log.d(">>>>>>>>>", "About to fall asleep...");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Log.w("Looping:: ", "Interrupt Exception while sleeping");
                    Toast.makeText(getApplicationContext(), "Sleep interrupted", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }
}
