package com.oliver.bathurst.individualproject;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Oliver on 02/12/2017.
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class GCMRelay {
    private final String deviceToken;
    private final String messageContent;

    GCMRelay(String devToken, String message){
        this.deviceToken = devToken;
        this.messageContent = message;
    }
    void send(){
        new send().execute(new String[]{deviceToken, messageContent});
    }

    static class send extends AsyncTask<String[],Void,Void>{
        @Override
        protected Void doInBackground(String[]... strings) {
            try {
                String[] finalArr = strings[0];
                String authKey = "AIzaSyAEyxIa31egmv-SScysTc_lmoUZLRt9gIo";   // You FCM AUTH key
                String FMCurl = "https://fcm.googleapis.com/fcm/send";

                HttpURLConnection conn = (HttpURLConnection) new URL(FMCurl).openConnection();

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + authKey);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("to", finalArr[0].trim());
                JSONObject info = new JSONObject();
                info.put("message", finalArr[1]); // Notification body
                json.put("data", info);
                System.out.println(json.toString());

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(json.toString());
                wr.flush();
                conn.getInputStream();
            }catch(Exception ignored){}
            return null;
        }
    }
}
