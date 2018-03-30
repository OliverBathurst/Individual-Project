package com.oliver.bathurst.individualproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WiFiScanner extends AppCompatActivity {
    private HashMap<String, Integer> wifiHashMap;//stores SSIDs and their signal strengths
    private int SCANS = 10;
    private String aliasString;
    private TextView alias, progressText;
    private WifiManager wifiMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_scanner);
        setTitle(getString(R.string.wifi_scanner_title));

        SCANS = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("total_scans", "10"));
        alias = findViewById(R.id.aliasWiFi);
        progressText = findViewById(R.id.progressUpdate);
        wifiMan = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        findViewById(R.id.print).setOnClickListener(v -> {
            if(wifiMan != null) {
                aliasString = alias.getText().toString().trim();
                if(aliasString.length() > 0) {
                    if (!wifiMan.isWifiEnabled()) {
                        progressText.setText(R.string.enabling_wifi);
                        wifiMan.setWifiEnabled(true);
                    }
                    wifiHashMap = new HashMap<>();
                    registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    wifiMan.startScan();
                    progressText.setText(R.string.scan_started);
                }else{
                    progressText.setText(R.string.blank_alias);
                }
            }else{
                progressText.setText(R.string.wifi_not_supported);
            }
        });
        findViewById(R.id.resetAll).setOnClickListener(v -> PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("WIFI_PRINTS", new Gson().toJson(new ArrayList<Pair<String, HashMap<String, Integer>>>())).apply());
    }
    private void save(){
        progressText.setText(R.string.saving);
        ArrayList<Pair<String, HashMap<String, Integer>>> toStore;//store alias along with a hashmap of SSIDs and averaged signal strengths
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String doesExist = sharedPreferences.getString("WIFI_PRINTS", null);

        if(doesExist != null){ //if the structure exists, deserialize
            toStore = new Gson().fromJson(doesExist, new TypeToken<ArrayList<Pair<String, HashMap<String, Integer>>>>() {}.getType()); //deserialize
            toStore.add(new Pair<>(aliasString, wifiHashMap));//add pair
        }else{//if null (non-existing)
            toStore = new ArrayList<>(); //create a new arraylist
            toStore.add(new Pair<>(aliasString, wifiHashMap));//add pair
        }
        //serialise and save into shared preferences
        sharedPreferences.edit().putString("WIFI_PRINTS", new Gson().toJson(toStore)).apply(); //write back to storage
        progressText.setText(R.string.finished);
    }
    private void adder(List<ScanResult> wifiList){
        if(SCANS != 0){//if there's scans left
            for(ScanResult scanResult: wifiList){//iterate over scan results
                Integer RSSI = wifiHashMap.get(scanResult.SSID);//get current RSSI for that SSID
                if(RSSI != null){//if already in hashmap
                    wifiHashMap.put(scanResult.SSID, ((RSSI + scanResult.level) / 2)); //calculate average and overwrite previous value with key
                }else{
                    wifiHashMap.put(scanResult.SSID, scanResult.level);//else add it to the list as a new AP
                }
            }
            SCANS--;//decrement scans left
            progressText.setText(String.format(Locale.UK, "%s%d", getString(R.string.scans_left), SCANS));
            wifiMan.startScan();//restart scan
        }else{
            save();//finally save
        }
    }
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adder(wifiMan.getScanResults());
        }
    };
    protected void onPause(){
        super.onPause();
        unregisterReceiver(wifiReceiver);
    }
}