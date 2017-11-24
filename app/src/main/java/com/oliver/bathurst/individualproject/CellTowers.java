package com.oliver.bathurst.individualproject;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import java.util.List;

public class CellTowers extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TelephonyManager tel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_towers);
        setTitle(getString(R.string.cell_tower_activity_title));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);

        tel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (tel != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    updateList(tel.getAllCellInfo());
                    tel.listen(new CellStateListener(),PhoneStateListener.LISTEN_CELL_INFO);
                }else{
                    Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.perform_permissions_checkup), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }else{
                Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.error_string), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }else{
            Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.build_number_low), Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private void updateList(List<CellInfo> cellInfo){
        Snackbar.make(findViewById(R.id.drawer_layout), "Cell Info Changed", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.viewMap) {
            Snackbar.make(findViewById(R.id.drawer_layout), "Viewing Map...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }
    public class CellStateListener extends PhoneStateListener {
        public void onCellInfoChanged (List<CellInfo> cellInfo){
            updateList(cellInfo);
        }
    }
    public void onDestroy() {
        super.onDestroy();
        if(tel != null) {
            tel.listen(null, PhoneStateListener.LISTEN_NONE);
        }
    }
    public void onPause() {
        super.onPause();
        if(tel != null) {
            tel.listen(null, PhoneStateListener.LISTEN_NONE);
        }
    }
    public void onResume(){
        super.onResume();
        if(tel != null) {
            tel.listen(new CellStateListener(), PhoneStateListener.LISTEN_NONE);
        }
    }
}
