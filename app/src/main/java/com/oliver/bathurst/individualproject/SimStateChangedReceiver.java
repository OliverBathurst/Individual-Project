package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class SimStateChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getExtras() != null ? intent.getExtras().getString("ss") : context.getString(R.string.no_sim_state);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        if(settings.getBoolean("check_sim_preference", false)){

            String action = settings.getString("sim_change_action",null);
            if(action != null){
                switch(action){
                    case "SMS":
                        sendSMS(context,state,settings.getString("secondary_phone",null));
                        break;
                    case "EMAIL":
                        sendEmail(context, new PostPHP(context), state);
                        break;
                    case "BOTH":
                        sendSMS(context,state,settings.getString("secondary_phone",null));
                        sendEmail(context, new PostPHP(context), state);
                        break;
                }
            }
        }
    }
    private void sendEmail(Context c, PostPHP php, String msg){
        String receiver = php.getReceiver();
        if(receiver != null) {
            php.execute(new String[]{receiver, c.getString(R.string.sim_change_alert), (php.getEmailString() + "\n" + c.getString(R.string.sim_state) + msg)});
        }
    }
    private void sendSMS(Context c, String state, String number){
        if(number != null && number.trim().length() != 0){
            try {
                SmsManager.getDefault().sendTextMessage(number.trim(), null, (new SMSHelper(c).getBody()+ "\n" + c.getString(R.string.sim_state) + state), null, null);
            }catch (Exception ignored){}
        }
    }
}