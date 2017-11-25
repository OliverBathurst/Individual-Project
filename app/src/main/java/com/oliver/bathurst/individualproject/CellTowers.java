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
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
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
                    updateUI();
                    tel.listen(new CellStateListener(), PhoneStateListener.LISTEN_CELL_INFO);
                } else {
                    Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.perform_permissions_checkup), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.error_string), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        } else {
            Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.build_number_low), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
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

    private void updateList(List<CellInfo> cellInfo) {
        Snackbar.make(findViewById(R.id.drawer_layout), R.string.cell_info_updated, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        if (general != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (cellInfo != null) {
                for (CellInfo cell : cellInfo) {
                    if (cell instanceof CellInfoGsm) {
                        CellInfoGsm gcm = ((CellInfoGsm) cell);
                        general.put(gcm.toString(), gcm);
                    } else if (cell instanceof CellInfoWcdma && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        CellInfoWcdma wcdma = ((CellInfoWcdma) cell);
                        general.put(wcdma.toString(), wcdma);
                    } else if (cell instanceof CellInfoLte) {
                        CellInfoLte lte = ((CellInfoLte) cell);
                        general.put(lte.toString(), lte);
                    } else if (cell instanceof CellInfoCdma){
                        CellInfoCdma cdma = (CellInfoCdma) cell;
                        general.put(cdma.toString(), cdma);
                    }
                }
            }
            if (tel != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    List<NeighboringCellInfo> neighboringCells = tel.getNeighboringCellInfo();
                    if(neighboringCells.isEmpty()){
                        Snackbar.make(findViewById(R.id.drawer_layout), R.string.noFriends, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    }else{
                        if (general != null) {
                            for (NeighboringCellInfo neighboringCellInfo : neighboringCells) {
                                general.put(neighboringCellInfo.getCid(), neighboringCellInfo);
                            }
                        }
                    }
                }
            }
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.viewMap) {
            startActivity(new Intent(this, CellTowerMap.class).putExtra("CELL_TOWERS", general));
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
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
    private void updateUI(){
        if(cells != null && list != null) {
            StringBuilder build = new StringBuilder();
            cells.clear();
            for (Object cellTower : general.values()) {
                if(cellTower instanceof CellInfoGsm){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                        CellInfoGsm cell = (CellInfoGsm) cellTower;
                        CellIdentityGsm cellCasted = ((CellInfoGsm) cellTower).getCellIdentity();

                        build.append(getString(R.string.CID)).append(cellCasted.getCid()).append("\n")
                                .append(getString(R.string.LAC)).append(cellCasted.getLac()).append("\n")
                                .append(getString(R.string.MCC)).append(cellCasted.getMcc()).append("\n")
                                .append(getString(R.string.MNC)).append(cellCasted.getMnc()).append("\n")
                                .append(getString(R.string.signalStrength)).append(((CellInfoGsm) cellTower).getCellSignalStrength().getDbm()).append("\n")
                                .append(getString(R.string.isActive)).append(cell.isRegistered()).append("\n")
                                .append(getString(R.string.typeCell)).append(getString(R.string.gcm));
                    }
                }else if(cellTower instanceof CellInfoLte){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        CellIdentityLte cellCasted = ((CellInfoLte) cellTower).getCellIdentity();

                        build.append(getString(R.string.CID)).append(cellCasted.getCi()).append("\n")
                                .append(getString(R.string.MCC)).append(cellCasted.getMcc()).append("\n")
                                .append(getString(R.string.MNC)).append(cellCasted.getMnc()).append("\n")
                                .append(getString(R.string.PCI)).append(cellCasted.getPci()).append("\n")
                                .append(getString(R.string.LAC)).append(cellCasted.getTac()).append("\n")
                                .append(getString(R.string.typeCell)).append(getString(R.string.lte));
                    }
                }else if(cellTower instanceof CellInfoWcdma){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                        CellInfoWcdma cell = (CellInfoWcdma) cellTower;
                        CellIdentityWcdma cellCasted = cell.getCellIdentity();

                        build.append(getString(R.string.CID)).append(cellCasted.getCid()).append("\n")
                                .append(getString(R.string.LAC)).append(cellCasted.getLac()).append("\n")
                                .append(getString(R.string.MCC)).append(cellCasted.getMcc()).append("\n")
                                .append(getString(R.string.MNC)).append(cellCasted.getMnc()).append("\n")
                                .append(getString(R.string.signalStrength)).append(cell.getCellSignalStrength().getDbm()).append("\n")
                                .append(getString(R.string.isActive)).append(cell.isRegistered()).append("\n")
                                .append(getString(R.string.typeCell)).append(getString(R.string.gcm));
                    }
                }else if(cellTower instanceof NeighboringCellInfo){

                    NeighboringCellInfo cellNeighbour = (NeighboringCellInfo) cellTower;
                    build.append(getString(R.string.neighbour)).append("\n")
                            .append(getString(R.string.CID)).append(cellNeighbour.getCid()).append("\n")
                            .append(getString(R.string.LAC)).append(cellNeighbour.getLac()).append("\n")
                            .append(getString(R.string.signalStrength)).append(cellNeighbour.getRssi()).append("\n")
                            .append(getString(R.string.typeCell)).append(cellNeighbour.getNetworkType());

                }else if(cellTower instanceof CellInfoCdma){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        CellIdentityCdma cdmaIdentity = ((CellInfoCdma) cellTower).getCellIdentity();

                        build.append(getString(R.string.cdma)).append("\n")
                                .append(getString(R.string.basestation_id)).append(cdmaIdentity.getBasestationId()).append("\n")
                                .append(getString(R.string.network_id)).append(cdmaIdentity.getNetworkId()).append("\n")
                                .append(getString(R.string.system_id)).append(cdmaIdentity.getSystemId()).append("\n")
                                .append(getString(R.string.latitude)).append(cdmaIdentity.getLatitude()).append("\n")
                                .append(getString(R.string.longitude)).append(cdmaIdentity.getLongitude());
                    }
                }
                cells.add(build.toString());
                build.setLength(0);
            }
            list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, cells));
        }
    }
}
