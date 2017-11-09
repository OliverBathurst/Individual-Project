package com.oliver.bathurst.individualproject;

/*
 * Created by Oliver on 05/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

public class ReceiverService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String isValid = data.getString("validator");

        if(isValid != null && isValid.equals(getString(R.string.API_GCM))){
            String isNull = data.getString("message");
            if(isNull != null && isNull.trim().length() != 0){
                new GCMHandler(isNull, this).examine();
            }
        }
    }
}
