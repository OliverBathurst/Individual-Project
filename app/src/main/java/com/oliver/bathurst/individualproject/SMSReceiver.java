package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

/**
 * This class listens for incoming texts (SMS) and checks against saved triggers
 * stored in shared preferences
 */

@SuppressWarnings({"UnusedAssignment", "DefaultFileTemplate", "deprecation"})
public class SMSReceiver extends BroadcastReceiver {
    private String message;

    /**
     * onReceive receives intents specified by the action within the intent filter (AndroidManifest.xml)
     */
    public void onReceive(Context context, Intent intent) {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("enable_triggers", true)) { //check if triggering is enabled
            if (intent.getExtras() != null) { //check that the intent has extras
                Object[] smsExtra = (Object[]) intent.getExtras().get("pdus"); //get pdus store

                for (int i = 0; i < (smsExtra != null ? smsExtra.length : 0); i++) { //iterate over objects
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);//create an SMS variable from the objects byte array
                    switchMessage(context, sms.getMessageBody().trim(), sms.getOriginatingAddress().trim());//pass the text, sender, and a context to an analyser method
                }
            }
        }
    }
    /**
     * This method accesses the shared preferences and retrieves all necessary triggers
     * uses if statements so the user can bind a single trigger to multiple actions
     */
    private void switchMessage(Context context, String body, String sender){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        this.message = body;

        String ring = settings.getString("sms_ring", null); //get various sms triggers (modifiable in the GUI)
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

        if(validate(smsTorch)){
            new Torch(context).toggle();
        }
        if(validate(stolen)){
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("stolen", true).apply();
        }
        if(validate(smsBeacon)){
            SmsManager.getDefault().sendTextMessage(sender, null, new NearbyBeacons(context).run() , null, null);
        }
        if(validate(smsGCMToken)){
            SmsManager.getDefault().sendTextMessage(sender, null, PreferenceManager.getDefaultSharedPreferences(context).getString("GCM_Token", null), null, null);
        }
        if(validate(ring)) {
            new Alarm(context,ringVol,ringDur,ringtone).ring();
        }
        if(validate(email)) {
            PostPHP p = new PostPHP(context);
            if(p.getReceiver() != null) {
                sendLoc(context, p.getReceiver(),updateInterval,updateIntervalNum,2);
            }
        }
        if(validate(text)) {
            sendLoc(context, sender,updateInterval,updateIntervalNum,1);
        }
        if(validate(locService)) {
            WifiManager wMan = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
            if(wMan != null && !wMan.isWifiEnabled()){
                wMan.setWifiEnabled(true);
            }
        }
        if(validate(remoteLock)) {
            new PolicyManager(context).lockPhone();
        }
        if(validate(wipe)) {
            new PolicyManager(context).wipePhone();
        }
        if (validate(unhideStr)){
            new HideApp(context).toggle();
        }
        if(validate(wipeSD)){
            new SDWiper().wipeSD();
        }
        if(message.contains("speak:")){
            context.startActivity(new Intent(context,TxtToSpeech.class).putExtra("SPEECH", (body.trim().split(":")[1])));
        }
    }
    private boolean validate(String str){
        return (str != null && message.equals(str));
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
                        SmsManager.getDefault().sendTextMessage(sender, null, new SMSHelper(c).getBody() +
                                (PreferenceManager.getDefaultSharedPreferences(c).getBoolean("cell_tower_sms", false) ? ("\n" + new CellTowerHelper(c).getAll()) : "")
                                + " (" + (counter+1) + "/" + i + ")", null, null);
                    }else{
                        PostPHP p = new PostPHP(c);
                        p.execute(new String[]{sender, c.getString(R.string.location_update_title), (p.getEmailString() + " (" + (counter + 1) + "/" + i + ")") });
                    }
                    counter++;
                }else{
                    t.cancel();
                }
                Looper.loop();
            }
        },0, interval);
    }
}