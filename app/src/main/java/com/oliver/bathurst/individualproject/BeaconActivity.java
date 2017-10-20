package com.oliver.bathurst.individualproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean tryNull = true;
                try{
                    //noinspection RedundantStringToString
                    deviceList.get(position).getName().toString(); //make sure it's not null, try parsing name
                }catch(Exception e){
                    tryNull = false;
                }

                if(tryNull){
                    addToList(deviceList.get(position));
                }else{
                    Snackbar.make(findViewById(R.id.drawer_layout), "Cannot add beacon", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);

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
        try{
            unregisterReceiver(mReceiver);
        }catch(Exception ignored){}
    }
    protected void onDestroy(){
        super.onDestroy();
        try {
            unregisterReceiver(mReceiver);
        }catch(Exception ignored){}
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
                if (i != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
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
        if (id == R.id.View_Beacons) {
            editBeacons();
        } else if (id == R.id.Erase_Beacons) {
            eraseBeacons();
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }
    private void editBeacons(){
        final ArrayList<BluetoothDevice> bluetoothDevices = getBTArray();
        if (bluetoothDevices.size() != 0) {
            try {
                ArrayList<String> names = new ArrayList<>();
                for (BluetoothDevice bl : bluetoothDevices) {
                    names.add(bl.getName());
                }

                final ArrayList<Integer> selected = new ArrayList<>();

                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Select Beacons to Delete")
                            .setMultiChoiceItems(names.toArray(new CharSequence[names.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                    if (isChecked) {
                                        selected.add(indexSelected);
                                    } else if (selected.contains(indexSelected)) {
                                        selected.remove(indexSelected);
                                    }
                                }
                            }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(getApplicationContext(), "Deleting: " + selected.size() + " beacon(s)", Toast.LENGTH_SHORT).show();
                                    for (Integer index : selected) {
                                        try {
                                            bluetoothDevices.remove((int) index);
                                        } catch (Exception ignored) {}
                                    }
                                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("BeaconKeys", new Gson().toJson(bluetoothDevices)).apply();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                } catch (Exception ignored) {}
            } else {
                Toast.makeText(getApplicationContext(), "No beacons found", Toast.LENGTH_SHORT).show();
            }
    }
    private ArrayList<BluetoothDevice> getBTArray(){
        return new Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("BeaconKeys", null),
                new TypeToken<ArrayList<BluetoothDevice>>() {}.getType());
    }
    private void addToList(final BluetoothDevice bluetoothDevice) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this).setTitle("Add to beacons list?");

        builder.setPositiveButton("ADD BEACON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    ArrayList<BluetoothDevice> arrList = new ArrayList<>();
                    if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("BeaconKeys", null) == null) {
                        arrList.add(bluetoothDevice);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("BeaconKeys", new Gson().toJson(arrList)).apply();
                    } else {
                        arrList = getBTArray();
                        if(!arrList.contains(bluetoothDevice)){
                            arrList.add(bluetoothDevice);
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("BeaconKeys", new Gson().toJson(arrList)).apply();
                            Snackbar.make(findViewById(R.id.drawer_layout), "Saved", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }else{
                            Snackbar.make(findViewById(R.id.drawer_layout), "Beacon already saved", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                    }
                }catch(Exception e){
                    Snackbar.make(findViewById(R.id.drawer_layout), "Unknown error saving prefs", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void eraseBeacons(){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Are you sure you want to delete all " + getBTArray().size() + " beacon(s)?");
        builder.setPositiveButton("DELETE ALL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("BeaconKeys", new Gson().toJson(new ArrayList<BluetoothDevice>())).apply(); //overwrite
                Snackbar.make(findViewById(R.id.drawer_layout), "Deleted All", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
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