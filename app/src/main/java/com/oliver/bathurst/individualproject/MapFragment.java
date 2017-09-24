package com.oliver.bathurst.individualproject;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings("deprecation")
public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback, LocationListener, SensorEventListener {
    private float ORIENTATION = 0.0f, currentAccuracy = 0.0f;
    private View mView;
    private Circle circle, circle_margin;
    private TextView marginOfError;
    private GoogleMap gMap;
    private SensorManager sensorManager, baro;
    private LocationManager locationManager;
    private Sensor gyro, barometer;
    private Marker marker;

    public MapFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.fragment_map, container, false);
        marginOfError = (TextView) mView.findViewById(R.id.margin_of_error);

        baro = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        if (baro.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            barometer = baro.getDefaultSensor(Sensor.TYPE_PRESSURE);
            baro.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null) {
            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
        }
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        return mView;
    }
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        MapView mMapView = (MapView) mView.findViewById(R.id.map);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }
    public void onResume() {
        super.onResume();
        if(gyro!=null) {
            try {
                sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (Exception ignored) {}
        }

        if (barometer != null){
            try {
                baro.registerListener(this, barometer, SensorManager.SENSOR_DELAY_NORMAL);
            }catch(SecurityException ignored){}
        }

        try {
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 1, this);
            }else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
            }else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 2000, 1, this);
            }else{
                Toast.makeText(getActivity(), "No providers available", Toast.LENGTH_LONG).show();
            }
        }catch(SecurityException ignored){}

    }
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
        baro.unregisterListener(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            gMap = googleMap;
            LocationService locService = new LocationService(getActivity());
            Location loc = locService.getLoc();
            currentAccuracy = loc.getAccuracy();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String map = settings.getString("mapType", null);
            if (map != null) {
                switch (map.toUpperCase()) {
                    case "NORMAL":
                        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case "HYBRID":
                        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case "SATELLITE":
                        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case "TERRAIN":
                        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    default:
                        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                }
            } else {
                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

            TextView txtView = (TextView) mView.findViewById(R.id.declare);
            txtView.setText(getString(R.string.declaration).concat(" " + locService.DECLARED_BY));
            TextView acc = (TextView) mView.findViewById(R.id.locationAcc);
            acc.setText(String.format("%s%s", getString(R.string.accuracy), Float.toString(loc.getAccuracy())));

            MapsInitializer.initialize(getContext());

            marker = gMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title("Device Location: " + loc.getLatitude() + loc.getLongitude()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).flat(true).anchor(0.5f,0.5f));
            CameraPosition cam = CameraPosition.builder().target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(16).bearing(0).tilt(45).build();
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));

            showExtras(loc);
        }catch(Exception e){
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void showExtras(Location loc){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isGeoEnabled = settings.getBoolean("geo_fence_enable_or_not", false);
        boolean showMargin = settings.getBoolean("show_margin", false);
        if(showMargin){
            if (circle_margin != null) {
                circle_margin.remove();
            }
            if(gMap != null) {
                circle_margin = gMap.addCircle(new CircleOptions().strokeColor(Color.RED)
                        .center(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .radius(loc.getAccuracy()));
            }
            marginOfError.setText(String.valueOf("Margin of error: " + loc.getAccuracy() + "m"));
        }
        if (isGeoEnabled) {
            TextView txtView = (TextView) mView.findViewById(R.id.radiusMap);
            int rad = settings.getInt("geo_fence_value", 0);
            double lat = Double.longBitsToDouble(settings.getLong("geo_fence_cordLat",0));
            double lon = Double.longBitsToDouble(settings.getLong("geo_fence_cordLon",0));
            txtView.setText(getString(R.string.radiuscolon).concat(" " + String.valueOf(rad)));
            if (circle != null) {
                circle.remove();
            }
            if (gMap != null) {
                circle = gMap.addCircle(new CircleOptions().strokeColor(Color.GREEN).fillColor(0x5500ff00).center(new LatLng(lat, lon)).radius(rad));
            }
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            TextView rotation = (TextView) mView.findViewById(R.id.degrees);
            rotation.setText(String.format("%s%s", getString(R.string.degrees), Float.toString(Math.round(event.values[0]))));
            if(marker != null) {
                ORIENTATION = Math.round(event.values[0]);
                marker.setRotation(Math.round(event.values[0]));
            }
        }else if(event.sensor.getType() == Sensor.TYPE_PRESSURE){
            TextView press = (TextView) mView.findViewById(R.id.pressure);
            press.setText(String.format("%s%s", getString(R.string.pressure), event.values[0]));
        }
    }
    @Override
    public void onLocationChanged(Location loc) {
        if (loc.getAccuracy() < currentAccuracy) {
            if (gMap != null) {
                gMap.clear();
                marker = gMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .title("Device Location: " + loc.getLatitude() + loc.getLongitude()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).flat(true).anchor(0.5f, 0.5f).rotation(ORIENTATION));
                showExtras(loc);
                TextView txtView = (TextView) mView.findViewById(R.id.declare);
                txtView.setText(getString(R.string.declaration).concat(" " + loc.getProvider()));
                TextView acc = (TextView) mView.findViewById(R.id.locationAcc);
                acc.setText(String.format("%s%s", getString(R.string.accuracy), Float.toString(loc.getAccuracy())));
            }
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}