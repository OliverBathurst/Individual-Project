package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

/**
 * Created by Oliver on 27/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class Torch {
    private static boolean isOn = false;
    private final Context c;

    Torch(Context context){
        this.c = context;
    }

    void toggle(){
        if(c.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            try {
                Camera camera = Camera.open();
                if (isOn) {
                    Camera.Parameters params = camera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(params);
                    camera.stopPreview();
                    isOn = false;
                } else {
                    Camera.Parameters params = camera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(params);
                    camera.startPreview();
                    isOn = true;
                }
            }catch(Exception ignored){}
        }
    }
}