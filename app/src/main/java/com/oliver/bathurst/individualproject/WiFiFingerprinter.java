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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Oliver on 11/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class WiFiFingerprinter {
    private ArrayList<Pair<String, HashMap<String, Integer>>> fromPrefs;
    private final Context c;
    private boolean isFinished = false;
    private WifiManager wifiMan;
    private String response;

    WiFiFingerprinter(Context context){
        this.c = context;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    String getResults(){
        startScan();
        while (!isFinished){}
        return response;
    }

    private void compare(List<ScanResult> results){
        final ArrayList<Pair<String, Integer>> points = new ArrayList<>(); //store aliases and score
        for(Pair<String, HashMap<String, Integer>> alias: fromPrefs){ //loop through aliases
            int temp = 0;//initialise score
            boolean hasFound = false;
            for(ScanResult scanned: results){//loop through scan results
                if(alias.second.containsKey(scanned.SSID)){//if the SSID exists in the alias' map
                    temp += Math.abs(alias.second.get(scanned.SSID) - scanned.level); //add the difference in signal to the score
                    hasFound = true;
                }
            }
            if(hasFound){
                points.add(new Pair<>(alias.first, temp)); //if at least one matched AP was found, add to score map
            }
        }
        if(points.isEmpty()){
            finish(c.getString(R.string.no_saved_points));
        }else{
            Collections.sort(points, (o1, o2) -> o1.second - o2.second);//sort ascending
            finish(c.getString(R.string.alias) + points.get(points.size()-1).first);//pick last entry
        }
    }
    private void startScan(){
        String doesExist = PreferenceManager.getDefaultSharedPreferences(c).getString("WIFI_PRINTS", null);
        if(doesExist != null) {
            fromPrefs = new Gson().fromJson(doesExist, new TypeToken<ArrayList<Pair<String, HashMap<String, Integer>>>>() {}.getType());

            if(!fromPrefs.isEmpty()) {
                wifiMan = (WifiManager) c.getApplicationContext().getSystemService(WIFI_SERVICE);
                if (wifiMan != null) {
                    c.getApplicationContext().registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    wifiMan.startScan();
                } else {
                    finish(c.getString(R.string.wifi_not_supported));
                }
            }else{
                finish(c.getString(R.string.no_fingerprints_found));
            }
        }else{
            finish(c.getString(R.string.no_fingerprints_found));
        }
    }
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            compare(wifiMan.getScanResults());
        }
    };
    private void finish(String endText){
        response = endText;
        isFinished = true;
    }
}