package com.oliver.bathurst.individualproject;

/*
 * Created by Oliver on 05/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;

public class ReceiverService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        handle(from, data.getString("message"));
    }
    @SuppressWarnings("unused")
    private void handle(String sender, String message) {
        Log.d("messagegcm", message);
    }
}
