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
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings("deprecation")
public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback, LocationListener, SensorEventListener {
    private float ORIENTATION = 0.0f;
    private View mView;
    private Circle circle, circle_margin;
    private TextView marginOfError;
    private GoogleMap gMap;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private Sensor gyro;
    private Marker marker;
    private SharedPreferences settings;

    public MapFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.fragment_map, container, false);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        marginOfError = (TextView) mView.findViewById(R.id.margin_of_error);

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        if ((sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) : null) != null) {
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
        if(gyro != null) {
            try {
                sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (SecurityException ignored) {}
        }
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        LocationService newLocationService = new LocationService(getActivity());
        if(newLocationService.tryProviders(locationManager, settings.getString("first", "GPS"), 2000, 1)){
            if(newLocationService.tryProviders(locationManager, settings.getString("second", "Wi-Fi"), 2000, 1)){
                if(newLocationService.tryProviders(locationManager, settings.getString("third", "Passive"), 2000, 1)){
                    Toast.makeText(getActivity(), "No providers available", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            gMap = googleMap;
            LocationService loc = new LocationService(getActivity());
            Location newLoc = loc.getLoc();

            String map = settings.getString("mapType", null);
            gMap.setMapType(map != null ? loc.getMapType(map) : GoogleMap.MAP_TYPE_NORMAL);

            ((TextView) mView.findViewById(R.id.declare)).setText(getString(R.string.declaration).concat(" " + newLoc.getProvider()));
            ((TextView) mView.findViewById(R.id.locationAcc)).setText(String.format("%s%s%s", getString(R.string.accuracy), Float.toString(newLoc.getAccuracy()), "m"));

            MapsInitializer.initialize(getContext());
            marker = gMap.addMarker(new MarkerOptions().position(new LatLng(newLoc.getLatitude(), newLoc.getLongitude()))
                    .title("Device Location: " + newLoc.getLatitude() + newLoc.getLongitude()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).flat(true).anchor(0.5f,0.5f));
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(new LatLng(newLoc.getLatitude(), newLoc.getLongitude())).zoom(19).bearing(0).tilt(45).build()));

            showExtras(newLoc);
        }catch(Exception e){
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void showExtras(Location loc){
        if(settings.getBoolean("show_margin", false)){
            if (circle_margin != null) {
                circle_margin.remove();
            }
            if(gMap != null) {
                circle_margin = gMap.addCircle(new CircleOptions().strokeColor(Color.GREEN).fillColor(0x5500ff00)
                        .center(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .radius(loc.getAccuracy()));
            }
            marginOfError.setText(String.valueOf("Margin of error: " + loc.getAccuracy() + "m"));
        }
        if (settings.getBoolean("geo_fence_enable_or_not", false)) {
            ((TextView) mView.findViewById(R.id.radiusMap)).setText(getString(R.string.radiuscolon).concat(" " + String.valueOf(settings.getInt("geo_fence_value", 0))));
            if (circle != null) {
                circle.remove();
            }
            if (gMap != null) {
                circle = gMap.addCircle(new CircleOptions().strokeColor(Color.GREEN).fillColor(0x5500ff00)
                        .center(new LatLng(Double.longBitsToDouble(settings.getLong("geo_fence_cordLat",0)),
                                Double.longBitsToDouble(settings.getLong("geo_fence_cordLon",0)))).radius(settings.getInt("geo_fence_value", 0)));
            }
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            ((TextView) mView.findViewById(R.id.degrees)).setText(String.format("%s%s", getString(R.string.degrees), Float.toString(Math.round(event.values[0]))));
            ORIENTATION = Math.round(event.values[0]);
            if(marker != null) {
                marker.setRotation(ORIENTATION);
            }
        }
    }
    @Override
    public void onLocationChanged(Location loc) {
        if (gMap != null) {
            gMap.clear();
            marker = gMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title("Device Location: " + loc.getLatitude() + loc.getLongitude()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).flat(true).anchor(0.5f, 0.5f).rotation(ORIENTATION));
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(19).bearing(0).tilt(45).build()));

            if(settings != null) {
                showExtras(loc);
            }
            ((TextView) mView.findViewById(R.id.declare)).setText(getString(R.string.declaration).concat(" " + loc.getProvider()));
            ((TextView) mView.findViewById(R.id.locationAcc)).setText(String.format("%s%s%s", getString(R.string.accuracy), Float.toString(loc.getAccuracy()), "m"));
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