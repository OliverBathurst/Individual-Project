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
import java.util.Date;
import java.util.List;

/**
 * Created by Oliver on 17/06/2017.
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
        final TextView scaleText = (TextView) mView.findViewById(R.id.scale);
        marginOfError = (TextView) mView.findViewById(R.id.margin_of_error_geomap);

        final MapView mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        String scaleFactor = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("geo_fence_scale", null);
        if (scaleFactor != null && scaleFactor.trim().length() != 0) {
            try {
                scaleFactorInt = Integer.parseInt(scaleFactor);
                scaleText.setText(getString(R.string.scalenozero).concat(String.valueOf(scaleFactorInt)));
            } catch (Exception e) {
                scaleFactorInt = 1;
                scaleText.setText(getString(R.string.scalewithone));
            }
        }
        ((SeekBar) mView.findViewById(R.id.radius)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius = progress;
                try {
                    if (circle != null) {
                        circle.remove();
                    }
                    circle = gMap.addCircle(new CircleOptions().strokeColor(Color.GREEN).fillColor(0x5500ff00).center(new LatLng(loc.getLatitude(), loc.getLongitude())).radius(mRadius * scaleFactorInt));
                    ((TextView) mView.findViewById(R.id.radiusTextView)).setText(getString(R.string.radiuscolon).concat(String.valueOf(mRadius * scaleFactorInt)));
                } catch (Exception ignored) {}
            }
        });
        (mView.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        });

        (mView.findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharedPreferences.Editor settings = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    settings.putInt("geo_fence_value", (mRadius * scaleFactorInt));
                    settings.putLong("geo_fence_cordLat", Double.doubleToRawLongBits(loc.getLatitude()));//geofence center latitude
                    settings.putLong("geo_fence_cordLon", Double.doubleToRawLongBits(loc.getLongitude()));//geofence center longitude
                    settings.apply();
                    mGeofencingClient = LocationServices.getGeofencingClient(getActivity());

                    myList.clear();
                    myList.add(new Geofence.Builder().setRequestId("geoId")
                            .setCircularRegion(loc.getLatitude(), loc.getLongitude(), (mRadius * scaleFactorInt)) // defining fence region
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build());

                    GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
                    builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
                    builder.addGeofences(myList);

                    try {
                        mGeofencingClient.addGeofences(builder.build(), PendingIntent.getService(getActivity(),
                                0, new Intent(getActivity(), GeoFenceService.class), PendingIntent.FLAG_UPDATE_CURRENT));
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
            LocationService locService = new LocationService(getActivity());
            gMap = googleMap;
            loc = locService.getLoc();

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());

            String map = settings.getString("mapType", null);
            gMap.setMapType(map != null ? locService.getMapType(map) : GoogleMap.MAP_TYPE_NORMAL);

            ((TextView) mView.findViewById(R.id.declare)).setText(getString(R.string.declaration).concat(loc.getProvider()));

            if (settings.getBoolean("show_margin", false)) {
                if (circle_margin != null) {
                    circle_margin.remove();
                }
                circle_margin = googleMap.addCircle(new CircleOptions().strokeColor(Color.RED)
                        .center(new LatLng(loc.getLatitude(), loc.getLongitude()))
                        .radius(loc.getAccuracy()));
                marginOfError.setText(String.valueOf(getString(R.string.margin_of_error) + loc.getAccuracy() + getString(R.string.meters_unit)));
            }
            MapsInitializer.initialize(getContext());
            gMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title(getString(R.string.device_location) + loc.getLatitude() + loc.getLongitude() + "\n" + new Date()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).flat(true).anchor(0.5f,0.5f));
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(19).bearing(0).tilt(45).build()));
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}