package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;

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
            String user = PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_username", null);
            String pass = PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_password", null);
            if(user != null && pass != null) {
                new EmailFetcher(new WeakReference<>(c), user.trim(), pass.trim()).execute();
            }
        }

        if(settings.getBoolean("battery_flare", false) && settings.getBoolean("stolen", false)) {
            float batteryPercentage = ((float) arg1.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) /
                    (float) arg1.getIntExtra(BatteryManager.EXTRA_SCALE, 0)) * 100;
            if (batteryPercentage <= settings.getInt("battery_percent",5)) {
                if (!hasSent){
                    String receive = email.getReceiver();
                    if(receive != null) {
                        email.execute(new String[]{receive,c.getString(R.string.low_batt_alert), email.getEmailString()});
                        hasSent = true;
                    }
                }
            }else{
                hasSent = false; //reset to false if over the required percentage, allows multiple emails to be sent
            }
        }
    }
}