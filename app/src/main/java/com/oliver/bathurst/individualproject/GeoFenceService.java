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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String emailToSendTo = settings.getString("email_string", null);

        if(settings.getBoolean("geo_fence_enable_or_not",false) && emailToSendTo != null && emailToSendTo.trim().length() != 0
                && emailToSendTo.contains("@") && settings.getBoolean("stolen", false)) {
            new GMailSender("locator.findmydevice.service@gmail.com", "TheWatchful2").sendMail("locator.findmydevice.service@gmail.com",
                    "Geofence Breach", new EmailAttachmentHelper(this).getEmailString(), emailToSendTo.trim());
        }
    }
}