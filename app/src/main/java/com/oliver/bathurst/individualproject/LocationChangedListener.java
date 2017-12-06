package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import com.google.android.gms.location.LocationResult;

/**
 * Created by Oliver on 06/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class LocationChangedListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if(LocationResult.hasResult(intent)){
                updateDB(context, LocationResult.extractResult(intent).getLastLocation());
            }
        }
    }
    private void updateDB(Context c, Location loc){
        //check if the user is signed up for the service
        //then check if only update over wifi is checked
        //finally send results to DB
        //on another note, on initial user sign up, send the device info,
        // such that you only send location data on a location change, not the whole
        //device information as well (maybe)
    }
}
