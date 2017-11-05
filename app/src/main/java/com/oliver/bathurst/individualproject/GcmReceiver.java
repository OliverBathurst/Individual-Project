package com.oliver.bathurst.individualproject;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by Oliver on 05/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */


public class GcmReceiver extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        sendNotification(data.getString("message"));
    }
    private void sendNotification(String message) {
        int requestCode = 0;//Your request code
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null){
            notificationManager.notify(0, new NotificationCompat.Builder(this)
                            .setContentText("My GCM message :X:X")
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setContentIntent(PendingIntent.getActivity(this, requestCode, new Intent(this, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_ONE_SHOT)).build());
        }
    }
}
