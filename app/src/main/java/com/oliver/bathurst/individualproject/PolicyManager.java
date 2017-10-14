package com.oliver.bathurst.individualproject;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class PolicyManager {
    static final int DPM_ACTIVATION_REQUEST_CODE = 100;
    private final DevicePolicyManager mDPM;
    private final ComponentName adminComponent;

    PolicyManager(Context context) {
        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(context, DeviceAdmin.class);
    }

    boolean isAdminActive() {
        return mDPM.isAdminActive(adminComponent);
    }

    ComponentName getAdminComponent() {
        return adminComponent;
    }

    void lockPhone(){
        if(isAdminActive()) {
            mDPM.lockNow();
        }
    }
    ///////WARNING////////////
    void wipePhone(){
        if(isAdminActive()) {
            mDPM.wipeData(0);
        }
    }
}