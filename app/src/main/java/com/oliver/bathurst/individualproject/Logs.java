package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Oliver on 06/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class Logs {
    private final Context c;

    Logs(Context context){
        this.c = context;
    }

    String getContacts() {
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
    String getCallLog(int num) {
        ArrayList<String> arrayList = new ArrayList<>();

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
                    arrayList.add((c.getString(R.string.num)) + (calllog.getString(calllog.getColumnIndex(CallLog.Calls.NUMBER)))
                             + ("\n") + (c.getString(R.string.call_date)) + (calllog.getString(calllog.getColumnIndex(CallLog.Calls.DATE)))
                             + ("\n") + (c.getString(R.string.call_time)) + (new Date((long) calllog.getColumnIndex(CallLog.Calls.DATE)))
                             + ("\n") + (c.getString(R.string.call_duration)) + (calllog.getString(calllog.getColumnIndex(CallLog.Calls.DURATION)))
                             + ("\n") + (c.getString(R.string.call_type)) + (type) + ("\n"));
                }
            }
            if (calllog != null) {
                calllog.close();
            }
        }
        StringBuilder content = new StringBuilder();
        for(int i = 0; i < arrayList.size(); i++){
            if(i==num){
                break;
            }else{
                content.append(arrayList.get(i));
            }
        }
        return content.toString();
    }
}
