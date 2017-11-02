package com.oliver.bathurst.individualproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class PermissionsManager {
    private final Context c;

    PermissionsManager(Context context) {
        c = context;
    }

    String getDeviceAttributes(){
        Location lastLoc = new LocationService(c).getLoc();
        return "Model: " + android.os.Build.MODEL +"\nBrand: "+ Build.BRAND +"\nDevice: "+ Build.DEVICE +"\nDisplay: "+ Build.DISPLAY
                +"\nManufacturer: "+ Build.MANUFACTURER +"\nModel: "+ Build.MODEL + "\nLast Known Coordinates: " + lastLoc.getLatitude()
                + " , " + lastLoc.getLongitude() + "\nAccuracy: " + lastLoc.getAccuracy();
    }
    String getCellInfo(){
        LocationService locationService = new LocationService(c);
        return "IMEI: " + locationService.IMEI()
                + "\nLAC: " + locationService.LAC() + "\nCID: " + locationService.CID() + "\nMCC: " + locationService.MCC()
                + "\nMNC: " + locationService.MNC();
    }

    String getAndroidVersion(){
        return "Release Version: " + Build.VERSION.RELEASE + "\nVersion Name: "
                + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

    String getAppInfo(){
        try {
            return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
        }catch(Exception e){
            return "Version name not found";
        }
    }
    void permissionsCheckup(){
        getSummary();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions((Activity) c, new String[]{Manifest.permission.READ_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG}, 1);
        }else{
            Toast.makeText(c, "Build number low, please enable permissions manually.",Toast.LENGTH_LONG).show();
        }
    }
    private void getSummary(){
        boolean accessFine = ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean sendSMS = ActivityCompat.checkSelfPermission(c, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean readSMS = ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean receiveSMS = ActivityCompat.checkSelfPermission(c, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean accessWIFI = ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean accessCoarse = ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean accessPhoneState = ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean changeWIFI = ActivityCompat.checkSelfPermission(c, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean writeExtern = ActivityCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean internet = ActivityCompat.checkSelfPermission(c, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        boolean accessNet = ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean readContacts = ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean blue = ActivityCompat.checkSelfPermission(c, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
        boolean blueAdmin = ActivityCompat.checkSelfPermission(c, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        boolean readCallLog = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN &&
                ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;

        if(accessFine && sendSMS && readSMS && receiveSMS && accessWIFI && accessCoarse
                && accessPhoneState && changeWIFI && writeExtern && internet
                && accessNet && readContacts && readCallLog && blue && blueAdmin){
            Toast.makeText(c, "All permissions granted.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(c, "Not all permissions granted, perform a permissions checkup.", Toast.LENGTH_SHORT).show();
        }
    }
}