package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class SimStateChangedReceiver extends BroadcastReceiver {

    private static final String EXTRA_SIM_STATE = "ss";

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getExtras() != null ? intent.getExtras().getString(EXTRA_SIM_STATE) : "Cannot get SIM state";

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        if(settings.getBoolean("check_sim_preference", false)){

            String action = settings.getString("sim_change_action",null);
            if(action != null){
                switch(action){
                    case "SMS":
                        sendSMS(context,state,settings.getString("secondary_phone",null));
                        break;
                    case "EMAIL":
                        checkEmail(new GMailSender(context), state);
                        break;
                    case "BOTH":
                        sendSMS(context,state,settings.getString("secondary_phone",null));
                        checkEmail(new GMailSender(context), state);
                        break;
                }
            }
        }
    }
    private void checkEmail(GMailSender gmail, String state){
        if(gmail.isEmailValid()) {
            gmail.setUserAndPass(gmail.getUserName().trim(), gmail.getPassword().trim());
            sendEmail(state, gmail.getReceiver(), gmail);
        }
    }
    private void sendEmail(final String state, final String address, final GMailSender g){
        if(address != null){
            try {
                @SuppressLint("StaticFieldLeak")
                class sendEmail extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        g.sendMail(g.getUserName().trim(), "SIM Change Alert", (g.getEmailString()+ "\nSIM State: " + state), address);
                        return null;
                    }
                }
                new sendEmail().execute();
            }catch(Exception ignored){}
        }
    }
    private void sendSMS(Context c, String state, String number){
        if(number != null && number.trim().length() != 0){
            try {
                SmsManager.getDefault().sendTextMessage(number.trim(), null, (new SMSHelper(c).getBody()+ "\nSIM State: " + state), null, null);
            }catch (Exception ignored){}
        }
    }
}