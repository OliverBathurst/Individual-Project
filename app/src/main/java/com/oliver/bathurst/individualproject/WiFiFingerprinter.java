package com.oliver.bathurst.individualproject;

import static android.content.Context.WIFI_SERVICE;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Oliver on 11/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class WiFiFingerprinter {
    private final Context c;
    private boolean isFinished = false;
    private String response;
    private WifiManager wifiMan;
    private ArrayList<Pair<String, HashMap<String, Integer>>> fromPrefs;

    WiFiFingerprinter(Context context){
        this.c = context;
    }

    String getResults(){
        startScan();
        while (!isFinished){}
        return response;
    }

    private void compare(List<ScanResult> results){
        HashMap<String, Integer> points = new HashMap<>();
        for(Pair<String, HashMap<String, Integer>> alias: fromPrefs){
            for(ScanResult scanned: results){

            }
        }
    }
    private void startScan(){
        String doesExist = PreferenceManager.getDefaultSharedPreferences(c).getString("WIFI_PRINTS", null);
        if(doesExist != null) {

            fromPrefs = new Gson().fromJson(doesExist, new TypeToken<ArrayList<Pair<String, HashMap<String, Integer>>>>() {}.getType());
            wifiMan = (WifiManager) c.getApplicationContext().getSystemService(WIFI_SERVICE);

            if(wifiMan != null){
                c.getApplicationContext().registerReceiver(new WifiReceiver(), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiMan.startScan();
            }else{
                response = c.getString(R.string.wifi_not_supported);
                isFinished = true;
            }
        }else{
            response = c.getString(R.string.no_fingerprints_found);
            isFinished = true;
        }
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            compare(wifiMan.getScanResults());
        }
    }
}