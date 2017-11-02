package com.oliver.bathurst.individualproject;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
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
        EmailAttachmentHelper helper =  new EmailAttachmentHelper(this);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if(helper.isEmailValid()) {
            if (settings.getBoolean("geo_fence_enable_or_not", false) && helper.getReceiver() != null && settings.getBoolean("stolen", false)) {
                new GMailSender(helper.getUserName(), helper.getPassword()).sendMail(helper.getUserName(), "Geofence Breach", helper.getEmailString(), helper.getReceiver().trim());
            }
        }
    }
}