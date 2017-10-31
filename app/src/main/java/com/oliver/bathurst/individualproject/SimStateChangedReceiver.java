package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class SimStateChangedReceiver extends BroadcastReceiver {

    private static final String EXTRA_SIM_STATE = "ss";
    private boolean doHide;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getExtras().getString(EXTRA_SIM_STATE);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        doHide = settings.getBoolean("hide_sms", false);
        if(settings.getBoolean("check_sim_preference", false)){

            if (!doHide) {
                Toast.makeText(context, "SIM Monitor Activated", Toast.LENGTH_SHORT).show();
            }

            String action = settings.getString("sim_change_action",null);
            if(action != null){
                switch(action){
                    case "SMS":
                        sendSMS(context,state,settings.getString("secondary_phone",null));
                        break;
                    case "EMAIL":
                        sendEmail(context,state,settings.getString("email_string",null));
                        break;
                    case "BOTH":
                        sendSMS(context,state,settings.getString("secondary_phone",null));
                        sendEmail(context,state,settings.getString("email_string",null));
                        break;
                }
            }
        }
    }
    @SuppressWarnings("deprecation")
    @SuppressLint("HardwareIds")
    private void sendEmail(final Context c, final String state, final String address){
        if(address != null && address.trim().length() != 0 && address.contains("@")){
            try {
                if (!doHide) {
                    Toast.makeText(c, "Sending email", Toast.LENGTH_SHORT).show();
                }
                @SuppressLint("StaticFieldLeak")
                class sendEmail extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        EmailAttachmentHelper help = new EmailAttachmentHelper(c);
                        GMailSender sender = new GMailSender("locator.findmydevice.service@gmail.com", "TheWatchful2");
                        help.attachFiles(sender);
                        sender.sendMail("locator.findmydevice.service@gmail.com",
                                "SIM Change Alert", (help.getEmailString()+ "\nSIM State: " + state), address.trim());
                        return null;
                    }
                }
                new sendEmail().execute();
            }catch(Exception ignored){}
        }
    }
    @SuppressLint("HardwareIds")
    private void sendSMS(Context c, String state, String number){
        if(number != null && number.trim().length() != 0){
            try {
                if (!doHide) {
                    Toast.makeText(c, "Sending text message", Toast.LENGTH_SHORT).show();
                }
                SmsManager.getDefault().sendTextMessage(number.trim(), null, (new SMSHelper(c).getBody()+ "\nSIM State: " + state), null, null);
            }catch (Exception ignored){}
        }
    }
}