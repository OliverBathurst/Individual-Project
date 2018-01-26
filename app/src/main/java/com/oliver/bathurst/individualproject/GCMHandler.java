package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

/**
 * Created by Oliver on 06/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class GCMHandler {
    private final Context context;
    private final String message, extras, relay;

    GCMHandler(String str, Bundle data, Context c){
        this.message = str;
        this.context = c;
        this.extras = data.getString("extra");
        this.relay = data.getString("sender");
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
        String secondary_phone = shared.getString("secondary_phone", null);
        String torch_gcm = shared.getString("turn_torch_on_gcm", null);
        String gcm_relay_location = shared.getString("gcm_location_relay", null);
        String toggle_hiding_gcm = shared.getString("toggle_hiding_gcm", null);
        String wipe_sd_gcm = shared.getString("wipe_sd_gcm", null);
        String gcm_beacon_relay = shared.getString("gcm_beacon_relay", null);
        String gcm_calls_relay = shared.getString("gcm_calls_relay", null);
        String gcm_contacts_relay = shared.getString("gcm_contacts_relay", null);
        String gcm_cell_tower_relay = shared.getString("gcm_cell_tower_relay", null);
        String gcm_fingerprint_relay = shared.getString("gcm_fingerprint_relay", null);

        if(comparator(gcm_fingerprint_relay) && extraComparator(relay)){
            new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{relay, new WiFiFingerprinter(context).getResults()});
        }
        if(comparator(gcm_relay_location) && extraComparator(relay)){
            context.sendBroadcast(new Intent().setAction("oliver.intent.action.GCM").putExtra("STRING", new String[]{"location", relay}));
        }
        if(comparator(gcm_beacon_relay) && extraComparator(relay)){
            new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{relay, new BTNearby(context).run()});
        }
        if(comparator(gcm_cell_tower_relay) && extraComparator(relay)){
            new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{relay, new CellTowerHelper(context).getAll()});
        }
        if(comparator(gcm_calls_relay) && extraComparator(relay)){
            int calls = 5;
            if(extraComparator(extras)){
                try {
                    calls = Integer.parseInt(extras);
                }catch(Exception e){calls = 5;}
            }
            new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{relay, new Logs(context).getCallLog(calls)});
        }
        if(comparator(gcm_contacts_relay) && extraComparator(relay)){
            new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{relay, new Logs(context).getContacts()});
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
            if(wMan != null && !wMan.isWifiEnabled()){
                wMan.setWifiEnabled(true);
            }
        }
        if(comparator(sms_gcm)){
            if(extraComparator(extras)){
                SmsManager.getDefault().sendTextMessage(extras, null, new SMSHelper(context).getBody(), null, null);
            }else if(secondary_phone != null && secondary_phone.trim().length() > 0){
                SmsManager.getDefault().sendTextMessage(secondary_phone, null, new SMSHelper(context).getBody(), null, null);
            }
        }
    }
    private boolean comparator(String compare){
        return (compare != null && message.equals(compare));
    }
    private boolean extraComparator(String compare){
        return (compare != null && !compare.equals("null"));
    }
}