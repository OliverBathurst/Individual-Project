package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class CellTowerMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap cellTowers;
    private float myLatitude = 0, myLongitude = 0;
    private boolean fail = false;

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
        getHome();
        processMap();
    }
    private void getHome() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(telephonyManager != null){
                GsmCellLocation gsmLocation = (GsmCellLocation) telephonyManager.getCellLocation();
                locationFromGoogle(gsmLocation.getCid(), gsmLocation.getLac());
                validate(gsmLocation.getCid(), true);
            }
        }else{
            Toast.makeText(this, getString(R.string.perform_permissions_checkup), Toast.LENGTH_SHORT).show();
        }
    }
    private void processMap(){
        if(cellTowers != null){
            for(Object cell: cellTowers.values()){
                if(cell instanceof CellInfoGsm){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        CellIdentityGsm gsm  = ((CellInfoGsm) cell).getCellIdentity();
                        locationFromGoogle(gsm.getCid(), gsm.getLac());
                        validate(gsm.getCid(),false);
                    }
                }else if (cell instanceof CellInfoLte){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        CellIdentityLte gsm = ((CellInfoLte) cell).getCellIdentity();
                        locationFromGoogle(gsm.getCi(), gsm.getTac());
                        validate(gsm.getCi(), false);
                    }
                }else if (cell instanceof NeighboringCellInfo){
                    NeighboringCellInfo cellNeighbour = (NeighboringCellInfo) cell;
                    locationFromGoogle(cellNeighbour.getCid(), cellNeighbour.getLac());
                    validate(cellNeighbour.getCid(), false);
                }
            }
        }
    }
    private void validate(int title, boolean zoom){
        if(!fail){
            if(myLatitude != 0 && myLongitude != 0) {
                LatLng pos = new LatLng(myLatitude, myLongitude);
                mMap.addMarker(new MarkerOptions().position(pos).title(String.valueOf(title)));
                if (zoom) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(new LatLng(myLatitude, myLongitude)).zoom(19).bearing(0).tilt(45).build()));
                }
            }else{
                Toast.makeText(this, "Cannot get home network location", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void locationFromGoogle(final int cid, final int lac){
         new AsyncTask<Void, Void, Void>(){
            protected Void doInBackground(Void... voids) {
                try {
                    HttpURLConnection httpConn = (HttpURLConnection) new URL("http://www.google.com/glm/mmap").openConnection();
                    httpConn.setRequestMethod("POST");
                    httpConn.setDoOutput(true);
                    httpConn.setDoInput(true);
                    httpConn.connect();
                    WriteData(httpConn.getOutputStream(), cid, lac);
                    DataInputStream dataInputStream = new DataInputStream(httpConn.getInputStream());
                    dataInputStream.readShort();
                    dataInputStream.readByte();
                    if (dataInputStream.readInt() == 0) {
                        myLatitude = dataInputStream.readInt() / 1000000;
                        myLongitude = dataInputStream.readInt() / 1000000;
                        fail = false;
                    }else{
                        fail = true;
                    }
                } catch (IOException ignored) {}
                return null;
            }
        }.execute();
    }
    private void WriteData(OutputStream out, int CID, int LAC) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Web");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(3);
        dataOutputStream.writeUTF("");
        dataOutputStream.writeInt(CID);
        dataOutputStream.writeInt(LAC);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();
    }
}
