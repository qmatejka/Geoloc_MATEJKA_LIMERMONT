package com.ece.qmatejka.geoloc;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ece.qmatejka.geoloc.listener.GPSLocation;
import com.ece.qmatejka.geoloc.listener.SMSListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.lize.oledcomm.camera_lifisdk_android.ILiFiPosition;
import com.lize.oledcomm.camera_lifisdk_android.LiFiSdkManager;
import com.lize.oledcomm.camera_lifisdk_android.V1.LiFiCamera;

public class MainActivity extends AppCompatActivity {

    private static MainActivity inst;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 140;

    private TextView messageView;
    private TextView alertMsg;
    private Button button;
    private Button buttonOK;
    private EditText phoneNumber;

    private SupportMapFragment mapFragment;
    private BottomNavigationView navigation;
    private LiFiSdkManager liFiSdkManager;
    private Vibrator vibrator;
    private GPSLocation gps;
    private SMSListener smsListener;
    private MapsTracker tracker;

    public static MainActivity instance() {
        return inst;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mapFragment.getView().setVisibility(View.INVISIBLE);
                    button.setVisibility(View.VISIBLE);
                    messageView.setVisibility(View.VISIBLE);
                    phoneNumber.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    mapFragment.getView().setVisibility(View.VISIBLE);
                    button.setVisibility(View.INVISIBLE);
                    messageView.setVisibility(View.INVISIBLE);
                    phoneNumber.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_notifications:
                    mapFragment.getView().setVisibility(View.INVISIBLE);
                    button.setVisibility(View.INVISIBLE);
                    messageView.setVisibility(View.VISIBLE);
                    phoneNumber.setVisibility(View.INVISIBLE);
                    setLiFiOn();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertMsg = findViewById(R.id.alertMsg);
        alertMsg.setVisibility(View.INVISIBLE);
        messageView = findViewById(R.id.messageView);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        navigation = findViewById(R.id.navigation);
        phoneNumber = findViewById(R.id.phoneNumber);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("GEOLOC", "Button find my phone clicked.");
                String phoneNumber = ((EditText)findViewById(R.id.phoneNumber)).getText().toString();
                String msg = "[GEOLOC]HELP";
                sendSMSMessage(phoneNumber, msg);
            }
        });

        buttonOK = findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLostMode(false);
            }
        });
        buttonOK.setVisibility(View.INVISIBLE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 160);
        }

        smsListener = new SMSListener();
        registerReceiver(smsListener, new IntentFilter());
        gps = new GPSLocation(this);
        tracker = new MapsTracker();
        liFiSdkManager = new LiFiSdkManager(this, LiFiSdkManager.CAMERA_LIB_VERSION_0_1,
                "token", "user", new ILiFiPosition() {
            @Override
            public void onLiFiPositionUpdate(String lamp) {
                messageView.setText(lamp);
            }
        });

        messageView.setText("En attente d'un sms...");
        mapFragment.getMapAsync(tracker);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    @Override
    protected void onStart(){
        super.onStart();
        inst = this;
    }

    public void sendSMSMessage(String phoneNumber, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, msg, null, null);
            Toast.makeText(this, "SMS sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this,
                    "SMS failed, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setLiFiOn(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        liFiSdkManager.setLocationRequestMode(LiFiSdkManager.LOCATION_REQUEST_OFFLINE_MODE);
        liFiSdkManager.init(R.id.container, LiFiCamera.FRONT_CAMERA);
        liFiSdkManager.start();
        /*if (liFiSdkManager != null && liFiSdkManager.isStarted()) {
            liFiSdkManager.stop();
            liFiSdkManager.release();
            liFiSdkManager = null;
        }*/
    }

    public void displayPhoneLocation(double lat, double lon){
        navigation.setSelectedItemId(R.id.navigation_dashboard);
        tracker.searchLocation(lat, lon);
    }

    public void setLostMode(boolean lost){
        if(lost){
            mapFragment.getView().setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
            messageView.setVisibility(View.INVISIBLE);
            phoneNumber.setVisibility(View.INVISIBLE);
            alertMsg.setVisibility(View.VISIBLE);
            buttonOK.setVisibility(View.VISIBLE);
            vibrator. vibrate(5000);
        }else{
            buttonOK.setVisibility(View.INVISIBLE);
            alertMsg.setVisibility(View.INVISIBLE);
            navigation.setSelectedItemId(R.id.navigation_home);
        }
    }

    public void requestSpecificPermission(String permission){
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{permission}, 160);
        }
    }

    public TextView getMessageView() {
        return messageView;
    }

    public Location getLocation() { return gps.getLocation(); }
}
