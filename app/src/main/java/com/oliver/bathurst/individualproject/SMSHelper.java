package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.telephony.TelephonyManager;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Oliver on 19/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class SMSHelper {
    private final Context c;

    SMSHelper(Context context){
        c = context;
    }

    @SuppressLint("HardwareIds")
    String getBody(){
        TelephonyManager telephonyManager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        LocationService locationService = new LocationService(c);
        Location newLocReturn = locationService.getLoc();

        return "This is a location alert, SIM Change Alert, your device location is: " + newLocReturn.getLatitude()
                + "," + newLocReturn.getLongitude()
                + "\nGoogleMaps link: http://maps.google.com/?q=" + newLocReturn.getLatitude()
                + "," + newLocReturn.getLongitude()
                + "\nTime Declared: " + DateFormat.getDateTimeInstance().format(new Date())
                + "\nDeclared by: " + newLocReturn.getProvider() + " Accuracy: " + newLocReturn.getAccuracy()
                + "\nBattery level: " + locationService.batteryLife()
                + "\nIMEI: " +  telephonyManager.getDeviceId()
                + "\nPhone number: " + telephonyManager.getLine1Number()
                + "\nSIM Serial: " + telephonyManager.getSimSerialNumber();
    }
}