package com.oliver.bathurst.individualproject;

import android.content.Intent;
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
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CellTowers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TelephonyManager tel;
    private ListView list;
    private HashMap<Object, Object> general;
    private ArrayList<String> cells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_towers);
        setTitle(getString(R.string.cell_tower_activity_title));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        list = ((ListView) findViewById(R.id.cell_tower_listview));
        ((NavigationView) findViewById(R.id.nav_view)).setNavigationItemSelectedListener(this);

        tel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (tel != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    cells = new ArrayList<>();
                    general = new HashMap<>();
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
        Snackbar.make(findViewById(R.id.drawer_layout), R.string.cell_info_updated, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        if(general != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
           if(cellInfo != null) {
               for (CellInfo cell : cellInfo) {
                   if(cell instanceof CellInfoGsm) {
                       CellInfoGsm gcm = ((CellInfoGsm) cell);
                       general.put(gcm, new Cell(gcm.getCellIdentity().getCid(), gcm.getCellIdentity().getLac(), gcm.getCellIdentity().getMcc(), gcm.getCellIdentity().getMnc(), gcm.getCellSignalStrength().getDbm(), gcm.isRegistered()));
                   }else if(cell instanceof CellInfoWcdma && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
                       CellInfoWcdma gcm = ((CellInfoWcdma) cell);
                       general.put(gcm, new Cell(gcm.getCellIdentity().getCid(), gcm.getCellIdentity().getLac(), gcm.getCellIdentity().getMcc(), gcm.getCellIdentity().getMnc(), gcm.getCellSignalStrength().getDbm(), gcm.isRegistered()));
                   }else if(cell instanceof CellInfoLte) {
                       CellInfoLte gcm = ((CellInfoLte) cell);
                       general.put(gcm, new CellLte(gcm.getCellIdentity().getCi(), gcm.getCellIdentity().getMcc(), gcm.getCellIdentity().getMnc(), gcm.getCellIdentity().getPci(), gcm.getCellIdentity().getTac()));
                   }
               }
           }
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.viewMap) {
            Snackbar.make(findViewById(R.id.drawer_layout), R.string.viewingMap, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            startActivity(new Intent(this, CellTowerMap.class).putExtra("CELL_TOWERS", general));
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }
    private void addNeighbours(){
        //add neighbours



    }
    private void updateUI(){
        if(cells != null && list != null) {
            StringBuilder build = new StringBuilder();
            cells.clear();
            for (Object cellTower : general.values()) {
                if(cellTower instanceof Cell){
                    Cell cellCasted = (Cell) cellTower;
                    build.append(getString(R.string.CID)).append(cellCasted.getCID())
                            .append(getString(R.string.LAC)).append(cellCasted.getLAC())
                            .append(getString(R.string.MCC)).append(cellCasted.getMCC())
                            .append(getString(R.string.MNC)).append(cellCasted.getMNC())
                            .append(getString(R.string.signalStrength)).append(cellCasted.getSignal())
                            .append(getString(R.string.isActive)).append(cellCasted.isActive())
                            .append(getString(R.string.typeCell)).append(getString(R.string.gcm));
                    cells.add(build.toString());
                    build.setLength(0);
                }else if(cellTower instanceof CellLte){
                    CellLte cellCastedLTE = (CellLte) cellTower;
                    build.append(getString(R.string.ID)).append(cellCastedLTE.getID())
                            .append(getString(R.string.MCC)).append(cellCastedLTE.getMCC())
                            .append(getString(R.string.MNC)).append(cellCastedLTE.getMNC())
                            .append(getString(R.string.PCI)).append(cellCastedLTE.getPCI())
                            .append(getString(R.string.TAC)).append(cellCastedLTE.getTac())
                            .append(getString(R.string.typeCell)).append(getString(R.string.lte));
                    cells.add(build.toString());
                    build.setLength(0);
                }
            }
            list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, cells));
        }
    }
    public class CellStateListener extends PhoneStateListener {
        public void onCellInfoChanged (List<CellInfo> cellInfo){
            updateList(cellInfo);
            updateUI();
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
