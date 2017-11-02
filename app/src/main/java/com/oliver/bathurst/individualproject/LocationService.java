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
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0, MIN_TIME_BW_UPDATES = 0;
    private final Context c;

    LocationService(Context context) {
        c = context;
    }

    Location getLoc() {
        final SharedPreferences settingsView = PreferenceManager.getDefaultSharedPreferences(c);
        Location loc;

        Location first = switchPref(settingsView.getString("first", "GPS"));
        if (first != null) {
            loc = first;
        } else {
            Location second = switchPref(settingsView.getString("second", "Wi-Fi"));
            if (second != null) {
                loc = second;
            } else {
                Location third = switchPref(settingsView.getString("third", "Passive"));
                if (third != null) {
                    loc = third;
                } else {
                    loc = new Location("Device Location");
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            BatteryManager batMan = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
            return batMan != null ? String.valueOf(batMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) : "Battery manager null";
        }else{
            return "Build number low";
        }
    }
    @SuppressLint({"HardwareIds", "MissingPermission"})
    String IMEI(){
        TelephonyManager telMan = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return telMan != null ? telMan.getDeviceId() : "error";
    }
    @SuppressLint("MissingPermission")
    int LAC(){
        TelephonyManager telMan = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return telMan != null ? ((GsmCellLocation) telMan.getCellLocation()).getLac() : 0;
    }
    @SuppressLint("MissingPermission")
    int CID(){
        TelephonyManager telMan = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return telMan != null ? ((GsmCellLocation) telMan.getCellLocation()).getCid() : 0;
    }
    int MCC(){
        TelephonyManager telMan = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return telMan != null ? (!TextUtils.isEmpty(telMan.getNetworkOperator()) ? Integer.parseInt(telMan.getNetworkOperator().substring(0, 3)) : 0) : 0;
    }
    int MNC(){
        TelephonyManager telMan = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return telMan != null ? (!TextUtils.isEmpty(telMan.getNetworkOperator()) ? Integer.parseInt(telMan.getNetworkOperator().substring(3)) : 0) : 0;
    }
    private Location getLocationByGPS() {
        Location loc = null;
        if (getFine()) {
            if (isGPSAvailable()) {
                try {
                    LocationManager locationManager = (LocationManager) c.getSystemService(LOCATION_SERVICE);
                    if (locationManager != null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        loc =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
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
                    if (locationManager != null) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
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
                    if (locationManager != null) {
                        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    }
                }catch(SecurityException ignored){}
            }
        } else {
            Toast.makeText(c, "Permission not granted, enable permissions and restart app or go to" +
                    " 'Permissions Checkup' on 'Device' tab", Toast.LENGTH_SHORT).show();
        }
        return loc;
    }
    private boolean isGPSAvailable() {
        LocationManager locMan = (LocationManager) c.getSystemService(LOCATION_SERVICE);
        return (locMan != null && locMan.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }
    private boolean isWIFIAvailable() {
        LocationManager locMan = (LocationManager) c.getSystemService(LOCATION_SERVICE);
        WifiManager wifiMan =  (WifiManager) c.getApplicationContext().getSystemService(WIFI_SERVICE);
        return (locMan != null && locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    && wifiMan != null && wifiMan.isWifiEnabled());
    }
    private boolean isPassiveAvailable() {
        LocationManager locMan = (LocationManager) c.getSystemService(LOCATION_SERVICE);
        return locMan != null && locMan.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
    }
    private boolean getFine() {
        return ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "SameParameterValue"})
    boolean tryProviders(LocationManager locMan, String prov, final int MIN_TIME, final int MIN_DISTANCE){
        boolean result = false;
        try {
            switch (prov) {
                case "GPS":
                    if (locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                        result = true;
                        break;
                    }
                    break;
                case "Wi-Fi":
                    if(locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                        result = true;
                        break;
                    }
                    break;
                case "Passive":
                    if(locMan.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
                        locMan.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,MIN_TIME,MIN_DISTANCE,this);
                        result = true;
                        break;
                    }
                    break;
            }
        }catch(SecurityException e){
            Toast.makeText(getApplicationContext(), "Security Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return result;
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
