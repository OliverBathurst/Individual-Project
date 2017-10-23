package com.oliver.bathurst.individualproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class BeaconConfig extends AppCompatActivity {
    private int selectedPosition = 0;
    private TextView signal;
    private BluetoothDevice globalDevice;
    private String globalDeviceName;
    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_beacon_config);

        globalDevice = new Gson().fromJson(getIntent().getStringExtra("BT_DEVICE"), new TypeToken<BluetoothDevice>() {}.getType());
        globalDeviceName = globalDevice.getName();
        signal = (TextView) findViewById(R.id.signal);
        ((TextView) findViewById(R.id.beaconName)).setText(globalDeviceName);


        Snackbar.make(findViewById(R.id.beaconContent), globalDeviceName + " Loaded", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        BTAdapter.startDiscovery();


        ((Spinner) findViewById(R.id.spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        (findViewById(R.id.saveBeacon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit = (EditText) findViewById(R.id.distanceBeacon);
                if(edit.getText().toString().trim().length() != 0){
                    try {
                        Float toSave = Float.parseFloat(edit.getText().toString().trim());
                        try{
                            int RSSI = Integer.parseInt(signal.getText().toString());





                        }catch(Exception e){
                            Snackbar.make(findViewById(R.id.beaconContent), "Failure to parse signal", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        }
                    }catch(Exception e){
                        Snackbar.make(findViewById(R.id.beaconContent), "Failure to parse text", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    }
                }else{
                    Snackbar.make(findViewById(R.id.beaconContent), "No distance provided", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }

                switch(selectedPosition){
                    case 0 :
                        break;
                    case 1 :
                        break;
                    default:
                        break;
                }
            }
        });
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                if(intent.getStringExtra(BluetoothDevice.EXTRA_NAME).equals(globalDeviceName)){
                    signal.setText(String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE)));
                    BTAdapter.cancelDiscovery();
                    BTAdapter.startDiscovery();
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
                BTAdapter.startDiscovery();
            }
        }
    };
}
