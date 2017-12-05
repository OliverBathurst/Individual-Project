package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings({"UnusedAssignment", "DefaultFileTemplate", "deprecation"})
public class SMSReceiver extends BroadcastReceiver {
    static String toSpeak = "";
    private boolean doHide;

    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        doHide = settings.getBoolean("hide_sms", false);
        if(settings.getBoolean("enable_triggers", true)) {
            if (intent.getExtras() != null) {
                Object[] smsExtra = (Object[]) intent.getExtras().get("pdus");

                for (int i = 0; i < (smsExtra != null ? smsExtra.length : 0); ++i) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
                    switchMessage(context, sms.getMessageBody().trim(), sms.getOriginatingAddress().trim());
                }
            }
        }
    }
    /**
     * uses if statements so can bind a single trigger to multiple actions
     */
    private void switchMessage(Context context, String body, String sender){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String ring = settings.getString("sms_ring", null);
        String email = settings.getString("sms_relay_email", null);
        String text = settings.getString("sms_relay_text", null);
        String locService = settings.getString("sms_loc_services", null);
        String remoteLock = settings.getString("sms_remote_lock", null);
        String wipe = settings.getString("sms_wipe", null);
        int ringVol = settings.getInt("seek_bar_volume", 90);
        String ringDur = settings.getString("ring_duration", null);
        String unhideStr = settings.getString("sms_hide_app", null);
        String ringtone = settings.getString("ringtone_select", null);
        String updateInterval = settings.getString("update_interval",null);
        String updateIntervalNum = settings.getString("update_interval_number",null);
        String wipeSD = settings.getString("wipe_sdcard",null);
        String stolen = settings.getString("sms_stolen", null);
        String smsBeacon = settings.getString("sms_relay_beacon", null);
        String smsTorch = settings.getString("turn_torch_on_sms", null);
        String smsGCMToken = settings.getString("get_gcm_sms", null);


        if(smsTorch != null && body.equals(smsTorch)){
            new Torch(context).toggle();
        }
        if(stolen != null && body.equals(stolen)){
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("stolen", true).apply();
        }
        if(smsBeacon != null && body.equals(smsBeacon)){
            SmsManager.getDefault().sendTextMessage(sender, null, new NearbyBeacons(context).run() , null, null);
        }
        if(smsGCMToken != null && body.equals(smsGCMToken)){
            SmsManager.getDefault().sendTextMessage(sender, null, PreferenceManager.getDefaultSharedPreferences(context).getString("GCM_Token", null), null, null);
        }

        if(body.contains("speak:")){
            toSpeak = (body.trim().split(":")[1]);
            context.startActivity(new Intent(context,TxtToSpeech.class));
        }

        if(ring != null && body.equals(ring)) {
            doNotification(context);
            new Alarm(context,ringVol,ringDur,ringtone).ring();
        }
        if(email != null && body.equals(email)) {
            PostPHP p = new PostPHP(context);
            if(p.getReceiver() != null) {
                doNotification(context);
                sendLoc(context, p.getReceiver(),updateInterval,updateIntervalNum,2);
            }
        }
        if(text != null && body.equals(text)) {
            doNotification(context);
            sendLoc(context, sender,updateInterval,updateIntervalNum,1);
        }
        if(locService != null && body.equals(locService)) {
            doNotification(context);
            remoteTurnOnWiFi(context);
        }
        if(remoteLock != null && body.equals(remoteLock)) {
            doNotification(context);
            remoteLockMethod(context);
        }
        if(wipe != null && body.equals(wipe)) {
            doNotification(context);
            remoteWipe(context);
        }
        if (unhideStr != null && body.equals(unhideStr)){
            doNotification(context);
            new HideApp(context).toggle();
        }
        if(wipeSD != null && body.equals(wipeSD)){
            doNotification(context);
            new SDWiper().wipeSD();
        }
    }
    private void doNotification(Context c){
        if(!doHide){
            Toast.makeText(c, c.getString(R.string.matched_trigger), Toast.LENGTH_SHORT).show();
        }
    }
    private void sendLoc(final Context c, final String sender, String updateInterval, String updateIntervalNum, final int requestNo){
        int interval = 1,number = 1;

        if(updateInterval != null && updateIntervalNum != null){
            try{
                interval = Integer.parseInt(updateInterval);
                number = Integer.parseInt(updateIntervalNum);
            }catch(Exception e){
                interval = 1;
                number = 1;
            }
        }
        final int i = number;
        final Timer t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            int counter = 0;
            @Override
            public void run() {
                Looper.prepare();
                if(counter<i) {
                    if(requestNo == 1) {
                        trySendingTextMessage(c, sender, counter, i);
                    }else{
                        trySendingEmail(c, sender, counter,i);
                    }
                    counter++;
                }else{
                    t.cancel();
                }
                Looper.loop();
            }
        },0, interval);
    }
    private void remoteTurnOnWiFi(Context c){
        WifiManager wMan = ((WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        if(wMan != null && wMan.isWifiEnabled()){
            wMan.setWifiEnabled(true);
        }
    }
    ///////////////WARNING//////////////////
    private void remoteWipe(Context c){
        new PolicyManager(c).wipePhone();
    }

    private void trySendingEmail(final Context c, final String address, final int counter, final int num){
        @SuppressLint("StaticFieldLeak")
        class sendAlert extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                Looper.prepare();
                PostPHP p = new PostPHP(c);
                p.execute(new String[]{address, c.getString(R.string.location_update_title), (p.getEmailString() + " (" + (counter + 1) + "/" + num + ")") });
                Looper.loop();
                return null;
            }
        }
        new sendAlert().execute();
    }
    private void trySendingTextMessage(Context c, String sender, int counter, int num){
        try {
            SmsManager.getDefault().sendTextMessage(sender, null, new SMSHelper(c).getBody()
                    + " (" + (counter+1) + "/" + num + ")", null, null);
        }catch (Exception ignored){}
    }
    private void remoteLockMethod(Context c){
        new PolicyManager(c).lockPhone();
    }
}