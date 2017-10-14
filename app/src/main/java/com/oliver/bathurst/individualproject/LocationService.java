package com.oliver.bathurst.individualproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings("DefaultFileTemplate")
public class LocationService extends Service implements LocationListener {
    String DECLARED_BY = "";
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0, MIN_TIME_BW_UPDATES = 0;
    private final Context c;

    LocationService(Context context) {
        c = context;
    }

    Location getLoc() {
        final SharedPreferences settingsView = PreferenceManager.getDefaultSharedPreferences(c);
        Location loc;
        
        Location gps = switchPref(settingsView.getString("first", "GPS"));
        if (gps != null) {
            loc = gps;
            DECLARED_BY = "GPS";
        } else {
            Location wifi = switchPref(settingsView.getString("second", "Wi-Fi"));
            if (wifi != null) {
                loc = wifi;
                DECLARED_BY = "Wi-Fi";
            } else {
                Location pass = switchPref(settingsView.getString("third", "Passive"));
                if (pass != null) {
                    loc = pass;
                    DECLARED_BY = "Passive";
                } else {
                    loc = new Location("Device Location");
                    DECLARED_BY = "ERROR!";
                }
            }
        }
        return loc;
    }
    private Location switchPref(String provider){
        Location loc = null;
        switch (provider) {
            case "GPS":
                loc = getLocationByGPS();
                break;
            case "Wi-Fi":
                loc = getLocationByWIFI();
                break;
            case "Passive":
                loc = getLocationByPassive();
                break;
        }
        return loc;
    }
    String batteryLife() {
        String life = "Build number low";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager bm = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
            life = String.valueOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        }
        return life;
    }
    @SuppressLint("HardwareIds")
    String IMEI(){
        return ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }
    int LAC(){
        return ((GsmCellLocation) ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE)).getCellLocation()).getLac();
    }
    int CID(){
        return ((GsmCellLocation) ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE)).getCellLocation()).getCid();
    }
    int MCC(){
        String networkOperator = ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperator();
        return !TextUtils.isEmpty(networkOperator) ? Integer.parseInt(networkOperator.substring(0, 3)) : 0;
    }
    int MNC(){
        String networkOperator = ((TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperator();
        return !TextUtils.isEmpty(networkOperator) ? Integer.parseInt(networkOperator.substring(3)) : 0;
    }
    private Location getLocationByGPS() {
        Location loc = null;
        if (getFine()) {
            if (isGPSAvailable()) {
                try {
                    LocationManager locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    loc =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }catch(SecurityException ignored){}
            }
        } else {
            Toast.makeText(c, "Permission not granted, enable permissions and restart app or go to" +
                    "'Permissions Checkup' on 'Device' tab", Toast.LENGTH_SHORT).show();
        }
        return loc;
    }

    private Location getLocationByWIFI() {
        Location loc = null;
        if (getFine()) {
            if (isWIFIAvailable()) {
                try {
                    LocationManager locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }catch(SecurityException ignored){}
            }
        } else {
            Toast.makeText(c, "Permission not granted, enable permissions and restart app or go to" +
                    "'Permissions Checkup' on 'Device' tab", Toast.LENGTH_SHORT).show();
        }
        return loc;
    }

    private Location getLocationByPassive() {
        Location loc = null;
        if (getFine()) {
            if (isPassiveAvailable()) {
                try {
                    LocationManager locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);
                    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                }catch(SecurityException ignored){}
            }
        } else {
            Toast.makeText(c, "Permission not granted, enable permissions and restart app or go to" +
                    " 'Permissions Checkup' on 'Device' tab", Toast.LENGTH_SHORT).show();
        }
        return loc;
    }
    private boolean isGPSAvailable() {
        LocationManager lm = (LocationManager) c.getSystemService(LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    private boolean isWIFIAvailable() {
        WifiManager wifiMan = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        LocationManager lm = (LocationManager) c.getSystemService(LOCATION_SERVICE);

        return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && wifiMan.isWifiEnabled();
    }
    private boolean isPassiveAvailable() {
        LocationManager lm = (LocationManager) c.getSystemService(LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
    }
    private boolean getFine() {
        return ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onLocationChanged(Location location) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
