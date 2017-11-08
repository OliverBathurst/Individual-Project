package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Oliver on 06/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class GCMHandler {
    private final String toExamine;
    private final Context context;

    GCMHandler(String str,Context c){
        this.toExamine = str;
        this.context = c;
    }

    void examine(){
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        String lock = shared.getString("lock_gcm", null);
        String ring = shared.getString("gcm_ring", null);
        String ringDur = shared.getString("ring_duration", null);
        String ringtone = shared.getString("ringtone_select", null);
        int ringVol = shared.getInt("seek_bar_volume", 90);

        if(lock != null && toExamine.equals(lock)) {
            new PolicyManager(context).lockPhone();
        }
        if(ring != null && toExamine.equals(ring)) {
            new Alarm(context,ringVol,ringDur,ringtone).ring();
        }
    }
}
