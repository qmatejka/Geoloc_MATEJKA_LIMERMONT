package com.ece.qmatejka.geoloc.listener;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ece.qmatejka.geoloc.MainActivity;

/**
 * Created by qmatejka on 22/11/2017.
 */

public class SMSListener extends BroadcastReceiver {

    private String MY_ALERT_MESSAGE = "This phone is wanted by his owner.";

    public SMSListener() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        String format = intentExtras.getString("format");
        if (intentExtras != null) {
            /* Get Messages */
            Object[] sms = (Object[]) intentExtras.get("pdus");
            String phone = "", message = "";
            for (int i = 0; i < sms.length; ++i) {
                /* Parse Each Message */
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);
                phone = smsMessage.getOriginatingAddress();
                message = smsMessage.getMessageBody();

                Log.d("SMSListener", "SMS Received : "+message );

                MainActivity inst = MainActivity.instance();
                inst.getMessageView().setText(phone+":"+message);

                /*Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer player = MediaPlayer.create(activity, notification);
                player.setLooping(true);
                player.start();
                */
                if(message.contains("[GEOLOC]")){
                    if(message.contains("help") || message.contains("HELP") || message.contains("Help")) {
                        Location loc = inst.getLocation();
                        Log.d("SMSListener", "Current Location : LAT=" + loc.getLatitude() + "/LON=" + loc.getLongitude());
                        String msg = "[GEOLOC]POSITION\n" +
                                "LAT=" + loc.getLatitude() + "/LON=" + loc.getLongitude();

                        inst.sendSMSMessage(phone, msg);
                        inst.getMessageView().setText(MY_ALERT_MESSAGE);
                        inst.setLostMode(true);
                    }

                    if(message.contains("POSITION")){
                        double latlon[] = new double[2];
                        String coord[] = message.split("/");
                        for(int j=0;j<coord.length;j++){
                            latlon[j] = Double.valueOf(coord[j].split("=")[1]);
                        }
                        Log.d("SMSListener", "LAT:"+latlon[0]+"/LON:"+latlon[1]);
                        inst.displayPhoneLocation(latlon[0], latlon[1]);
                    }
                }
            }
            //Toast.makeText(context, phone + ": " + message, Toast.LENGTH_SHORT).show();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("SMS_RECEIVED_ACTION");
            broadcastIntent.putExtra("message", phone + ": " + message);
            context.sendBroadcast(broadcastIntent);
        }
    }

}
