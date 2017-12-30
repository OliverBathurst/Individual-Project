package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Oliver on 30/12/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Updater extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if(intent.getAction() != null) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
            if (settings.getBoolean("status_update", false)) {
                new UpdateDatabase(new LocationService(c).getLoc(), c).update();
            }
        }
    }
}
