package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Looper;
import android.preference.PreferenceManager;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class BatteryReceiver extends BroadcastReceiver {
    private static boolean hasSent = false;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context c, Intent arg1) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        PostPHP email = new PostPHP(c);

        if(settings.getBoolean("sms_by_email", false)){
            new EmailReceiver(c, getMonitoredUserName(c).trim(), getMonitoredPassword(c).trim()).getNewEmails();
        }

        if(settings.getBoolean("battery_flare", false) && settings.getBoolean("stolen", false)) {
            float batteryPercentage = ((float) arg1.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) /
                    (float) arg1.getIntExtra(BatteryManager.EXTRA_SCALE, 0)) * 100;
            if (batteryPercentage <= settings.getInt("battery_percent",5)) {
                if (!hasSent){
                    if(email.getReceiver() != null) {
                        sendEmailLowBatteryAlert(c, email.getReceiver(), email);
                    }
                }
            }else{
                hasSent = false; //reset to false if over the required percentage, allows multiple emails to be sent
            }
        }
    }
    private String getMonitoredUserName(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_username", null);
    }

    private String getMonitoredPassword(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_password", null);
    }
    private void sendEmailLowBatteryAlert(final Context c, final String email, final PostPHP p){
        @SuppressLint("StaticFieldLeak")
        class sendAlert extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                Looper.prepare();
                p.execute(new String[]{email,c.getString(R.string.low_batt_alert), p.getEmailString()});
                hasSent = true;
                Looper.loop();
                return null;
            }
        }
        new sendAlert().execute();
    }
}