package com.oliver.bathurst.individualproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Oliver on 03/10/2017.
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
        setSupportActionBar(findViewById(R.id.toolbar));

        ActionBar bar = getSupportActionBar();
        if(bar != null) {
            bar.hide();
        }

        (findViewById(R.id.fab)).setOnClickListener(view -> {
            saveOrder();
            Reorder.super.onBackPressed();
        });
        order = new ArrayList<>();
        lv = findViewById(R.id.list);

        final SharedPreferences settingsView = PreferenceManager.getDefaultSharedPreferences(this);
        Collections.addAll(order, settingsView.getString("first", getString(R.string.gps_value)), settingsView.getString("second", getString(R.string.wifi_value)),
                    settingsView.getString("third", getString(R.string.passive_value)));
        lv.setAdapter(new ArrayAdapter(this, R.layout.list_view, R.id.listviewAdapt, order));

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(getApplication(), getString(R.string.selected) + order.get(position), Toast.LENGTH_SHORT).show();
            selected = position;
        });
        (findViewById(R.id.up)).setOnClickListener(v -> {
            if (selected > 0) {
                Collections.swap(order, selected, selected -1);
                selected--;
                reOrder();
            }
        });
        (findViewById(R.id.down)).setOnClickListener(v -> {
            if (selected  < order.size()-1) {
                Collections.swap(order, selected, selected + 1);
                selected++;
                reOrder();
            }
        });
        new AlertDialog.Builder(this).setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.locator_precedence_warning))
                .setPositiveButton(getString(R.string.OK), (dialog, id) -> dialog.dismiss()).create().show();
    }
    private void reOrder(){
        lv.setAdapter(new ArrayAdapter(this, R.layout.list_view, R.id.listviewAdapt, order));
    }
    private void saveOrder(){
        PreferenceManager.getDefaultSharedPreferences(getApplication()).edit()
                .putString("first", order.get(0)).putString("second", order.get(1)).putString("third", order.get(2)).apply();
    }
}