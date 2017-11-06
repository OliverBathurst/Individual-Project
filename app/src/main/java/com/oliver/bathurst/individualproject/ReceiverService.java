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
        handle(data.toString().trim());
    }
    private void handle(String message) {
        new GCMHandler(message).examine();
    }
}
