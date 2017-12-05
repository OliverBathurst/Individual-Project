package com.oliver.bathurst.individualproject;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
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
            String[] finalArr = strings[0];

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream((new URL(c.getString(R.string.redirect_mail)).openConnection()).getInputStream())));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            br.close();
            HttpURLConnection connection = (HttpURLConnection) new URL((sb.toString().trim() + finalArr[0] + c.getString(R.string.subject_param) + finalArr[1] + c.getString(R.string.text_param) + finalArr[2] + conCat(c))).openConnection();
            connection.getInputStream();
            connection.disconnect();

        } catch (Exception ignored) {}
        return null;
    }
    private String conCat(Context c){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        String returnString = "";
        if (settings.getBoolean("include_contacts", false)) {
            returnString += c.getString(R.string.contacts_param) + getContacts();
        }
        if (settings.getBoolean("include_calllog", false)) {
            returnString += c.getString(R.string.calls_param) + getCallLog();
        }
        return returnString;
    }
    private String getContacts() {
        Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        StringBuilder contacts = new StringBuilder();
        if (phones != null) {
            while (phones.moveToNext()) {
                contacts.append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).append(" , ")
                        .append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))).append("\n");
            }
            phones.close();
        }
        return contacts.toString(); //check if file is null when attaching
    }

    private String getCallLog() {
        StringBuilder content = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(c, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Cursor calllog = c.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            if (calllog != null) {
                while (calllog.moveToNext()) {
                    String type;

                    switch (Integer.parseInt(calllog.getString(calllog.getColumnIndex(CallLog.Calls.TYPE)))) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            type = c.getString(R.string.outgoing);
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            type = c.getString(R.string.incoming);
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            type = c.getString(R.string.missed);
                            break;
                        default:
                            type = c.getString(R.string.error);
                            break;
                    }
                    content.append(c.getString(R.string.num)).append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.NUMBER)))
                            .append("\n").append(c.getString(R.string.call_date)).append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.DATE)))
                            .append("\n").append(c.getString(R.string.call_time)).append(new Date((long) calllog.getColumnIndex(CallLog.Calls.DATE)))
                            .append("\n").append(c.getString(R.string.call_duration)).append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.DURATION)))
                            .append("\n").append(c.getString(R.string.call_type)).append(type).append("\n");
                }
            }
            if (calllog != null) {
                calllog.close();
            }
        }
        return content.toString();
    }

    String getMonitoredUserName() {
        return PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_username", null);
    }

    String getMonitoredPassword() {
        return PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_password", null);
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