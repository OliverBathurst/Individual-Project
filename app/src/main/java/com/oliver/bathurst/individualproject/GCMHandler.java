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


        ///TEST
        if(lock != null && toExamine.equals(lock)) {
            new PolicyManager(context).lockPhone();
        }
    }
}
