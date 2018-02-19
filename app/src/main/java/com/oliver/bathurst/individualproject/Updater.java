package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
/**
 * Created by Oliver on 30/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

/**
 * This class performs an update request upon receipt of a connectivity intent (if below Android 7.0)
 */
@SuppressWarnings("DefaultFileTemplate")
public class Updater extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if(intent.getAction() != null) {//check the intent is valid
            if (PreferenceManager.getDefaultSharedPreferences(c).getBoolean("status_update", false)) {
                new UpdateDatabase(new LocationService(c).getLocation(), c).update();//if user has enabled feature, update database
            }
        }
    }
}
