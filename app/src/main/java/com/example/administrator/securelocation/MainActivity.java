package com.example.administrator.securelocation;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;
import utils.PermissionUtils;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private TextView tv_sample_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv_sample_text = (TextView) findViewById(R.id.sample_text);
        tv_sample_text.setText(stringFromJNI());

        //开启定位
        startLocation();
    }

    public void startLocation(){
        String permissionName="android.permission.ACCESS_FINE_LOCATION";
        if(PermissionUtils.checkPermission(this,permissionName)){
            TrackerSettings settings =
                    new TrackerSettings()
                            .setUseGPS(true)
                            .setUseNetwork(true)
                            .setUsePassive(true)
                            .setTimeBetweenUpdates(3* 1000)
                            .setMetersBetweenUpdates(10);
            LocationTracker tracker = new LocationTracker(this, settings) {
                @Override
                public void onLocationFound(Location location) {
                    // Do some stuff when a new location has been found.
                    //获取到定位经纬度
                    tv_sample_text.setText(location.getLatitude()+"  "+location.getLongitude());
                }

                @Override
                public void onTimeout() {

                }
            };
            tracker.startListening();
        }else {
            Toast.makeText(this, "定位权限不可用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
