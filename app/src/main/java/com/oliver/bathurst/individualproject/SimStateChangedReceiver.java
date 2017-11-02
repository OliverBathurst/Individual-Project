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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getExtras().getString(EXTRA_SIM_STATE);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        if(settings.getBoolean("check_sim_preference", false)){

            String action = settings.getString("sim_change_action",null);
            if(action != null){
                switch(action){
                    case "SMS":
                        sendSMS(context,state,settings.getString("secondary_phone",null));
                        break;
                    case "EMAIL":
                        EmailAttachmentHelper help = new EmailAttachmentHelper(context);
                        if(help.isEmailValid()) {
                            sendEmail(context, state, help.getReceiver(), help.getUserName(), help.getPassword(), help);
                        }
                        break;
                    case "BOTH":
                        EmailAttachmentHelper helper = new EmailAttachmentHelper(context);
                        sendSMS(context,state,settings.getString("secondary_phone",null));
                        if(helper.isEmailValid()) {
                            sendEmail(context, state, helper.getReceiver(), helper.getUserName(), helper.getPassword(), helper);
                        }
                        break;
                }
            }
        }
    }
    @SuppressWarnings("deprecation")
    @SuppressLint("HardwareIds")
    private void sendEmail(final Context c, final String state, final String address, final String username, final String password,
                           final EmailAttachmentHelper help){
        if(address != null){
            try {
                @SuppressLint("StaticFieldLeak")
                class sendEmail extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        GMailSender sender = new GMailSender(username, password);
                        help.attachFiles(sender);
                        sender.sendMail(username, "SIM Change Alert", (help.getEmailString()+ "\nSIM State: " + state), address.trim());
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
                SmsManager.getDefault().sendTextMessage(number.trim(), null, (new SMSHelper(c).getBody()+ "\nSIM State: " + state), null, null);
            }catch (Exception ignored){}
        }
    }
}