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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Oliver on 11/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class WiFiFingerprinter {
    private final Context c;
    private ArrayList<Pair<String, HashMap<String, Integer>>> fromPrefs;
    private boolean isFinished = false;
    private WifiManager wifiMan;
    private String response;

    WiFiFingerprinter(Context context){
        this.c = context;
    }

    String getResults(){
        startScan();
        while (!isFinished){}
        return response;
    }

    private void compare(List<ScanResult> results){
        final HashMap<String, Integer> points = new HashMap<>();
        for(Pair<String, HashMap<String, Integer>> alias: fromPrefs){
            int temp = 0;
            boolean hasFound = false;
            for(ScanResult scanned: results){
                if(alias.second.containsKey(scanned.SSID)){
                    temp += (alias.second.get(scanned.SSID) - scanned.level);
                    hasFound = true;
                }
            }
            if(hasFound){
                points.put(alias.first, temp);
            }
        }
        if(points.isEmpty()){
            response = "No saved access points found";
            isFinished = true;
        }else{
            String currAlias = "";
            Integer currLowScore = 0;

            Iterator it = points.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Integer APDifference = (Integer)pair.getValue();
                if(currLowScore > APDifference){
                    currAlias = (String) pair.getKey();
                    currLowScore = APDifference;
                }
                it.remove();
            }
            response = c.getString(R.string.alias) + currAlias;
            isFinished = true;
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