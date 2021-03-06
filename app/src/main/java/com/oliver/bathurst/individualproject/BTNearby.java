package com.oliver.bathurst.individualproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

/**
 * Created by Oliver on 02/11/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class BTNearby {
    private final ArrayList<Pair<BluetoothDevice, Integer>> deviceList, finalList;
    private final BluetoothAdapter blue;
    private final Context context;
    private volatile boolean isFinished = false;
    private ArrayList<BluetoothDevice> bluetoothArray;

    BTNearby(Context c){
        this.blue = BluetoothAdapter.getDefaultAdapter();
        this.deviceList = new ArrayList<>();
        this.finalList = new ArrayList<>();
        this.context = c;
    }
    @SuppressWarnings("StatementWithEmptyBody")
    String run(){
        if(blue != null){
            if (!blue.isEnabled()) {
                blue.enable();
            }
            bluetoothArray = getBTArray();
            if(bluetoothArray != null && !bluetoothArray.isEmpty()){
                scan();
                while(!isFinished){}
                return getSummary();
            }else{
                return context.getString(R.string.no_beacons_found);
            }
        }else{
            return context.getString(R.string.bluetooth_not_available);
        }
    }
    private String getSummary(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder sb = new StringBuilder();
        for(Pair<BluetoothDevice, Integer> p: finalList){
            Float temp = sp.getFloat(p.first.getName(), Float.MAX_VALUE);//default to max value for error checking
            if(temp != Float.MAX_VALUE){
                sb.append(context.getString(R.string.beacon_device)).append(p.first.getName()).append("\n")
                        .append(context.getString(R.string.est_distance_m))
                        .append(p.second/temp).append(context.getString(R.string.centimetres)).append("\n");
            }
        }
        return sb.toString().trim().length() != 0 ? sb.toString().trim() : context.getString(R.string.no_beacons_found);
    }
    private void scan(){
        context.getApplicationContext().registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.getApplicationContext().registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        if(blue.isDiscovering()) {
            blue.cancelDiscovery();
        }
        blue.startDiscovery();
    }
    private void updateList(BluetoothDevice blueDev, int rssi){
        if(blueDev != null) {
            boolean found = false;
            for (Pair<BluetoothDevice, Integer> p : deviceList) {
                if (p.first.equals(blueDev)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                deviceList.add(new Pair<>(blueDev, rssi));
            }
        }
    }
    private ArrayList<BluetoothDevice> getBTArray(){
        return new Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(context)
                .getString("BeaconKeys", null), new TypeToken<ArrayList<BluetoothDevice>>() {}.getType());
    }
    private void compare() {
        for (BluetoothDevice bd : bluetoothArray) {
            if(bd != null) {
                for (Pair<BluetoothDevice, Integer> p : deviceList) {
                    if (p.first.equals(bd)) { //if it finds a saved beacon
                        finalList.add(p);
                        break;
                    }
                }
            }
        }
        context.getApplicationContext().unregisterReceiver(mReceiver);
        isFinished = true;
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                updateList(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE), (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                compare();
            }
        }
    };
}