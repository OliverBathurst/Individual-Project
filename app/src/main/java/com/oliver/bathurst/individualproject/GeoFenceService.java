package com.oliver.bathurst.individualproject;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
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
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            if (settings.getBoolean("geo_fence_enable_or_not", false) && settings.getBoolean("stolen", false)) {
                PostPHP php = new PostPHP(this);
                String receiver = php.getReceiver();
                if(receiver != null) {
                    php.execute(new String[]{receiver, getString(R.string.geofence_breach_title), php.getEmailString()});
                }
            }
        }
    }
}