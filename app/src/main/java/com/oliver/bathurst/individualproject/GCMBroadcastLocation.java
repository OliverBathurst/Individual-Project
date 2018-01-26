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

public class GCMBroadcastLocation extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            String intentStr =  intent.getAction();
            if(intentStr != null && intentStr.equals("oliver.intent.action.GCM")){
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    sendLocationBack(context, bundle.getStringArray("STRING"));
                }
            }
        }
    }
    private void sendLocationBack(Context c, String[] arr){
        if (arr != null && arr[1] != null) {
            new GCMRelay().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , new String[]{arr[1], new PostPHP(c).getEmailString()});
        }
    }
}
