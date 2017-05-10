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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView t = (TextView) findViewById(R.id.outputView);
        TextView t2 = (TextView) findViewById(R.id.textView2);

        TelephonyManager telephonyManager = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
        String operatorName = telephonyManager.getNetworkOperatorName();
        int networkType = telephonyManager.getNetworkType();
        String networkOperator = telephonyManager.getNetworkOperator();
        String mcc = networkOperator.substring(0, 3);
        String mnc = networkOperator.substring(3);

        // Type of the network
        int phoneTypeInt = telephonyManager.getPhoneType();
        String phoneType = null;
        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_GSM ? "gsm" : phoneType;
        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_CDMA ? "cdma" : phoneType;
        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_SIP ? "sip" : phoneType;


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
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        // credits: http://stackoverflow.com/questions/23710045/android-location-using-cell-tower
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        int cellId = cellLocation.getCid();
        int cellLac = cellLocation.getLac();


        JSONArray cellList = new JSONArray();

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
                    ;
                }
            }

        } else {
            List<CellInfo> infos = telephonyManager.getAllCellInfo();
            for (int i = 0; i < infos.size(); ++i) {
                try {
                    JSONObject cellObj = new JSONObject();
                    CellInfo info = infos.get(i);
                    if (info instanceof CellInfoGsm) {
                        phoneType = phoneType + "GSM";
                        CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                        CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                        cellObj.put("cellId", identityGsm.getCid());
                        cellObj.put("lac", identityGsm.getLac());
                        cellObj.put("dbm", gsm.getDbm());
                        cellList.put(cellObj);
                    } else if (info instanceof CellInfoLte) {
                        phoneType = phoneType + "LTE";
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
                    ;
                }
            }
        }

        t2.setText(cellList.toString());

        Log.d("CellLocation", cellLocation.toString());
        Log.d("GSM CELL ID", String.valueOf(cellId));
        Log.d("GSM Location Code", String.valueOf(cellLac));


//        CellIdentityLte CID;
//        CellLocation location = CellLocation.getEmpty();
//        location = telephonyManager.getCellLocation();
//        CellLocation.requestLocationUpdate();

        String out = "Operator Name: " + operatorName + "\nNetwork Type: " + networkType + "\nMCC: " + mcc + "\nMNC: " + mnc + "\nPhone Type: " + phoneType + "\nCID: " + cellId + "\nLAC: " + cellLac + "\n" + cellLocation.toString();

        t.setText(out);
    }
}
