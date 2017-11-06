package com.oliver.bathurst.individualproject;

/*
  Created by Oliver on 05/11/2017.
  Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class RegistrationIntentService extends IntentService {
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess", REGISTRATION_ERROR = "RegistrationError";

    public RegistrationIntentService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent registration;
        try {
            String token = InstanceID.getInstance(getApplicationContext()).getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.w("GCMRegIntentService", "token:" + token);
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("GCM_Token", token).apply();
            registration = new Intent(REGISTRATION_SUCCESS);
            registration.putExtra("token", token);
        } catch (Exception e) {
            Log.w("GCMRegIntentService", "Registration error");
            registration = new Intent(REGISTRATION_ERROR);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(registration);
    }
}
