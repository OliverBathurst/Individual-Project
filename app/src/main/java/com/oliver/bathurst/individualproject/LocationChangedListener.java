package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import com.google.android.gms.location.LocationResult;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Oliver on 06/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

public class LocationChangedListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if(LocationResult.hasResult(intent)){
                updateDB(context, LocationResult.extractResult(intent).getLastLocation());
            }
        }
    }
    private void updateDB(Context c, Location loc){
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(c);
        String user = shared.getString("registered_user",null);
        String pass = shared.getString("registered_pass",null);

        if(isValid(user) && isValid(pass)){
            if(shared.getBoolean("only_wifi", false)){ //only update over wifi
                ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
                if(cm != null){
                    NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifiNetwork != null && wifiNetwork.isConnected()) {
                        new sendLocationToDB(loc, user, pass, c).execute();
                    }
                }
            }else{
                new sendLocationToDB(loc, user, pass, c).execute();
            }
        }
    }
    private boolean isValid(String compare){
        return compare != null && compare.length() > 0;
    }
    @SuppressLint("StaticFieldLeak")
    public class sendLocationToDB extends AsyncTask<Void, Void, Void> {
        private final String username, password;
        private final Location loc;
        private final Context c;

        sendLocationToDB(Location location, String user, String pass, Context context) {
            this.loc = location;
            this.username = user;
            this.password = pass;
            this.c = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream((new URL(c.getString(R.string.update_redirect)).openConnection()).getInputStream())));
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                br.close();

                BufferedReader read = new BufferedReader(new InputStreamReader(new BufferedInputStream((new URL(sb.toString().trim() + "&user=" + username + "&pass="
                        + password + "&lat=" + loc.getLatitude() + "&lon=" + loc.getLongitude()).openConnection()).getInputStream())));
                read.close();
            }catch(Exception ignored){}
            return null;
        }
    }
}
