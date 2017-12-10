package com.oliver.bathurst.individualproject;

import android.content.Intent;
import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Oliver on 05/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class TokenRefreshListener extends InstanceIDListenerService {
    /**
     * When token refresh, start the service to get new token
     */
    @Override
    public void onTokenRefresh() {
        startService(new Intent(this, RegistrationIntentService.class)); //start registration service
    }
}