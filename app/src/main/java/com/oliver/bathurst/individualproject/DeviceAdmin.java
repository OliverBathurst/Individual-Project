package com.oliver.bathurst.individualproject;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class DeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.disabled_admin), Toast.LENGTH_SHORT).show();
        super.onDisabled(context, intent);
    }

    public CharSequence onDisableRequested(Context context, Intent intent) {
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("prevent_uninstall", false)){ //if 'prevent uninstall' is true
            DevicePolicyManager devPol = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if(devPol != null){
                devPol.lockNow();
            }
            return context.getString(R.string.some_features_disabled);
        }else{
            return context.getString(R.string.confirm_disable);
        }
    }
    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, context.getString(R.string.enabled_admin), Toast.LENGTH_SHORT).show();
    }
}