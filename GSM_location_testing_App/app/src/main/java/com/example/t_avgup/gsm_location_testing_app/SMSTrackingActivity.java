package com.example.t_avgup.gsm_location_testing_app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContentResolverCompat;
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

import org.w3c.dom.Text;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class SMSTrackingActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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

            // TODO: Remove this code segment after testing reading inbox
        }

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

        String[] smsNo = new String[] { "9580264736" };
        // cursor
        Cursor cursor = contentResolver.query( Uri.parse("content://sms/inbox" ), null, null, null, null);

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            logContent += cursor.getColumnNames()[i] + ", ";
        }
        logContent += "\n";
        log.setText(logContent);
        Context context = getApplicationContext();
        int indexBody = cursor.getColumnIndex("body");
        int indexAddr = cursor.getColumnIndex("address");
        int date = cursor.getColumnIndex("date");

        if ( indexBody < 0 || !cursor.moveToFirst() ) return;
        do {
            if (cursor.getString(indexAddr).contains("9580264736") ) {
//                String str = "Sender: " + cursor.getString(indexAddr) + "\n" + cursor.getString(indexBody);
                String body = cursor.getString(indexBody);
                String str = "";
                if (body.contains("cellid")){
                    Map<String, String> info = parseMessageBody(body);
                    str += cursor.getString(date) + " :: " + info.toString();
                }
                logContent += str + "\n";
            }
        }
        while( cursor.moveToNext() );
        log.setText(logContent);
    }
}
