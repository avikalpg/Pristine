package com.example.t_avgup.gsm_location_testing_app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.CookieStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private JSONArray retrieveCellInfo(TelephonyManager telephonyManager) {
        JSONArray cellList = new JSONArray();

        // Obtaining permission to get Cell data
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d("tag", "I want ACCESS_COARSE_LOCATION permissions");

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        // Obtaining cell data, depending on the version of Android in use
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            List<NeighboringCellInfo> neighCells = telephonyManager.getNeighboringCellInfo();
            for (int i = 0; i < neighCells.size(); i++) {
                try {
                    JSONObject cellObj = new JSONObject();
                    NeighboringCellInfo thisCell = neighCells.get(i);
                    cellObj.put("cellId", thisCell.getCid());
                    cellObj.put("lac", thisCell.getLac());
                    cellObj.put("rssi", thisCell.getRssi());
                    cellList.put(cellObj);
                } catch (Exception e) {
                    Log.d("Old Version Cell Info", "The App has detected an old version for the phone. This version has not been tested. Error in extracting cell data");
                    Toast.makeText(this, "Old Version: Error in Cell information retrieval", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            List<CellInfo> infos = telephonyManager.getAllCellInfo();
            for (int i = 0; i < infos.size(); ++i) {
                try {
                    JSONObject cellObj = new JSONObject();
                    CellInfo info = infos.get(i);
                    if (info instanceof CellInfoGsm) {
//                        phoneType = phoneType + "GSM";
                        CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                        CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                        cellObj.put("cellId", identityGsm.getCid());
                        cellObj.put("lac", identityGsm.getLac());
                        cellObj.put("dbm", gsm.getDbm());
                        cellList.put(cellObj);
                    } else if (info instanceof CellInfoLte) {
//                        phoneType = phoneType + "LTE";
                        CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                        CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                        cellObj.put("cellId", identityLte.getCi());
                        cellObj.put("tac", identityLte.getTac());
                        cellObj.put("dbm", lte.getDbm());
                        cellList.put(cellObj);
                    }
                } catch (Exception ex) {
                    Log.d("Cell Info", "Error in extracting cell data");
                    Toast.makeText(this, "Error in Cell information retrieval", Toast.LENGTH_SHORT).show();
                }
            }
        }

        return cellList;
    }

    private void sendHTTPRequest(final String deviceID, final String mcc, final String mnc, final JSONArray cellInfo) {

        final TextView mTextView = (TextView) findViewById(R.id.responseTextBox);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.43.150:8000/receiveCellInfo/location";

        Long tsLong = System.currentTimeMillis()/1000;
        String ext = "?device=" + deviceID + "&time=" + tsLong.toString() + "&mcc=" + mcc + "&mnc=" + mnc + "&cellinfo=" + cellInfo;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url + ext,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: "+ response.substring(0, java.lang.Math.min(500, response.length())));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        }){
//                @Override
//                protected Map<String, String> getParams() {
//                    Map<String, String> params = new HashMap<>();
//                    params.put("device", deviceID);
//                    Long tsLong = System.currentTimeMillis()/1000;
//                    params.put("time", tsLong.toString());
//                    params.put("mcc", mcc);
//                    params.put("mnc", mnc);
//                    params.put("cellinfo", cellInfo.toString());
//                    return params;
//                }
        };
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void transmitGSMInfo() {

        TextView t = (TextView) findViewById(R.id.outputView);
        TextView t2 = (TextView) findViewById(R.id.textView2);

        TelephonyManager telephonyManager = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));

        // I don't really know why is this useful
//        String operatorName = telephonyManager.getNetworkOperatorName();
//        int networkType = telephonyManager.getNetworkType();
        // Type of the network
//        int phoneTypeInt = telephonyManager.getPhoneType();
//        String phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_GSM ? "gsm" : null;
//        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_CDMA ? "cdma" : phoneType;
//        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_SIP ? "sip" : phoneType;
        // credits: http://stackoverflow.com/questions/23710045/android-location-using-cell-tower
//        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
//        int cellId = cellLocation.getCid();
//        int cellLac = cellLocation.getLac();
//        CellIdentityLte CID;
//        CellLocation location = CellLocation.getEmpty();
//        location = telephonyManager.getCellLocation();
//        CellLocation.requestLocationUpdate();

        // Getting the MCC and MNC
        String networkOperator = telephonyManager.getNetworkOperator();
        String mcc = networkOperator.substring(0, 3);
        String mnc = networkOperator.substring(3);

        String out = "MCC: " + mcc + "\nMNC: " + mnc;
//      out += "\nOperator Name: " + operatorName + "\nNetwork Type: " + networkType + "\nPhone Type: " + phoneType;

        assert t2 != null;
        t2.setText(out);

        JSONArray cellList = retrieveCellInfo(telephonyManager);

        assert t != null;
        t.setText(cellList.toString());

        sendHTTPRequest("G00001", mcc, mnc, cellList);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void startTransmitting(View view) {
        transmitGSMInfo();
    }
}
