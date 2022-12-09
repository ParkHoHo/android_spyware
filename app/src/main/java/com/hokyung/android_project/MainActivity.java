package com.hokyung.android_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setBtn();
        startClient();
    }

    // 버튼 기능 추가
    public void setBtn() {
        Button bt_start = (Button) findViewById(R.id.bt_start);
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        Button bt_stop = (Button) findViewById(R.id.bt_stop);
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
        });

//        Button attack = (Button) findViewById(R.id.textView);
//        attack.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                setContentView(R.layout.galary);
//            }
//        });

    }



    // 권한 부여하기
    // 마시멜로우 버전 이상일 경우에는 보안 문제로 권한을 무조건 허용받아야함.

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                startService(new Intent(MainActivity.this, MyService.class));
            }
        } else {
            startService(new Intent(MainActivity.this, MyService.class));
        }
    }


    public static final String TCP_SERVER_IP = "127.0.0.1";
    public static final int TCP_SERVER_PORT = 7777;
    public void startClient() {


        Client client = new Client(TCP_SERVER_IP,TCP_SERVER_PORT);
        client.setClientCallback(new Client.ClientCallback() {
            @Override
            public void onMessage(String message) {
                try {
                    JSONObject msg  = new JSONObject(new String(message));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnect(Client socket) {
                socket.startHeartBeatTimer();
            }

            @Override
            public void onDisconnect(Client socket, String message) {
                socket.stopHeartBeatTimer();
                client.connect();
            }

            @Override
            public void onConnectError(Client socket, String message) {
                socket.stopHeartBeatTimer();
                client.connect();
            }
        });

        client.connect();

    }
}