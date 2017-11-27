package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

/**
 * Created by Oliver on 27/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class Torch {

    private boolean isOn = false, hasFeature = false;

    Torch(Context c){
        this.hasFeature = c.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    void toggle(){
        if(hasFeature){
            Camera camera = Camera.open();
            if(isOn) {
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                camera.stopPreview();
                isOn = false;
            }else{
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
                isOn = true;
            }
        }
    }
}
