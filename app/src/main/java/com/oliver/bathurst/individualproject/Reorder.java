package com.oliver.bathurst.individualproject;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Oliver on 03/10/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

@SuppressWarnings("unchecked")
public class Reorder extends AppCompatActivity {

    private ArrayList<String> order;
    private ListView lv;
    private int selected = 0;
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_reorder);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        try {
            getSupportActionBar().hide();
        }catch(Exception ignored){}

        (findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saving...", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                saveOrder();
                Snackbar.make(view, "Saved", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });
        order = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        final SharedPreferences settingsView = PreferenceManager.getDefaultSharedPreferences(this);
        Collections.addAll(order, settingsView.getString("first", "GPS"), settingsView.getString("second", "Wi-Fi"),
                    settingsView.getString("third", "Passive"));
        lv.setAdapter(new ArrayAdapter(this, R.layout.list_view, R.id.listviewAdapt, order));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplication(), "Selected: " + order.get(position), Toast.LENGTH_SHORT).show();
                selected = position;
            }
        });
        (findViewById(R.id.up)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected > 0) {
                    Collections.swap(order, selected, selected -1);
                    selected--;
                    reOrder();
                }
            }
        });
        (findViewById(R.id.down)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected + 1 < order.size()) {
                    Collections.swap(order, selected, selected + 1);
                    selected++;
                    reOrder();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("No not edit these without good reason, this order has been " +
                "specifically chosen for optimal accuracy");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}
        });
        builder.create().show();
    }
    private void reOrder(){
        lv.setAdapter(new ArrayAdapter(this, R.layout.list_view, R.id.listviewAdapt, order));
    }
    private void saveOrder(){
        final SharedPreferences.Editor settingsView = PreferenceManager.getDefaultSharedPreferences(getApplication()).edit();
        settingsView.putString("first", order.get(0));
        settingsView.putString("second", order.get(1));
        settingsView.putString("third", order.get(2));
        settingsView.apply();
    }
}