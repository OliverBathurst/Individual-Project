package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

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
        String lock = shared.getString("lock_gcm", null);
        String ring = shared.getString("gcm_ring", null);
        String ringDur = shared.getString("ring_duration", null);
        String ringtone = shared.getString("ringtone_select", null);
        String stolen = shared.getString("sms_stolen_gcm", null);
        String wipe_gcm = shared.getString("wipe_gcm", null);
        String wifi_gcm = shared.getString("enable_wifi_gcm", null);
        String sms_gcm = shared.getString("send_sms_gcm", null);
        String send_email_gcm = shared.getString("send_email_gcm", null);
        int ringVol = shared.getInt("seek_bar_volume", 90);

        if(lock != null && message.equals(lock)) {
            new PolicyManager(context).lockPhone();
        }
        if(wipe_gcm != null && message.equals(wipe_gcm)) {
            new PolicyManager(context).wipePhone();
        }
        if(ring != null && message.equals(ring)) {
            new Alarm(context,ringVol,ringDur,ringtone).ring();
        }
        if(stolen != null && message.equals(stolen)){
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("stolen", true).apply();
        }
        if(wifi_gcm != null && message.equals(wifi_gcm)){
            WifiManager wMan = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
            if(wMan != null && wMan.isWifiEnabled()){
                wMan.setWifiEnabled(true);
            }
        }
        if(sms_gcm != null && message.equals(sms_gcm)){

        }
        if(send_email_gcm != null && message.equals(send_email_gcm)){

        }
    }
}
