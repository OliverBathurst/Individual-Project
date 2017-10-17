package com.oliver.bathurst.individualproject;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class DeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "Disabled Admin", Toast.LENGTH_SHORT).show();
        super.onDisabled(context, intent);
    }
    public CharSequence onDisableRequested(Context context, Intent intent) {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("prevent_uninstall", false)){ //if 'prevent uninstall' is true
            ((DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE)).lockNow();
            return "WARNING: some features will be disabled, continue?";
        }else{
            return "Are you sure?";
        }
    }
    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, "Enabled Admin!", Toast.LENGTH_SHORT).show();
    }
}