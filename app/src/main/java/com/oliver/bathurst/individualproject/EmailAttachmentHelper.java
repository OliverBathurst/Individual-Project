package com.oliver.bathurst.individualproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Oliver on 19/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class EmailAttachmentHelper {
    private final Context c;

    EmailAttachmentHelper(Context context) {
        c = context;
    }

    boolean isEmailValid(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        String user = settings.getString("gmail_username", null);
        String pass = settings.getString("gmail_password", null);
        return user != null && user.trim().length() != 0 && user.contains("@") && pass != null && pass.trim().length() != 0;
    }
    String getUserName(){
        return PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_username", null);
    }
    String getPassword(){
        return PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_password", null);
    }
    String getReceiver(){
        String receiversEmail = PreferenceManager.getDefaultSharedPreferences(c).getString("email_string", null);
        return (receiversEmail != null && receiversEmail.trim().length() != 0 && receiversEmail.contains("@")) ? receiversEmail.trim() : null;
    }

    private File getContacts() {
        Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        StringBuilder contacts = new StringBuilder();
        if (phones != null) {
            while (phones.moveToNext()) {
                contacts.append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).append(" , ")
                        .append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))).append("\n");
            }
            phones.close();
        }
        File contactsFile = null;

        try {
            contactsFile = new File(Environment.getExternalStorageDirectory(), "contacts.txt");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(contactsFile));
            writer.write(contacts.toString());
            writer.close();
        } catch (Exception ignored) {}

        return contactsFile; //check if file is null when attaching
    }

    private File getCallLog() {
        File callLogFile = null;
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Cursor calllog = c.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);

            StringBuilder content = new StringBuilder();

            if (calllog != null) {
                while (calllog.moveToNext()) {
                    String type;

                    switch (Integer.parseInt(calllog.getString(calllog.getColumnIndex(CallLog.Calls.TYPE)))) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            type = "Outgoing";
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            type = "Incoming";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            type = "Missed";
                            break;
                        default:
                            type = "Error";
                            break;
                    }

                    content.append("Number: ").append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.NUMBER)))
                            .append("\nCall Date: ").append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.DATE)))
                            .append("\nCall Time: ").append(new Date((long) calllog.getColumnIndex(CallLog.Calls.DATE)))
                            .append("\nCall Duration: ").append(calllog.getString(calllog.getColumnIndex(CallLog.Calls.DURATION)))
                            .append("\nCall Type: ").append(type).append("\n");
                }
            }
            if (calllog != null) {
                calllog.close();
            }

            try {
                callLogFile = new File(Environment.getExternalStorageDirectory(), "calllog.txt");
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(callLogFile));
                writer.write(content.toString());
                writer.close();
            } catch (Exception ignored) {}
        }
        return callLogFile;
    }

    void attachFiles(GMailSender sender) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        ///contacts
        if (settings.getBoolean("include_contacts", false)) {
            File contact = getContacts();
            if (contact != null) {
                try {
                    sender.addAttachment(contact.getAbsolutePath());
                } catch (Exception ignored) {
                }
            }
        }
        //call log
        if (settings.getBoolean("include_calllog", false)) {
            File calllog = getCallLog();
            if (calllog != null) {
                try {
                    sender.addAttachment(calllog.getAbsolutePath());
                } catch (Exception ignored) {
                }
            }
        }
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    String getEmailString() {
        String emailBody = "";
        try {
            final WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            ConnectivityManager networkInfo = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo net = null;
            if (networkInfo != null) {
                net = networkInfo.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            }
            TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            Location loc = new LocationService(c).getLoc();

            emailBody = "This is a location alert, your device location is: " + loc.getLatitude()
                    + "," + loc.getLongitude()
                    + "\nGoogleMaps link: http://maps.google.com/?q=" + loc.getLatitude()
                    + "," + loc.getLongitude()
                    + "\nTime Declared: " + DateFormat.getDateTimeInstance().format(new Date())
                    + "\nDeclared by: " + loc.getProvider()
                    + "\nAccuracy: " + loc.getAccuracy()
                    + "\nWiFi enabled? " + (wifiManager != null && wifiManager.isWifiEnabled())
                    + "\nSSID: " + (wifiManager != null ? wifiManager.getConnectionInfo().getSSID() : "null")
                    + "\nIP: " + (wifiManager != null ? wifiManager.getConnectionInfo().getIpAddress() : 0)
                    + "\nMobile network enabled? " + (net != null && net.isConnected())
                    + "\nNetwork type: " + (net != null ? net.getType() : "error")
                    + " Network name: " + (net != null ? net.getTypeName() : "error")
                    + "\nExtra info: " + (net != null ? net.getExtraInfo() : "error")
                    + "\nBattery: " + getBattery(c)
                    + "\nIMEI: " + (telephonyManager != null ? telephonyManager.getDeviceId() : "null")
                    + "\nPhone number: " + (telephonyManager != null ? telephonyManager.getLine1Number() : "null")
                    + "\nSIM Serial: " + (telephonyManager != null ? telephonyManager.getSimSerialNumber() : "null");
        }catch(Exception ignored){}
        return emailBody;
    }
    private String getBattery(Context c){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            BatteryManager batMan = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
            return batMan != null ? String.valueOf(batMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) : "Battery manager is null";
        }else{
            return "Build number low";
        }
    }
}