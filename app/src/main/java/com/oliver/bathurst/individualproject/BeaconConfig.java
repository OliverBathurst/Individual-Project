package com.oliver.bathurst.individualproject;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

public class BeaconConfig extends AppCompatActivity {
    private int selectedPosition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_beacon_config);

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
}
