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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WiFiScanner extends AppCompatActivity {
    private static final int TOTAL_SCANS = 10;
    private int scans = 0;
    private String aliasString;
    private TextView alias;
    private ProgressBar progress;
    private WifiManager wifiMan;
    private WifiReceiver broadcast;
    private ArrayList<Pair<String, Integer>> wifiScanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_scanner);
        setTitle(getString(R.string.wifi_scanner_title));

        alias = (TextView) findViewById(R.id.aliasWiFi);
        wifiMan = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        progress = (ProgressBar) findViewById(R.id.progressFingerprint);

        findViewById(R.id.print).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiMan != null) {
                    aliasString = alias.getText().toString().trim();
                    if(aliasString.length() > 0) {
                        if (!wifiMan.isWifiEnabled()) {
                            wifiMan.setWifiEnabled(true);
                        }
                        wifiScanList = new ArrayList<>();
                        broadcast = new WifiReceiver();
                        registerReceiver(broadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        wifiMan.startScan();
                    }else{
                        Toast.makeText(getApplicationContext(), "Alias cannot be blank", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "WiFi not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void reduceAndSave(){
        HashMap<String, Integer> storage = new HashMap<>();
        for(int i = 0; i < wifiScanList.size(); i++){
            Integer temp = storage.get(wifiScanList.get(i).first);
            if(temp != null) {
                storage.put(wifiScanList.get(i).first, ((temp + wifiScanList.get(i).second)/2));
            }else{
                storage.put(wifiScanList.get(i).first, wifiScanList.get(i).second);
            }
        }
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("WIFI_PRINTS", new Gson().toJson(storage)).apply();
        Toast.makeText(getApplicationContext(), "Finished", Toast.LENGTH_SHORT).show();
    }
    private void updateStats(){
        scans++;
        progress.setProgress((scans/TOTAL_SCANS) * 100);
        wifiMan.startScan();
    }
    private void scanner(List<ScanResult> wifiList){
        if(scans != TOTAL_SCANS){
            for(int i = 0; i < wifiList.size(); i++){
                wifiScanList.add(new Pair<>(wifiList.get(i).SSID, wifiList.get(i).level));
            }
            updateStats();
        }else{
            reduceAndSave();
        }
    }
    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            scanner(wifiMan.getScanResults());
        }
    }
    protected void onPause(){
        super.onPause();
        unregisterReceiver(broadcast);
    }
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcast);
    }
}
