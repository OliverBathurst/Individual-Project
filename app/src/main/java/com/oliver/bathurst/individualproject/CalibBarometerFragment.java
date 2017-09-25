package com.oliver.bathurst.individualproject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Oliver on 25/09/2017.
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class CalibBarometerFragment extends android.support.v4.app.Fragment implements SensorEventListener {
    private SensorManager baro;
    private TextView pressure;
    private Sensor barometer;

    public CalibBarometerFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.barometer, container, false);
        pressure = (TextView) mView.findViewById(R.id.pressureReading);

        baro = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        if (baro.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            barometer = baro.getDefaultSensor(Sensor.TYPE_PRESSURE);
            baro.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        return mView;
    }
    public void onPause() {
        super.onPause();
        baro.unregisterListener(this);
    }
    public void onResume() {
        super.onResume();
        if (barometer != null){
            try {
                baro.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (SecurityException ignored){}
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_PRESSURE){
            pressure.setText(String.format("%s", Float.toString(event.values[0])));
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
