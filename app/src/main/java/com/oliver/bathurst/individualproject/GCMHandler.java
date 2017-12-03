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
        String gcm_gcm = shared.getString("gcm_get_gcm", null);
        String gcm_relay_location = shared.getString("gcm_location_relay", null);

        String extras = toExamine.getString("extra");
        String relay = toExamine.getString("sender");

        if(gcm_relay_location != null && message.equals(gcm_relay_location)){
            if(relay != null && !relay.equals("null")){
                context.sendBroadcast(new Intent().setAction("oliver.intent.action.GCM").putExtra("STRING", new String[]{relay, "location"}));
            }
        }
        if(torch_gcm != null && message.equals(torch_gcm)){
            new Torch(context).toggle();
        }
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
            if(extras != null && !extras.equals("null")){
                SmsManager.getDefault().sendTextMessage(extras, null, new SMSHelper(context).getBody(), null, null);
            }else if(secondary_phone != null && secondary_phone.trim().length() > 0){
                SmsManager.getDefault().sendTextMessage(secondary_phone, null, new SMSHelper(context).getBody(), null, null);
            }
        }
        if(send_email_gcm != null && message.equals(send_email_gcm)){
            if(extras != null && !extras.equals("null")){
                MailSender g = new MailSender(context);
                trySendingEmail(context.getString(R.string.location_update_title), context, extras, g.getEmailString());//special send (send to extra)
            }else if(email_string != null && email_string.trim().length() > 0 &&  email_string.contains("@")) {
                MailSender g = new MailSender(context);
                trySendingEmail(context.getString(R.string.location_update_title), context, email_string, g.getEmailString());//normal send
            }
        }

        if(gcm_gcm != null && message.equals(gcm_gcm)){
            if(extras != null && !extras.equals("null")){
                trySendingEmail(context.getString(R.string.gcm_token_info), context, extras, PreferenceManager.getDefaultSharedPreferences(context).getString("GCM_Token", null));//special send (send to extra)
            }else if(email_string != null && email_string.trim().length() > 0 &&  email_string.contains("@")) {
                trySendingEmail(context.getString(R.string.gcm_token_info), context, email_string, PreferenceManager.getDefaultSharedPreferences(context).getString("GCM_Token", null));//normal send
            }
        }
    }
    private void trySendingEmail(final String title, final Context c, final String address, final String message){
        @SuppressLint("StaticFieldLeak")
        class sendAlert extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                Looper.prepare();
                MailSender g = new MailSender(c);
                g.sendMail(title, message, address);
                Looper.loop();
                return null;
            }
        }
        new sendAlert().execute();
    }
}
