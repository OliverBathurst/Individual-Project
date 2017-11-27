package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Oliver on 19/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class SMSHelper {
    private final Context c;

    SMSHelper(Context context){
        this.c = context;
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    String getBody(){
        TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        LocationService locationService = new LocationService(c);
        Location newLocReturn = locationService.getLoc();
        boolean pref = PreferenceManager.getDefaultSharedPreferences(c).getBoolean("cell_tower_sms", false);

        return c.getString(R.string.sim_change_alert_subject_title) + newLocReturn.getLatitude()
                + "," + newLocReturn.getLongitude()
                + "\n" + c.getString(R.string.gmaps_syntax) + newLocReturn.getLatitude()
                + "," + newLocReturn.getLongitude()
                + "\n" + c.getString(R.string.time_declared) + DateFormat.getDateTimeInstance().format(new Date())
                + "\n" + c.getString(R.string.declared_by) + newLocReturn.getProvider()
                + "\n" + c.getString(R.string.accuracy) + newLocReturn.getAccuracy()
                + "\n" + c.getString(R.string.batt_level) + locationService.batteryLife()
                + "\n" + c.getString(R.string.imei) + (telephonyManager != null ? telephonyManager.getDeviceId() : c.getString(R.string.null_value_string))
                + "\n" + c.getString(R.string.phone_number) + (telephonyManager != null ? telephonyManager.getLine1Number() : c.getString(R.string.null_value_string))
                + "\n" + c.getString(R.string.sim_serial) + (telephonyManager != null ? telephonyManager.getSimSerialNumber() : c.getString(R.string.null_value_string))
                + "\n" + (pref ? (c.getString(R.string.cell_tower_info) + new CellTowerHelper(c).getAll()) : "");
    }
}