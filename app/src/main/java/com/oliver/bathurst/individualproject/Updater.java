package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * Created by Oliver on 30/12/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Updater extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if(intent.getAction() != null) {
            if (PreferenceManager.getDefaultSharedPreferences(c).getBoolean("status_update", false)) {
                new UpdateDatabase(new LocationService(c).getLoc(), c).update();
            }
        }
    }
}
