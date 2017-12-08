package com.oliver.bathurst.individualproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import org.json.JSONObject;

import static android.support.v4.app.ActivityCompat.requestPermissions;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class PermissionsManager {
    private final Context c;

    PermissionsManager(Context context) {
        c = context;
    }

    JSONObject signupDeviceJson(){
        JSONObject json;
        try {
            String j = "{" + c.getString(R.string.model).trim()+ android.os.Build.MODEL + "," + c.getString(R.string.brand).trim() + Build.BRAND + ","
                    + c.getString(R.string.device).trim() + Build.DEVICE + "," + c.getString(R.string.display).trim() + Build.DISPLAY + ","
                    + c.getString(R.string.manufacturer).trim() + Build.MANUFACTURER + "}";
            json = new JSONObject(j);
        }catch(Exception e){
            json = null;
        }
        return json;
    }

    String getDeviceAttributes(){
        Location lastLoc = new LocationService(c).getLoc();
        return c.getString(R.string.model) + android.os.Build.MODEL + "\n" + c.getString(R.string.brand) + Build.BRAND + "\n" + c.getString(R.string.device)
                + Build.DEVICE + "\n" + c.getString(R.string.display) + Build.DISPLAY
                + "\n" + c.getString(R.string.manufacturer) + Build.MANUFACTURER + "\n"
                + c.getString(R.string.last_known) + lastLoc.getLatitude()
                + " , " + lastLoc.getLongitude() + "\n" + c.getString(R.string.accuracy) + lastLoc.getAccuracy();
    }
    String getCellInfo(){
        LocationService locationService = new LocationService(c);
        return c.getString(R.string.imei) + locationService.IMEI()
                + "\n" + c.getString(R.string.lac) + locationService.LAC() + "\n" + c.getString(R.string.cid) + locationService.CID() + "\n" + c.getString(R.string.mcc) + locationService.MCC()
                + "\n" + c.getString(R.string.mnc) + locationService.MNC();
    }

    String getAndroidVersion(){
        return c.getString(R.string.release_version) + Build.VERSION.RELEASE + "\n" + c.getString(R.string.version_name)
                + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

    String getAppInfo(){
        try {
            return c.getString(R.string.version) + c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
        }catch(Exception e){
            return c.getString(R.string.version_not_found);
        }
    }
    void permissionsCheckup(){
        if(!getSummary()){
            requestPermissions((Activity) c, new String[]{Manifest.permission.READ_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.CAMERA}, 1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions((Activity) c, new String[]{Manifest.permission.READ_CALL_LOG},1);
            }
        }
    }
    private boolean getSummary(){
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
        boolean cam = ActivityCompat.checkSelfPermission(c, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean readCallLog = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN && ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED;

        if(accessFine && sendSMS && readSMS && receiveSMS && accessWIFI && accessCoarse && accessPhoneState && changeWIFI && writeExtern && internet
                && accessNet && readContacts && readCallLog && blue && blueAdmin && cam){
            Toast.makeText(c, c.getString(R.string.all_perms_granted), Toast.LENGTH_SHORT).show();
            return true;
        }else{
            Toast.makeText(c, c.getString(R.string.perform_permissions_checkup), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}