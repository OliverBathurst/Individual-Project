package com.oliver.bathurst.individualproject;

import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.Serializable;
import java.util.HashMap;

public class CellTowerMap extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private HashMap cellTowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_tower_map);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        Serializable cellTowersReceived = getIntent().getSerializableExtra("CELL_TOWERS");
        if (cellTowersReceived instanceof HashMap) {
            cellTowers = (HashMap) cellTowersReceived;
        } else {
            Toast.makeText(this, getString(R.string.error_string), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(cellTowers != null) {
            processMap();
        }else{
            Toast.makeText(this, getString(R.string.error_string), Toast.LENGTH_SHORT).show();
        }
    }
    private void processMap(){
        CellTowerHelper cth = new CellTowerHelper(this);
        for(Object cell: cellTowers.values()){
            if(cell instanceof CellInfoGsm){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    CellIdentityGsm gsm  = ((CellInfoGsm) cell).getCellIdentity();
                    if(gsm.getCid() != 0) {
                        validate(gsm.getCid(), cth.callOpenCell(gsm.getCid(), gsm.getLac(), gsm.getMcc(), gsm.getMnc()));
                    }
                }
            }else if (cell instanceof CellInfoLte){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    CellIdentityLte lte = ((CellInfoLte) cell).getCellIdentity();
                    if(lte.getCi() != 0) {
                        validate(lte.getCi(), cth.callOpenCell(lte.getCi(), lte.getTac(), lte.getMcc(), lte.getMnc()));
                    }
                }
            }else if (cell instanceof CellInfoWcdma){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    CellIdentityWcdma wcdma = ((CellInfoWcdma) cell).getCellIdentity();
                    if(wcdma.getCid() != 0) {
                        validate(wcdma.getCid(), cth.callOpenCell(wcdma.getCid(), wcdma.getLac(), wcdma.getMcc(), wcdma.getMnc()));
                    }
                }
            }else if (cell instanceof CellInfoCdma){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    CellIdentityCdma  identity = ((CellInfoCdma) cell).getCellIdentity();
                    if(mMap != null) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(identity.getLatitude(), identity.getLongitude())).title(getString(R.string.cdma)));
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(new LatLng(identity.getLatitude(), identity.getLongitude())).zoom(15).bearing(0).tilt(45).build()));
                    }
                }
            }
        }
    }
    private void validate(int title, Double[] da){
        if(da != null && da[0] != null && da[1] != null && da[2] != null && mMap != null) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(da[0], da[1])).title(String.valueOf(title)));
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(new LatLng(da[0], da[1])).zoom(15).bearing(0).tilt(45).build()));
            mMap.addCircle(new CircleOptions().strokeColor(Color.GREEN).fillColor(0x5500ff00).center(new LatLng(da[0], da[1])).radius(da[2]));
        }else{
            Toast.makeText(this, R.string.can_not_get_location, Toast.LENGTH_SHORT).show();
        }
    }
}