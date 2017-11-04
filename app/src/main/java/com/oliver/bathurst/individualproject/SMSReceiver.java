package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings({"UnusedAssignment", "DefaultFileTemplate", "deprecation"})
public class SMSReceiver extends BroadcastReceiver {
    private static final String SMS_EXTRA_NAME = "pdus";
    static String toSpeak = "";
    private boolean doHide;

    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        doHide = settings.getBoolean("hide_sms", false);
        if(settings.getBoolean("enable_triggers", true)) {
            if (!doHide) {
                Toast.makeText(context, "Received", Toast.LENGTH_SHORT).show();
            }
            try {
                if (intent.getExtras() != null) {
                    Object[] smsExtra = (Object[]) intent.getExtras().get(SMS_EXTRA_NAME);

                    for (int i = 0; i < (smsExtra != null ? smsExtra.length : 0); ++i) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
                        switchMessage(context, sms.getMessageBody().trim(), sms.getOriginatingAddress().trim());
                    }
                }
            } catch (Exception e) {
                if (!doHide) {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        if(stolen != null && body.equals(stolen)){
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("stolen", true).apply();
        }
        if(smsBeacon != null && body.equals(smsBeacon)){
            SmsManager.getDefault().sendTextMessage(sender, null, new NearbyBeacons(context).run() , null, null);
        }

        if(body.contains("speak:")){
            toSpeak = (body.trim().split(":")[1]);
            context.startActivity(new Intent(context,TxtToSpeech.class));
        }

        if(ring != null && body.equals(ring)) {
            int duration = 20;
            if(ringDur != null) {
                try {
                    duration = Integer.parseInt(ringDur);
                } catch (NumberFormatException e) {
                    duration = 20;
                }
            }else{
                duration = 20;
            }
            doNotification(context);
            ringPhone(context,ringVol,duration,ringtone);
        }
        if(email != null && body.equals(email)) {
            GMailSender g = new GMailSender(context);
            if(g.getReceiver() != null) {
                doNotification(context);
                sendLoc(context, g.getReceiver(),updateInterval,updateIntervalNum,2);
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
            unHideApp(context);
        }
        if(wipeSD != null && body.equals(wipeSD)){
            doNotification(context);
            wipeSD();
        }
    }
    private void doNotification(Context c){
        if(!doHide){
            Toast.makeText(c, "String matched, performing action", Toast.LENGTH_SHORT).show();
        }
    }
    private void ringPhone(Context c, int ringVol, int ringDur, String ringtone){
        Uri ringtoneUri;
        try{
            ringtoneUri = (ringtone != null && !ringtone.equals("error")) ? Uri.parse(ringtone) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }catch(Exception e){
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        AudioManager audMan = ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE));
        if(audMan != null){
            audMan.setStreamVolume(AudioManager.STREAM_MUSIC, ringVol, 0);
        }
        try {
            final MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(c, ringtoneUri);
            mp.setVolume(100, 100);
            mp.prepare();
            mp.start();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mp.stop();
                }
            }, ringDur * 1000);
        }catch(Exception e){
            if(!doHide) {
                Toast.makeText(c, "Error playing sound: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
                GMailSender g = new GMailSender(c);
                if(g.isEmailValid()) {
                    g.setUserAndPass(g.getUserName().trim(), g.getPassword().trim());
                    g.sendMail(g.getUserName().trim(), "Location Alert", g.getEmailString()
                            + " (" + (counter + 1) + "/" + num + ")", address);
                }
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
    private void unHideApp(Context c){
        c.getPackageManager().setComponentEnabledSetting(new ComponentName(c, Login.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void wipeSD(){
        File deleteMatchingFile = new File(Environment.getExternalStorageDirectory().toString());
        try {
            File[] filenames = deleteMatchingFile.listFiles();
            if (filenames != null && filenames.length > 0) {
                for (File tempFile : filenames) {
                    if (tempFile.isDirectory()) {
                        wipeDirectory(tempFile.toString());
                        tempFile.delete();
                    } else {
                        tempFile.delete();
                    }
                }
            } else {
                deleteMatchingFile.delete();
            }
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void wipeDirectory(String name) {
        File directoryFile = new File(name);
        File[] filenames = directoryFile.listFiles();
        if (filenames != null && filenames.length > 0) {
            for (File tempFile : filenames) {
                if (tempFile.isDirectory()) {
                    wipeDirectory(tempFile.toString());
                    tempFile.delete();
                } else {
                    tempFile.delete();
                }
            }
        } else {
            directoryFile.delete();
        }
    }
}