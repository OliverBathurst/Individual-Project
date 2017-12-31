package com.oliver.bathurst.individualproject;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Oliver on 03/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class PostPHP extends AsyncTask<String[], Void, Void> {
    @SuppressLint("StaticFieldLeak")
    private final Context c;

    PostPHP(Context context) {
        this.c = context;
    }

    @Override
    protected Void doInBackground(String[]... strings) {
        try {
            String[] finalArr = strings[0];//get the first string array

            StringBuilder sb = new StringBuilder();//new string storage for the URL
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream((new URL(c.getString(R.string.redirect_mail)).openConnection()).getInputStream())));//open connection to text file
            String inputLine;
            while ((inputLine = br.readLine()) != null) { //read each line of the file
                sb.append(inputLine); //append line of file to string builder
            }
            br.close();//close the connection
            HttpURLConnection connection = (HttpURLConnection) new URL((sb.toString().trim() +c.getString(R.string.to) + finalArr[0] + c.getString(R.string.subject_param) + finalArr[1] + c.getString(R.string.text_param) + finalArr[2] + addExtraInfo(c))).openConnection();
            connection.getInputStream(); //open a connection to the modified URL with parameters taken from the string array
            connection.disconnect(); //finally disconnect

        } catch (Exception ignored) {}
        return null;
    }
    private String addExtraInfo(Context c){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        String returnString = " ";
        Logs l = new Logs(c);
        if (settings.getBoolean("include_contacts", false)) {
            returnString += l.getContacts();
        }
        if (settings.getBoolean("include_calllog", false)) {
            returnString += l.getCallLog(10);
        }
        return returnString;
    }

    String getReceiver() {
        String receiversEmail = PreferenceManager.getDefaultSharedPreferences(c).getString("email_string", null);
        return (receiversEmail != null && receiversEmail.trim().length() != 0 && receiversEmail.contains("@")) ? receiversEmail.trim() : null;
    }

    @SuppressLint("HardwareIds")
    String getEmailString() {
        final WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager networkInfo = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean pref = PreferenceManager.getDefaultSharedPreferences(c).getBoolean("cell_tower_email", false);

        NetworkInfo net = null;
        if (networkInfo != null) {
            net = networkInfo.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        }

        TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        Location loc = new LocationService(c).getLoc();

        return c.getString(R.string.location_alert_title) + loc.getLatitude() + "," + loc.getLongitude() + " "
                + "\n" + c.getString(R.string.gmaps_syntax) + loc.getLatitude() + "," + loc.getLongitude() + " "
                + "\n" + c.getString(R.string.time_declared) + DateFormat.getDateTimeInstance().format(new Date()) + " "
                + "\n" + c.getString(R.string.declared_by) + loc.getProvider() + " "
                + "\n" + c.getString(R.string.accuracy) + loc.getAccuracy() + " "
                + "\n" + c.getString(R.string.isWiFiEnabled) + (wifiManager != null && wifiManager.isWifiEnabled()) + " "
                + "\n" + c.getString(R.string.ssid) + (wifiManager != null ? wifiManager.getConnectionInfo().getSSID() : c.getString(R.string.null_value_string)) + " "
                + "\n" + c.getString(R.string.ipaddr) + (wifiManager != null ? wifiManager.getConnectionInfo().getIpAddress() : 0) + " "
                + "\n" + c.getString(R.string.mobile_network) + (net != null && net.isConnected()) + " "
                + "\n" + c.getString(R.string.net_type) + (net != null ? net.getType() : c.getString(R.string.error_string)) + " "
                + "\n" + c.getString(R.string.net_name) + (net != null ? net.getTypeName() : c.getString(R.string.error_string)) + " "
                + "\n" + c.getString(R.string.extra_info) + (net != null ? net.getExtraInfo() : c.getString(R.string.error_string)) + " "
                + "\n" + c.getString(R.string.batt_level) + getBattery(c) + " "
                + "\n" + c.getString(R.string.imei) + ((telephonyManager != null && ActivityCompat.checkSelfPermission(c, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) ? telephonyManager.getDeviceId() : c.getString(R.string.null_value_string)) + " "
                + "\n" + c.getString(R.string.phone_number) + ((telephonyManager != null && ActivityCompat.checkSelfPermission(c, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) ? telephonyManager.getLine1Number() : c.getString(R.string.null_value_string))+ " "
                + "\n" + c.getString(R.string.sim_serial) + ((telephonyManager != null && ActivityCompat.checkSelfPermission(c, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) ? telephonyManager.getSimSerialNumber() : c.getString(R.string.null_value_string))+ " "
                + (pref ? ("\n" + c.getString(R.string.cell_tower_info) + new CellTowerHelper(c).getAll()) : "");
    }
    private String getBattery(Context c){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            BatteryManager batMan = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
            return batMan != null ? String.valueOf(batMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) : c.getString(R.string.batt_manager_null);
        }else{
            return c.getString(R.string.build_number_low);
        }
    }
}