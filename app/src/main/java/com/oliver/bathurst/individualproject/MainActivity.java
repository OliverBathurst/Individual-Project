package com.oliver.bathurst.individualproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by Oliver on 17/06/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class MainActivity extends AppCompatActivity{
    private FrameLayout frame;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (!item.isChecked()) {
                item.setChecked(true);
                frame.removeAllViews();
                removeAllFragments();

                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        getSupportFragmentManager().beginTransaction().add(R.id.content, new MapFragment()).commit();
                        return true;
                    case R.id.navigation_dashboard:
                        getFragmentManager().beginTransaction().add(R.id.content, new SettingsFragment()).commit();
                        return true;
                    case R.id.navigation_device:
                        getFragmentManager().beginTransaction().add(R.id.content, new DeviceFragment()).commit();
                        return true;
                }
                return false;
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        ((BottomNavigationView) findViewById(R.id.navigation)).setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        frame = (FrameLayout) findViewById(R.id.content);
        getSupportFragmentManager().beginTransaction().add(R.id.content, new MapFragment()).commit();
    }
    private void removeAllFragments() {
        try {
            getSupportFragmentManager().getFragments().clear();
            while (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStackImmediate();
            }
        }catch(Exception ignored){}
    }
}