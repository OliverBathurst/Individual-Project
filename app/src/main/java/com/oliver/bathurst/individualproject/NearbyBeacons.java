package com.oliver.bathurst.individualproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

/**
 * Created by Oliver on 02/11/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class NearbyBeacons {
    private ArrayList<Pair<BluetoothDevice, Integer>> deviceList, finalList;
    private BluetoothAdapter blue;
    private Context context;
    private boolean isFinished = false;

    NearbyBeacons(Context c){
        this.blue = BluetoothAdapter.getDefaultAdapter();
        this.deviceList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.context = c;
    }
    ArrayList run(){
        scan();
        while(!isFinished){}
        return finalList;
    }

    void scan(){
        if(blue != null) {
            if (!blue.isEnabled()) {
                blue.enable();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                context.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                context.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
                blue.startDiscovery();
            }else{
                isFinished = true;
            }
        }else{
            isFinished = true;
        }
    }
    private void updateList(BluetoothDevice blueDev, int rssi){
        boolean found = false;
        try {
            for (Pair<BluetoothDevice, Integer> p : deviceList) {
                if (p.first.equals(blueDev)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                deviceList.add(new Pair<>(blueDev, rssi));
            }
        }catch(Exception ignored){}// if .equals() is performed on a null object
    }
    private void compare() {
        ArrayList<BluetoothDevice> fromPrefs = new Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(context)
                .getString("BeaconKeys", null), new TypeToken<ArrayList<BluetoothDevice>>() {}.getType());

        if(fromPrefs != null && !fromPrefs.isEmpty()) {
            for (BluetoothDevice bd : fromPrefs) {
                for (Pair<BluetoothDevice, Integer> p : deviceList) {
                    try {
                        if (p.first.equals(bd)) { //if it finds a saved beacon
                            finalList.add(p);
                            break;
                        }
                    }catch(Exception ignored){} //if .equals on null object
                }
            }
        }
        isFinished = true;
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction()) && blue.isDiscovering()) {
                updateList((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE), (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                compare();
            }
        }
    };
}