package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Oliver on 03/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class GCMBroadcast extends BroadcastReceiver {
    private static final String CUSTOM_INTENT = "oliver.intent.action.GCM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            String intentStr =  intent.getAction();
            if(intentStr != null && intent.getAction().equals(CUSTOM_INTENT)){
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switchCommand(context, bundle.getStringArray("STRING"));
                }
            }
        }
    }
    private void switchCommand(Context c, String[] arr){
        if(arr != null && arr.length == 2){
            String sender = arr[0];
            switch(arr[1]) {
                case "location" :
                    new GCMRelay().execute(new String[]{sender, new MailSender(c).getEmailString()});
                    break;
            }
        }
    }
}
