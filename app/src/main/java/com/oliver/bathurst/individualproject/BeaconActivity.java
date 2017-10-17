package com.oliver.bathurst.individualproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class BeaconActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<BluetoothDevice> deviceList;
    private ArrayList<String> names;
    private BluetoothAdapter blue;
    private ListView devices;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);
        setTitle("Beacons");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        devices = (ListView) findViewById(R.id.devices);

        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplication(), "Selected: " + deviceList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        deviceList = new ArrayList<>();
        names = new ArrayList<>();
        blue = BluetoothAdapter.getDefaultAdapter();

        if(blue != null) {
            if (!blue.isEnabled()) {
                blue.enable();
                Toast.makeText(this, "Turning Bluetooth on...", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, "Scanning for beacons...", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try{
                    registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                    registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
                    blue.startDiscovery();
                    Toast.makeText(this, "Started discovery", Toast.LENGTH_SHORT).show();
                }catch(NullPointerException e){
                    Toast.makeText(this, "Cannot scan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Android version too low for BLE", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_SHORT).show();
        }
    }
    protected void onResume(){
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    private void updateDevices(BluetoothDevice dev) {
        if (!deviceList.contains(dev)) {
            deviceList.add(dev);
        }
    }
    @SuppressWarnings("unchecked")
    private void redrawListView(){
        if(names != null) {
            names.clear();
            for (BluetoothDevice i : deviceList) {
                if (!i.getName().equals("null") && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    String UUID = "";
                    if (i.getUuids() != null && i.getUuids().length != 0) {
                        for (int ID = 0; ID < i.getUuids().length; ID++) {
                            UUID += "UUID " + ID + ": " + i.getUuids()[ID] + "\n";
                        }
                    }
                    if (i.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                        names.add("Alias: " + i.getName() + "  (LOW ENERGY)\nAddress: " + i.getAddress()
                                + "\n" + UUID);
                    } else {
                        names.add("Alias: " + i.getName() + "\nAddress: " + i.getAddress()
                                + "\n" + UUID);
                    }
                }
            }
            devices.setAdapter(new ArrayAdapter(this, R.layout.list_view, R.id.listviewAdapt, names));
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_share) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction()) && blue.isDiscovering()) {
                updateDevices((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                redrawListView();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                blue.startDiscovery(); //restart discovery
            }
        }
    };
}