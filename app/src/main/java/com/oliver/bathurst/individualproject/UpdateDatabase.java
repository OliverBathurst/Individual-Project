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

    /**
     * UpdateDatabase takes a location object (and a context for checking prefs)
     */
    UpdateDatabase(Location location, Context context){
        this.loc = location;
        this.c = context;
    }

    /**
     * Update checks user preferences before starting update background task
     */
    void update(){
        if(loc != null) {//if location is not invalid (reduces network traffic)
            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(c);

            String user = shared.getString("registered_user", null);//get saved account user+pass
            String pass = shared.getString("registered_pass", null);

            if(user != null && pass != null) {//if user and pass are valid
                if (shared.getBoolean("only_wifi", false)) { //only update over wifi
                    ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm != null) {
                        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (wifiNetwork != null && wifiNetwork.isConnected()) {
                            new SendLocationToDB(user, pass).execute();//if wifi is enabled and connected
                        }
                    }
                }else{
                    new SendLocationToDB(user, pass).execute();//otherwise just execute
                }
            }
        }
    }

    /**
     * This method gets the update URL from hosted text file and posts required parameters in URL
     */
    @SuppressLint("StaticFieldLeak")
    public class SendLocationToDB extends AsyncTask<Void, Void, Void> {
        private final String username, password;

        SendLocationToDB(String user, String pass){//initialise with user+pass
            this.username = user;
            this.password = pass;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream((new URL(c.getString(R.string.update_redirect)).openConnection()).getInputStream())));
                String inputLine;//read current update URL from hosted text file
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);//read output
                }
                br.close();
                //append parameters to update URL and resolve
                String fullURL = sb.toString().trim() + "user=" + username + "&pass=" + password + "&lat=" + loc.getLatitude() + "&lon=" + loc.getLongitude() + "&acc=" + loc.getAccuracy();
                BufferedReader read = new BufferedReader(new InputStreamReader(new BufferedInputStream((new URL(fullURL).openConnection()).getInputStream())));
                read.close();//finally close
            } catch (Exception ignored) {}
            return null;
        }
    }
}
