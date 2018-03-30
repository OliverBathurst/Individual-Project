package com.oliver.bathurst.individualproject;

import android.app.Fragment;
import android.os.Build;
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
                frame.removeAllViews();

                getSupportFragmentManager().getFragments().clear();
                while (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStackImmediate();
                }
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        navigate(new MapFragment(), item);
                        return true;
                    case R.id.navigation_dashboard:
                        navigate(new SettingsFragment(), item);
                        return true;
                    case R.id.navigation_device:
                        navigate(new DeviceFragment(), item);
                        return true;
                }
                return false;
            }
            return false;
        }
    };
    private void navigate(Fragment f, MenuItem i){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getFragmentManager().beginTransaction().add(R.id.content, f).commitNow();
        }else{
            getFragmentManager().beginTransaction().add(R.id.content, f).commit();
        }
        i.setChecked(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        frame = findViewById(R.id.content);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
    }
}