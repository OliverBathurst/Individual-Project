package com.oliver.bathurst.individualproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by Oliver on 05/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class HideApp {
    private final Context c;

    HideApp(Context context){
        this.c = context;
    }

    void toggle(){
        if(getStatus()){
            c.getPackageManager().setComponentEnabledSetting(new ComponentName(c, Login.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            getDefaultSharedPreferences(c).edit().putBoolean("is_app_hidden", false).apply();
        }else{
            c.getPackageManager().setComponentEnabledSetting(new ComponentName(c, Login.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            getDefaultSharedPreferences(c).edit().putBoolean("is_app_hidden", true).apply();
        }
    }
    boolean getStatus(){
       return getDefaultSharedPreferences(c).getBoolean("is_app_hidden", false);
    }
}
