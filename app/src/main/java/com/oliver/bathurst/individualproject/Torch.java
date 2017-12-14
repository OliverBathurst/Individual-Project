package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

/**
 * Created by Oliver on 27/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

/**
 * This class is for toggling the torch feature
 */

class Torch {
    private static boolean isOn = false, hasFeature = false; //isOn static variable that exists across objects of this class, keeps track of the torch's status
    /**
     * Torch constructor simply takes a context and checks camera capabilities
     */
    Torch(Context context){
        hasFeature = context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH); //set boolean hasFeature to whether the system supports the torch feature
    }
    /**
     * This method toggles the flash
     */
    void toggle(){
        if(hasFeature){//if the device supports the torch (flash)
            Camera camera = Camera.open();//open the camera object
            if (isOn) {//if on, turn it off
                Camera.Parameters params = camera.getParameters();//get params from camera object
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//set the flash to off
                camera.setParameters(params);//set camera params with params object
                camera.stopPreview();//finally stop camera preview
                isOn = false;//switch flag
            } else {
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//set the flash mode
                camera.setParameters(params);
                camera.startPreview();//start the preview (open camera)
                isOn = true;
            }
        }
    }
}