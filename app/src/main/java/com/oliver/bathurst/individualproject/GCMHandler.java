package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

/**
 * Created by Oliver on 06/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class GCMHandler {
    private final Bundle toExamine;
    private final Context context;
    private final String message;

    GCMHandler(String str, Bundle data, Context c){
        this.message = str;
        this.toExamine = data;
        this.context = c;
    }

    void examine(){
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        int ringVol = shared.getInt("seek_bar_volume", 90);
        String lock = shared.getString("lock_gcm", null);
        String ring = shared.getString("gcm_ring", null);
        String ringDur = shared.getString("ring_duration", null);
        String ringtone = shared.getString("ringtone_select", null);
        String stolen = shared.getString("sms_stolen_gcm", null);
        String wipe_gcm = shared.getString("wipe_gcm", null);
        String wifi_gcm = shared.getString("enable_wifi_gcm", null);
        String sms_gcm = shared.getString("send_sms_gcm", null);
        String send_email_gcm = shared.getString("send_email_gcm", null);
        String email_string = shared.getString("email_string", null);
        String secondary_phone = shared.getString("secondary_phone", null);
        String torch_gcm = shared.getString("turn_torch_on_gcm", null);
        String gcm_relay_location = shared.getString("gcm_location_relay", null);
        String toggle_hiding_gcm = shared.getString("toggle_hiding_gcm", null);
        String wipe_sd_gcm = shared.getString("wipe_sd_gcm", null);
        String gcm_beacon_relay = shared.getString("gcm_beacon_relay", null);
        String gcm_calls_relay = shared.getString("gcm_calls_relay", null);
        String gcm_contacts_relay = shared.getString("gcm_contacts_relay", null);

        String extras = toExamine.getString("extra");
        String relay = toExamine.getString("sender");


        if(message.equals("testing")){
            new PostPHP(context).execute(new String[]{"oliverbathurst12345@gmail.com", "title", "message"});
            //context.sendBroadcast(new Intent().setAction("oliver.intent.action.GCM").putExtra("STRING", new String[]{"test_function"}));
        }
        if(comparator(gcm_relay_location)){
            if(relay != null && !relay.equals("null")){
                context.sendBroadcast(new Intent().setAction("oliver.intent.action.GCM").putExtra("STRING", new String[]{"location", relay}));
            }
        }
        if(comparator(gcm_beacon_relay)){
            if(relay != null && !relay.equals("null")){
                new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{relay, new NearbyBeacons(context).run()});
            }
        }
        if(comparator(gcm_calls_relay)){
            if(relay != null && !relay.equals("null")){
                int calls = 5;
                if(extras != null && !extras.equals("null")){
                    try {
                        calls = Integer.parseInt(extras);
                    }catch(Exception e){calls = 5;}
                }
                new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{relay, new Logs(context).getCallLog(calls)});
            }
        }
        if(comparator(gcm_contacts_relay)){
            if(relay != null && !relay.equals("null")){
                new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{relay, new Logs(context).getContacts()});
            }
        }
        if(comparator(wipe_sd_gcm)){
            new SDWiper().wipeSD();
        }
        if(comparator(toggle_hiding_gcm)){
            new HideApp(context).toggle();
        }
        if(comparator(torch_gcm)){
            new Torch(context).toggle();
        }
        if(comparator(lock)) {
            new PolicyManager(context).lockPhone();
        }
        if(comparator(wipe_gcm)) {
            new PolicyManager(context).wipePhone();
        }
        if(comparator(ring)) {
            new Alarm(context,ringVol,ringDur,ringtone).ring();
        }
        if(comparator(stolen)){
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("stolen", true).apply();
        }
        if(comparator(wifi_gcm)){
            WifiManager wMan = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
            if(wMan != null && wMan.isWifiEnabled()){
                wMan.setWifiEnabled(true);
            }
        }
        if(comparator(sms_gcm)){
            if(extras != null && !extras.equals("null")){
                SmsManager.getDefault().sendTextMessage(extras, null, new SMSHelper(context).getBody(), null, null);
            }else if(secondary_phone != null && secondary_phone.trim().length() > 0){
                SmsManager.getDefault().sendTextMessage(secondary_phone, null, new SMSHelper(context).getBody(), null, null);
            }
        }
        if(comparator(send_email_gcm)){
            if(extras != null && !extras.equals("null")){
                context.sendBroadcast(new Intent().setAction("oliver.intent.action.GCM").putExtra("STRING", new String[]{"email_send_loc", extras, context.getString(R.string.location_update_title)}));
            }else if(email_string != null && email_string.trim().length() > 0 &&  email_string.contains("@")) {
                context.sendBroadcast(new Intent().setAction("oliver.intent.action.GCM").putExtra("STRING", new String[]{"email_send_loc", email_string, context.getString(R.string.location_update_title)}));
            }
        }
    }
    private boolean comparator(String compare){
        return (compare != null && message.equals(compare));
    }
}
