package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WiFiScanner extends AppCompatActivity {
    private int scans = 0, TOTAL_SCANS = 10;
    private String aliasString;
    private TextView alias, progressText;
    private WifiManager wifiMan;
    private WifiReceiver broadcast;
    private ArrayList<Pair<String, Integer>> wifiScanList; //stores SSIDs and their signal strengths

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_scanner);
        setTitle(getString(R.string.wifi_scanner_title));

        TOTAL_SCANS = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("total_scans", "10"));
        alias = (TextView) findViewById(R.id.aliasWiFi);
        progressText = (TextView) findViewById(R.id.progressUpdate);
        wifiMan = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        findViewById(R.id.print).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiMan != null) {
                    aliasString = alias.getText().toString().trim();
                    if(aliasString.length() > 0) {
                        if (!wifiMan.isWifiEnabled()) {
                            progressText.setText(R.string.enabling_wifi);
                            wifiMan.setWifiEnabled(true);
                        }
                        wifiScanList = new ArrayList<>();
                        broadcast = new WifiReceiver();
                        registerReceiver(broadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        wifiMan.startScan();
                        progressText.setText(R.string.scan_started);
                    }else{
                        progressText.setText(R.string.blank_alias);
                    }
                }else{
                    progressText.setText(R.string.wifi_not_supported);
                }
            }
        });
        findViewById(R.id.resetAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("WIFI_PRINTS", new Gson().toJson(new ArrayList<Pair<String, HashMap<String, Integer>>>())).apply();
            }
        });
    }
    private void reduceAndSave(){
        progressText.setText(R.string.reducing_and_saving);
        HashMap<String, Integer> storage = new HashMap<>();
        for(int i = 0; i < wifiScanList.size(); i++){
            Integer temp = storage.get(wifiScanList.get(i).first); //get the value associated with the SSID String key
            if(temp != null) { //if the value associated with the key already exists...
                storage.put(wifiScanList.get(i).first, ((temp + wifiScanList.get(i).second)/2)); // add the new value to the old one and halve to average, write back with the same key
            }else{
                storage.put(wifiScanList.get(i).first, wifiScanList.get(i).second); //otherwise place in hashmap
            }
        }
        ArrayList<Pair<String, HashMap<String, Integer>>> toStore;//store alias along with a hashmap of SSIDs and averaged signal strengths
        String doesExist = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("WIFI_PRINTS", null);
        if(doesExist != null){ //if the string exists
            toStore = new Gson().fromJson(doesExist, new TypeToken<ArrayList<Pair<String, HashMap<String, Integer>>>>() {}.getType()); //deserialize
            toStore.add(new Pair<>(aliasString, storage));//add pair
        }else{//if null (non-existing)
            toStore = new ArrayList<>(); //create a new arraylist
            toStore.add(new Pair<>(aliasString, storage));//add pair
        }
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("WIFI_PRINTS", new Gson().toJson(toStore)).apply(); //write back to storage
        progressText.setText(R.string.finished);
    }
    private void adder(List<ScanResult> wifiList){
        if(scans != TOTAL_SCANS){
            for(int i = 0; i < wifiList.size(); i++){
                wifiScanList.add(new Pair<>(wifiList.get(i).SSID, wifiList.get(i).level));
            }
            scans++;
            progressText.setText(String.format(Locale.UK, "%s%d%s%d%s%s%d%s", getString(R.string.scans_finished), scans, getString(R.string.slash), TOTAL_SCANS, " , " , "(" , ((scans/TOTAL_SCANS)*100),  ")"));
            wifiMan.startScan();
        }else{
            reduceAndSave();
        }
    }
    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            adder(wifiMan.getScanResults());
        }
    }
    protected void onPause(){
        super.onPause();
        unregisterReceiver(broadcast);
    }
}