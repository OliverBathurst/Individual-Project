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

/**
 * BatterReceiver performs actions that need to be completed on a battery status change
 */
public class BatteryReceiver extends BroadcastReceiver {
    private static boolean hasSent = false;//has email sent?

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context c, Intent arg1) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
        //if user wants to update DB on battery change
        if(settings.getBoolean("battery_update", false)){
            new UpdateDatabase(new LocationService(c).getLoc(), c).update();
        }
        //fetch emails and analyse them for triggers
        if(settings.getBoolean("sms_by_email", false)){//if feature enabled
            String user = PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_username", null);//get saved user+pass
            String pass = PreferenceManager.getDefaultSharedPreferences(c).getString("gmail_password", null);
            if(user != null && pass != null) {//if valid create email fetcher instance
                new EmailFetcher(new WeakReference<>(c), user.trim(), pass.trim()).execute();
            }
        }
        //if the phone is stolen and battery flare is enabled
        if(settings.getBoolean("battery_flare", false) && settings.getBoolean("stolen", false)) {
            float batteryPercentage = ((float) arg1.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) /
                    (float) arg1.getIntExtra(BatteryManager.EXTRA_SCALE, 0)) * 100;//get battery percentage
            if (batteryPercentage <= settings.getInt("battery_percent",5)) {//if battery level is below threshold
                if (!hasSent){//if email has not been sent
                    PostPHP email = new PostPHP(c);//create new email sender object
                    String receive = email.getReceiver();
                    if(receive != null) {//if user has set recipient
                        email.execute(new String[]{receive,c.getString(R.string.low_batt_alert), email.getEmailString()});//send low battery email
                        hasSent = true;//set flag to true
                    }
                }
            }else{
                hasSent = false; //reset to false if over the required percentage, allows multiple emails to be sent
            }
        }
    }
}