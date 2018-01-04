package com.oliver.bathurst.individualproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Oliver on 10/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class UpdateDatabase {
    private final Location loc;
    private final Context c;

    UpdateDatabase(Location location, Context context){
        this.loc = location;
        this.c = context;
    }

    void update(){
        if(loc != null) {
            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(c);

            String user = shared.getString("registered_user", null);
            String pass = shared.getString("registered_pass", null);

            if(user != null && pass != null) {
                if (shared.getBoolean("only_wifi", false)) { //only update over wifi
                    ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm != null) {
                        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (wifiNetwork != null && wifiNetwork.isConnected()) {
                            new SendLocationToDB(user, pass).execute();
                        }
                    }
                } else {
                    new SendLocationToDB(user, pass).execute();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class SendLocationToDB extends AsyncTask<Void, Void, Void> {
        private final String username, password;

        SendLocationToDB(String user, String pass){
            this.username = user;
            this.password = pass;
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

                String fullURL = sb.toString().trim() + "user=" + username + "&pass=" + password + "&lat=" + loc.getLatitude() + "&lon=" + loc.getLongitude() + "&acc=" + loc.getAccuracy();
                System.out.println(fullURL);
                BufferedReader read = new BufferedReader(new InputStreamReader(new BufferedInputStream((new URL(fullURL).openConnection()).getInputStream())));
                read.close();
            } catch (Exception ignored) {}
            return null;
        }
    }
}
