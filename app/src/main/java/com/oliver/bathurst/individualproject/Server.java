package com.oliver.bathurst.individualproject;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.text.format.Formatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Oliver on 03/10/2017.
 * All Rights Reserved
 * Unauthorized copying of this file via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Oliver Bathurst <oliverbathurst12345@gmail.com>
 */

class Server{
    private ServerSocket httpServerSocket;
    private final Context c;
    static boolean running = false;

    Server(Context context){
        c = context;
    }

    void start(){
        new HttpServerThread().start();
    }
    void stop(){
        if (httpServerSocket != null) {
            try {
                httpServerSocket.close();
                running = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressWarnings("deprecation")
    String getIP() {
        WifiManager wifiMan = (WifiManager) c.getApplicationContext().getSystemService(WIFI_SERVICE);
        return (wifiMan != null && wifiMan.getConnectionInfo() != null) ? Formatter.formatIpAddress(wifiMan.getConnectionInfo().getIpAddress()) : Formatter.formatIpAddress(0);
    }
    int getPort(){
        return httpServerSocket != null ? httpServerSocket.getLocalPort() : 0;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private class HttpServerThread extends Thread {
        @Override
        public void run() {
            try {
                httpServerSocket = new ServerSocket(8888);
                while(true){
                    new HttpResponseThread(httpServerSocket.accept()).start();
                    running = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class HttpResponseThread extends Thread {
        final Socket socket;

        HttpResponseThread(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            Looper.prepare();
            PrintWriter os;
            try {
                os = new PrintWriter(socket.getOutputStream(), true);
                os.print("HTTP/1.0 200" + "\r\n");
                os.print("Content type: text/html" + "\r\n");
                os.print("Content length: " + webContent().length() + "\r\n");
                os.print("\r\n");
                os.print(webContent() + "\r\n");
                os.flush();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private String webContent(){
            Location loc = new LocationService(c).getLoc();
            PermissionsManager perm = new PermissionsManager(c);
            return
                    "<html>\n" +
                            "  <head>\n" +
                            "    <style>\n" +
                            "      #map {\n" +
                            "        width: 100%;\n" +
                            "        height: 800px;\n" +
                            "      }\n" +
                            "    </style>\n" +
                            "  </head>\n" +
                            "  <body>\n" +
                            "    <h3>Your Device Location</h3>\n" +
                            "    <div id=\"map\"></div>\n" +
                            "\t<script>\n" +
                            "      function initMap() {\n" +
                            "        var loc = {lat: " + loc.getLatitude() + "," + " lng: " + loc.getLongitude() + "};\n" +
                            "        var map = new google.maps.Map(document.getElementById('map'), {\n" +
                            "          zoom: 30,\n" +
                            "          center: loc\n" +
                            "        });\n" +
                            "        var mapMark = new google.maps.Marker({\n" +
                            "          position: loc,\n" +
                            "          map: map\n" +
                            "        });\n" +
                            "      }\n" +
                            "    </script>\n" +
                            "\t<script async defer\n" +
                            "    src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyDh84qms-mgdSRGTOUckwDdITPey7X3O18&callback=initMap\">\n" +
                            "    </script>\n" +
                            "   <h3>Your Device Information</h3>\n" +
                            "   <p>" + perm.getDeviceAttributes() + "</p>\n" +
                            "   <p>" + perm.getAndroidVersion() + "</p>\n" +
                            "   <p>" + perm.getCellInfo() + "</p>\n" +
                            "  </body>\n" +
                            "</html>";
        }
    }
}