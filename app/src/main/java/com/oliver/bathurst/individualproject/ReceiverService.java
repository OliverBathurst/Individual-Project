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
        System.out.println("Received");
        if(data.toString().contains("isTrigger")){ //if it contains the keyword

            new GCMHandler(data.toString(), this).examine();
        }
    }
}
