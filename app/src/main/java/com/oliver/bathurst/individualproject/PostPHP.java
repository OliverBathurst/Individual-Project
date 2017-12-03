package com.oliver.bathurst.individualproject;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by Oliver on 03/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class PostPHP extends AsyncTask<String[],Void,Void>{
    @SuppressLint("StaticFieldLeak")
    private Context c;

    PostPHP(Context context){
        this.c = context;
    }
    @Override
    protected Void doInBackground(String[]... strings) {
        try {
            String[] finalArr = strings[0];
            String url = ("https://oliverbathurst9.000webhostapp.com/mail.php?to=" + finalArr[0] + "&subject=" + finalArr[1] + "&text=" + finalArr[2]);

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
            if (settings.getBoolean("include_contacts", false)) {
                url += "&contacts=" + getContacts();
            }
            if (settings.getBoolean("include_calllog", false)) {
                url += "&calls=" + getCallLog();
            }

            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();

            InputStreamReader isw = new InputStreamReader(connection.getInputStream());
            int data = isw.read();
            while (data != -1) {
                data = isw.read();
                System.out.print((char) data);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private String getContacts() {
        Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        StringBuilder contacts = new StringBuilder();
        if (phones != null) {
            while (phones.moveToNext()) {
                contacts.append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))).append(" , ")
                        .append(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))).append(" ");
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
}
