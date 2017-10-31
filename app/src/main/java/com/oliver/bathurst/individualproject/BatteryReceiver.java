package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */


public class BatteryReceiver extends BroadcastReceiver {
    private boolean hasSent = false;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context c, Intent arg1) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);

        if(settings.getBoolean("sms_by_email", false)){
            String user = settings.getString("gmail_username", null);
            String pass = settings.getString("gmail_password", null);
            if((user != null && user.trim().length() != 0 && user.contains("@")) && (pass != null && pass.trim().length() != 0)){
                new EmailReceiver(c, user, pass).getNewEmails();
            }
        }
        try {
            if(settings.getBoolean("battery_flare", false) && settings.getBoolean("stolen", false)) {
                String emailToSendTo = settings.getString("email_string", null);

                float batteryPercentage = ((float) arg1.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) /
                        (float) arg1.getIntExtra(BatteryManager.EXTRA_SCALE, 0)) * 100;

                if (batteryPercentage <= settings.getInt("seek_bar_battery",5)) {
                    if (!hasSent && emailToSendTo!=null && emailToSendTo.trim().length()!=0
                            && emailToSendTo.contains("@")){
                        sendEmailLowBatteryAlert(c,emailToSendTo.trim());
                    }
                }
            }
        }catch(Exception ignored){}
    }
    private void sendEmailLowBatteryAlert(final Context c, final String email){
        @SuppressLint("StaticFieldLeak")
        class sendAlert extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                EmailAttachmentHelper help = new EmailAttachmentHelper(c);

                GMailSender sender = new GMailSender("locator.findmydevice.service@gmail.com", "TheWatchful2");
                help.attachFiles(sender);

                sender.sendMail("locator.findmydevice.service@gmail.com",
                        "Low Battery Alert", help.getEmailString(), email);
                hasSent = true;
                return null;
            }
        }
        new sendAlert().execute();
    }
}