package com.oliver.bathurst.individualproject;

import android.os.AsyncTask;
import android.os.Looper;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Oliver on 02/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class GCMRelay extends AsyncTask<String[],Void,Void>{

    @Override
    protected Void doInBackground(String[]... strings) {
        Looper.prepare();
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream((new URL("https://oliverbathurst.github.io/web_api.txt").openConnection()).getInputStream())));
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            br.close();

            String[] finalArr = strings[0];

            HttpURLConnection conn = (HttpURLConnection) new URL("https://fcm.googleapis.com/fcm/send").openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "key=" + sb.toString().trim());
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject json = new JSONObject();
            json.put("to", finalArr[0]);
            JSONObject info = new JSONObject();
            info.put("message", finalArr[1]); // Notification body
            json.put("data", info);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();
            conn.getInputStream();
            conn.disconnect();
        }catch(Exception ignored){}
        Looper.loop();
        return null;
    }
}
