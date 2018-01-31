package com.oliver.bathurst.individualproject;

/*
  Created by Oliver on 05/11/2017.
  Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import java.io.IOException;

public class RegistrationIntentService extends IntentService {
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    private static final String REGISTRATION_ERROR = "RegistrationError";

    public RegistrationIntentService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent registration;
        try {
            String token = InstanceID.getInstance(getApplicationContext()).getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);//get a token
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("GCM_Token", token).apply();//save to prefs
            registration = new Intent(REGISTRATION_SUCCESS);//specify success intent
            registration.putExtra("token", token);//put token in intent
        } catch (IOException e) {
            registration = new Intent(REGISTRATION_ERROR);//send error intent
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(registration);//send broadcast
    }
}