package com.hokyung.android_project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends Thread {
    public static void main(String[] args) {
        String strServerIP = "127.0.0.1";

        int nServerPort = 8888;

        int nDelayTime = 10000;

        InetSocketAddress isaSvr = null;
        Socket socket = null;

        try {
            socket = new Socket();
            System.out.println("서버와 연결을 시도합니다.");
            isaSvr = new InetSocketAddress(strServerIP, nServerPort);
            socket.connect(isaSvr, nDelayTime);
            System.out.printf("연결이 성공 되었습니다.[%s] [%d]\n", strServerIP, nServerPort);
        } catch (Exception e) {

        }
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e1) {
                System.out.println("예외 발생 : " + e1.getLocalizedMessage());
            }
        }


    }
}

