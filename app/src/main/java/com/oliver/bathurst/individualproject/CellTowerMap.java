package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class CellTowerMap extends FragmentActivity implements OnMapReadyCallback {

    private static final String MOBILE_LOCATION_URI = "http://www.opencellid.org/cell/get?key=978e483439b03f";
    private GoogleMap mMap;
    private HashMap cellTowers;
    private double lat = 0, lon = 0, range = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_tower_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Serializable cellTowersReceived = getIntent().getSerializableExtra("CELL_TOWERS");
        if (cellTowersReceived instanceof HashMap) {
            cellTowers = (HashMap) cellTowersReceived;
        } else {
            Toast.makeText(this, getString(R.string.error_string), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(cellTowers != null) {
            processMap();
        }
    }
    private void processMap(){
        if(cellTowers != null){
            for(Object cell: cellTowers.values()){
                if(cell instanceof CellInfoGsm){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        CellIdentityGsm gsm  = ((CellInfoGsm) cell).getCellIdentity();
                        if(gsm.getCid() != 0) {
                            validate(gsm.getCid(), locationFromOpenCellID(gsm.getCid(), gsm.getLac(), gsm.getMcc(), gsm.getMnc()));
                        }
                    }
                }else if (cell instanceof CellInfoLte){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        CellIdentityLte gsm = ((CellInfoLte) cell).getCellIdentity();
                        if(gsm.getCi() != 0) {
                            validate(gsm.getCi(), locationFromOpenCellID(gsm.getCi(), gsm.getTac(), gsm.getMcc(), gsm.getMnc()));
                        }
                    }
                }else if (cell instanceof CellInfoWcdma){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        CellIdentityWcdma gsm = ((CellInfoWcdma) cell).getCellIdentity();
                        if(gsm.getCid() != 0) {
                            validate(gsm.getCid(), locationFromOpenCellID(gsm.getCid(), gsm.getLac(), gsm.getMcc(), gsm.getMnc()));
                        }
                    }
                }
            }
        }
    }
    private void validate(int title, Double[] da){
        if(da != null && da[0] != null && da[1] != null && da[2] != null && mMap != null) {
            if(da[0] != 0 && da[1] != 0) {
                try {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(da[0], da[1])).title(String.valueOf(title)));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(new LatLng(da[0], da[1])).zoom(15).bearing(0).tilt(45).build()));
                    mMap.addCircle(new CircleOptions().strokeColor(Color.GREEN).fillColor(0x5500ff00)
                            .center(new LatLng(da[0], da[1]))
                            .radius(da[2]));
                }catch (Exception ignored){}
            }
        }else{
            Toast.makeText(this, "Cannot get location", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private Double[] locationFromOpenCellID(final int cid, final int lac, final int mcc, final int mnc){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Double[] doubleArr = null;
        String toPost = MOBILE_LOCATION_URI + "&mcc=" + mcc + "&mnc=" + mnc + "&cellid=" + cid + "&lac=" + lac;
        System.out.println(toPost);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(toPost).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(connection.getInputStream());
            doc.normalize();
            NodeList nodeList = doc.getElementsByTagName("cell");
            if(nodeList.getLength() >= 1){
                NamedNodeMap attributes = nodeList.item(0).getAttributes();
                lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
                lon = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());
                range = Double.parseDouble(attributes.getNamedItem("range").getNodeValue());
                doubleArr = new Double[] {lat, lon, range};
            }else{
                doubleArr = new Double[] {0.0, 0.0, 0.0};
            }
        }catch (Exception ignored){}

        return doubleArr;
    }
}