package com.oliver.bathurst.individualproject;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver on 17/06/2017.
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings("DefaultFileTemplate")
public class GeoFencingFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {
    private GeofencingClient mGeofencingClient;
    private View mView;
    private Location loc;
    private int mRadius = 0, scaleFactorInt = 1;
    private GoogleMap gMap;
    private Circle circle,circle_margin;
    private TextView marginOfError;
    private final List<Geofence> myList = new ArrayList<>(1);

    public GeoFencingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_geomap, container, false);
        return mView;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView radiusText = (TextView) mView.findViewById(R.id.radiusTextView);
        final TextView scaleText = (TextView) mView.findViewById(R.id.scale);
        marginOfError = (TextView) mView.findViewById(R.id.margin_of_error_geomap);

        final MapView mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        String scaleFactor = settings.getString("geo_fence_scale", null);
        if (scaleFactor != null && scaleFactor.trim().length() != 0) {
            try {
                scaleFactorInt = Integer.parseInt(scaleFactor);
                scaleText.setText(getString(R.string.scalenozero).concat(" " + String.valueOf(scaleFactorInt)));
            } catch (Exception e) {
                scaleFactorInt = 1;
                scaleText.setText(R.string.scalewithone);
            }
        }
        SeekBar radius = (SeekBar) mView.findViewById(R.id.radius);
        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius = progress;
                try {
                    if (circle != null) {
                        circle.remove();
                    }
                    circle = gMap.addCircle(new CircleOptions().strokeColor(Color.GREEN).fillColor(0x5500ff00).center(new LatLng(loc.getLatitude(), loc.getLongitude())).radius(mRadius * scaleFactorInt));
                    radiusText.setText(getString(R.string.radiuscolon).concat(" " + String.valueOf(mRadius * scaleFactorInt)));

                } catch (Exception ignored) {}
            }
        });
        Button cancelButton = (Button) mView.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        });

        Button saveButton = (Button) mView.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int rad = (mRadius * scaleFactorInt);
                    SharedPreferences.Editor settings = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    settings.putInt("geo_fence_value", rad);
                    settings.putLong("geo_fence_cordLat", Double.doubleToRawLongBits(loc.getLatitude()));//geofence center latitude
                    settings.putLong("geo_fence_cordLon", Double.doubleToRawLongBits(loc.getLongitude()));//geofence center longitude
                    settings.apply();
                    mGeofencingClient = LocationServices.getGeofencingClient(getActivity());

                    Geofence geofence = new Geofence.Builder().setRequestId("geoId")
                            .setCircularRegion(loc.getLatitude(), loc.getLongitude(), rad) // defining fence region
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build();

                    myList.clear();
                    myList.add(geofence);

                    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
                    builder.addGeofences(myList);
                    GeofencingRequest geoReq = builder.build();

                    PendingIntent mGeofenceIntent = PendingIntent.getService(getActivity(),
                            0, new Intent(getActivity(), GeoFenceService.class), PendingIntent.FLAG_UPDATE_CURRENT);

                    try {
                        mGeofencingClient.addGeofences(geoReq, mGeofenceIntent);
                    } catch (SecurityException securityException) {
                        Toast.makeText(getActivity(), securityException.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    startActivity(new Intent(getContext(), MainActivity.class));

                }catch(Exception e){
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getContext(), MainActivity.class));
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            gMap = googleMap;
            LocationService locService = new LocationService(getActivity());
            loc = locService.getLoc();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean showMargin = settings.getBoolean("show_margin", false);
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

            if (showMargin) {
                if (circle_margin != null) {
                    circle_margin.remove();
                }
                circle_margin = googleMap.addCircle(new CircleOptions().strokeColor(Color.RED)
                        .center(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .radius(loc.getAccuracy()));
                marginOfError.setText(String.valueOf("Margin of error: " + loc.getAccuracy() + "m"));
            }

            MapsInitializer.initialize(getContext());
            gMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title("Device Location: " + loc.getLatitude() + loc.getLongitude()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).flat(true).anchor(0.5f,0.5f));
            CameraPosition cam = CameraPosition.builder().target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(19).bearing(0).tilt(45).build();
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}