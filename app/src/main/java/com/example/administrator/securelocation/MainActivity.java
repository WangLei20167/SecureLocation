package com.example.administrator.securelocation;

import android.location.Location;
import android.os.Handler;
import android.os.Message;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import connect.TCPClient;
import connect.TCPServer;
import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;
import msg.MsgValue;
import utils.PermissionUtils;
import appData.GlobalVar;

public class MainActivity extends AppCompatActivity {
    private TextView tv_sample_text;

    private TCPServer mTCPServer;
    private TCPClient mTCPClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv_sample_text = (TextView) findViewById(R.id.sample_text);

        //开启定位
        startLocation();
        //
        mTCPServer = new TCPServer(handler);
        mTCPClient = new TCPClient(handler);
    }

    public void onStartTCPServer(View view) {
        mTCPServer.startTCPServer();
    }

    public void onConnectTCPServer(View view) {
        mTCPClient.connectServer("172.25.214.2");
    }

    //定时获取定位的经纬度
    public void startLocation() {
        String permissionName = "android.permission.ACCESS_FINE_LOCATION";
        if (PermissionUtils.checkPermission(this, permissionName)) {
            TrackerSettings settings =
                    new TrackerSettings()
                            .setUseGPS(true)
                            .setUseNetwork(true)
                            .setUsePassive(true)
                            .setTimeBetweenUpdates(3 * 1000)
                            .setMetersBetweenUpdates(10);
            LocationTracker tracker = new LocationTracker(this, settings) {
                @Override
                public void onLocationFound(Location location) {
                    // Do some stuff when a new location has been found.
                    //获取到定位经纬度
                    tv_sample_text.setText(location.getLatitude() + "  " + location.getLongitude());
                    GlobalVar.LonAndLat = location.getLongitude() + "," + location.getLatitude();
                }

                @Override
                public void onTimeout() {

                }
            };
            tracker.startListening();
        } else {
            Toast.makeText(this, "定位权限不可用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理各个类发来的UI请求消息
     */
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MsgValue.TELL_ME_SOME_INFOR:
                    String infor = msg.obj.toString();
                    Toast.makeText(MainActivity.this, infor, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


}
