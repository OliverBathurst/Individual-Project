package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * Created by Oliver on 03/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class GCMBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            String intentStr =  intent.getAction();
            if(intentStr != null && intent.getAction().equals("oliver.intent.action.GCM")){
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switchCommand(context, bundle.getStringArray("STRING"));
                }
            }
        }
    }
    private void switchCommand(Context c, String[] arr){
        try {
            if (arr != null) {
                switch (arr[0]) {
                    case "location":
                        new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{arr[1], new PostPHP(c).getEmailString()});
                        break;
                    case "beacons":
                        new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{arr[1], new NearbyBeacons(c).run()});
                        break;
                    case "email_send_loc":
                        PostPHP php = new PostPHP(c);
                        php.execute(new String[]{arr[1], arr[2], php.getEmailString()});
                        break;
                }
            }
        }catch(Exception ignored){}
    }
}
