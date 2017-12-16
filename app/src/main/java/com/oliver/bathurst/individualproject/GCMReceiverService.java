package com.oliver.bathurst.individualproject;

/**
 * Created by Oliver on 05/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

import android.os.Bundle;
import com.google.android.gms.gcm.GcmListenerService;

public class GCMReceiverService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String isNull = data.getString("message");
        if(isNull != null && isNull.trim().length() != 0){
            new GCMHandler(isNull,data, getBaseContext()).examine();
        }
    }
}