package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings("deprecation")
public class GeoFenceService extends IntentService {

    public GeoFenceService() {
        super("GeoFenceService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (GeofencingEvent.fromIntent(intent).hasError()) {
            return;
        }
        if(GeofencingEvent.fromIntent(intent).getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {
            sendEmail();
        }
    }
    private void sendEmail(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (settings.getBoolean("geo_fence_enable_or_not", false) && settings.getBoolean("stolen", false)) {
            trySendingEmail(new PostPHP(this));
        }
    }
    private void trySendingEmail(final PostPHP php){
        @SuppressLint("StaticFieldLeak")
        class sendAlert extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                php.execute(new String[]{php.getReceiver(), getString(R.string.geofence_breach_title), php.getEmailString()});
                return null;
            }
        }
        new sendAlert().execute();
    }
}