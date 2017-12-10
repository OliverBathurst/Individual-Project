package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
                new UpdateDatabase(LocationResult.extractResult(intent).getLastLocation(), context).update();
            }
        }
    }
}
