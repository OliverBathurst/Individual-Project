package com.oliver.bathurst.individualproject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

class Server{

    private ServerSocket httpServerSocket;

    void start(){
        HttpServerThread httpServerThread = new HttpServerThread();
        httpServerThread.start();
    }
    void stop(){
        if (httpServerSocket != null) {
            try {
                httpServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    String getIP() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + ":8888\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    private class HttpServerThread extends Thread {
        @Override
        public void run() {
            Socket socket;
            try {
                httpServerSocket = new ServerSocket(8888);

                while(true){
                    socket = httpServerSocket.accept();
                    HttpResponseThread httpResponseThread = new HttpResponseThread(socket, "Hello");
                    httpResponseThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class HttpResponseThread extends Thread {
        final Socket socket;
        final String h1;

        HttpResponseThread(Socket socket, String msg){
            this.socket = socket;
            h1 = msg;
        }

        @Override
        public void run() {
            PrintWriter os;
            try {
                os = new PrintWriter(socket.getOutputStream(), true);

                String response =
                        "<html><head></head>" +
                                "<body>" +
                                "<h1>" + h1 + "</h1>" +
                                "</body></html>";

                //put plaintext webpage here

                os.print("HTTP/1.0 200" + "\r\n");
                os.print("Content type: text/html" + "\r\n");
                os.print("Content length: " + response.length() + "\r\n");
                os.print("\r\n");
                os.print(response + "\r\n");
                os.flush();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}