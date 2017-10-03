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

    private File getContacts() {
        Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String contacts = null;
        if (phones != null) {
            while (phones.moveToNext()) {
                contacts += phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        + " , " + phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + "\n";
            }
            phones.close();
        }
        File contactsFile = null;
        if (contacts != null) {
            try {
                contactsFile = new File(Environment.getExternalStorageDirectory(), "contacts.txt");
                FileOutputStream fileInput = new FileOutputStream(contactsFile);
                OutputStreamWriter writer = new OutputStreamWriter(fileInput);
                writer.write(contacts);
                writer.close();
                fileInput.close();
            } catch (Exception ignored) {}
        }
        return contactsFile; //check if file is null when attaching
    }

    private File getCallLog() {
        File callLogFile = null;
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Cursor calllog = c.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);

            String content = null;

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

                    content += "Number: " + calllog.getString(calllog.getColumnIndex(CallLog.Calls.NUMBER))
                            + "\nCall Date: " + calllog.getString(calllog.getColumnIndex(CallLog.Calls.DATE))
                            + "\nCall Time: " + new Date((long) calllog.getColumnIndex(CallLog.Calls.DATE))
                            + "\nCall Duration: " + calllog.getString(calllog.getColumnIndex(CallLog.Calls.DURATION))
                            + "\nCall Type: " + type + "\n";
                }
            }
            if (calllog != null) {
                calllog.close();
            }

            if(content!=null){
                try {
                    callLogFile = new File(Environment.getExternalStorageDirectory(), "calllog.txt");
                    FileOutputStream fileInput = new FileOutputStream(callLogFile);
                    OutputStreamWriter writer = new OutputStreamWriter(fileInput);
                    writer.write(content);
                    writer.close();
                    fileInput.close();
                } catch (Exception ignored) {}
            }
        }
        return callLogFile;
    }
    void attachFiles(GMailSender sender){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        boolean incContacts = settings.getBoolean("include_contacts", false);
        boolean incCallLog = settings.getBoolean("include_calllog", false);

        ///contacts
        if(incContacts) {
            File contact = getContacts();
            if (contact != null) {
                try {
                    sender.addAttachment(contact.getAbsolutePath());
                } catch (Exception ignored) {}
            }
        }
        //call log
        if(incCallLog) {
            File calllog = getCallLog();
            if (calllog != null) {
                try {
                    sender.addAttachment(calllog.getAbsolutePath());
                } catch (Exception ignored) {}
            }
        }
    }
    @SuppressWarnings("deprecation")
    @SuppressLint("HardwareIds")
    String getEmailString(){
        String emailBody = "";
        try {
            final WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            TelephonyManager telephonyManager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);

            LocationService locationService = new LocationService(c);
            Location loc = locationService.getLoc();


            emailBody = "This is a location alert, your device location is: " + loc.getLatitude()
                    + "," + loc.getLongitude()
                    + "\nGoogleMaps link: http://maps.google.com/?q=" + loc.getLatitude()
                    + "," + loc.getLongitude()
                    + "\nTime Declared: " + DateFormat.getDateTimeInstance().format(new Date())
                    + "\nDeclared by: " + loc.getProvider() + "\nAccuracy: " + loc.getAccuracy()
                    + "\nWiFi enabled? " + wifiManager.isWifiEnabled() + "\nSSID: " + wifiManager.getConnectionInfo().getSSID()
                    + "\nIP: " + wifiManager.getConnectionInfo().getIpAddress()
                    + "\nMobile network enabled? " + networkInfo.isConnected()
                    + "\nNetwork type: " + networkInfo.getType() + " Network name: " + networkInfo.getTypeName()
                    + "\nExtra info: " + networkInfo.getExtraInfo()
                    + "\nBattery: " + getBattery(c)
                    + "\nIMEI: " +  telephonyManager.getDeviceId()
                    + "\nPhone number: " + telephonyManager.getLine1Number()
                    + "\nSIM Serial: " + telephonyManager.getSimSerialNumber();
        }catch(Exception ignored){}
        return emailBody;
    }

    private String getBattery(Context c){
        String life = "Build number low";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager bm = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
            life = String.valueOf(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
        }
        return life;
    }
}