package com.oliver.bathurst.individualproject;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_reorder);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar bar = getSupportActionBar();
        if(bar != null) {
            bar.hide();
        }

        (findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrder();
                Reorder.super.onBackPressed();
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
                if (selected  < order.size()-1) {
                    Collections.swap(order, selected, selected + 1);
                    selected++;
                    reOrder();
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Warning")
                .setMessage("No not edit these without good reason, this order has been " +
                "specifically chosen for optimal accuracy")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void reOrder(){
        lv.setAdapter(new ArrayAdapter(this, R.layout.list_view, R.id.listviewAdapt, order));
    }
    private void saveOrder(){
        PreferenceManager.getDefaultSharedPreferences(getApplication()).edit()
                .putString("first", order.get(0)).putString("second", order.get(1)).putString("third", order.get(2)).apply();
    }
}