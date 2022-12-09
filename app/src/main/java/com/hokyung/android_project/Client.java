package com.hokyung.android_project;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Client extends Thread {

        public static final int SOCKET_TIMEOUT = 10000;
        public static final int TCP_MSG_TYPE_HEARTBEAT = 100;
        public static final int HEARTBEAT_TIMER_PERIOD = 5000;

        private Socket socket;
        private OutputStream socketOutput;
        private BufferedReader socketInput;
        private Timer timer;
        private String ip;
        private int port;
        private ClientCallback listener=null;

        public Client(String ip, int port){
            this.ip= ip;
            this.port= port;
        }

        public void startHeartBeatTimer() {
            timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    if(socket != null && socket.isConnected()) {
                        JSONObject jsonObject  = new JSONObject();
                        try {
                            jsonObject.put("msg_type",TCP_MSG_TYPE_HEARTBEAT);
                            send(jsonObject.toString());
                            send("HEllo");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }, 0, HEARTBEAT_TIMER_PERIOD);
        }

        public void stopHeartBeatTimer(){
            if(timer != null){
                timer.purge();
                timer.cancel();
                timer = null;
            }
        }


        public void connect(){
            Log.d("Start","Connection");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket();
                        InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
                        socket.connect(socketAddress,SOCKET_TIMEOUT);
                        socketOutput = socket.getOutputStream();
                        socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        new ReceiveThread().start();
                        if(listener!=null)
                            listener.onConnect(Client.this);
                    } catch (IOException e) {
                        if(listener!=null)
                            listener.onConnectError(Client.this, e.getMessage());
                    } catch (Exception e){
                        Log.e("createSocketError",e.getMessage());
                    }
                }
            }).start();
        }


        public void disconnect(){
            try {
                socket.close();
            } catch (IOException e) {
                if(listener!=null)
                    listener.onDisconnect(Client.this, e.getMessage());
            }
        }

        public void send(String message){
            try {
                socketOutput.write(message.getBytes());
                socketOutput.flush();
            } catch (IOException e) {
                if(listener!=null)
                    listener.onDisconnect(Client.this, e.getMessage());
            }
        }

        public void send(byte[] message){
            try {
                socketOutput.write(message);
            } catch (IOException e) {
                if(listener!=null)
                    listener.onDisconnect(Client.this, e.getMessage());
            }
        }

        private class ReceiveThread extends Thread implements Runnable {
            public void run(){
                String message = null;
                int charsRead;
                char[] buffer = new char[1024];

                try {

                    while ((charsRead = socketInput.read(buffer)) != -1) {
                        message = new String(buffer, 0, charsRead);
                        if (listener != null)
                            listener.onMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                }

            }
        }

        public void setClientCallback(ClientCallback listener){
            this.listener=listener;
        }

        public void removeClientCallback(){
            this.listener=null;
        }

        public interface ClientCallback {
            void onMessage(String message);
            void onConnect(Client socket);
            void onDisconnect(Client socket, String message);
            void onConnectError(Client socket, String message);
        }
    }






