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

    private String MY_ALERT_MESSAGE = "Je suis là !";
    private String MY_BACKUP_NUMBER = null;

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
                if(message.equalsIgnoreCase("geoloc")) {
                    MY_BACKUP_NUMBER = phone;
                    sendSMSMessage();
                    inst.getMessageView().setText(MY_ALERT_MESSAGE);
                    for (int sec = 0; i < 3; i++) {
                        inst.getVibrator().vibrate(3000);
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

    public void sendSMSMessage() {
        MainActivity inst = MainActivity.instance();
        Location loc = inst.getLocation();
        Log.d("SMSListener", "Current Location : LAT="+loc.getLatitude()+"/LON="+loc.getLongitude());
        String sms = "[GEOLOC]POSITION\n" +
                "Votre téléphone se trouve aux coordonnés GPS suivantes:\n"+
                "LAT="+loc.getLatitude()+"/LON="+loc.getLongitude();
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(MY_BACKUP_NUMBER, null, sms, null, null);
            Toast.makeText(inst, "Coordinates Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(inst,
                    "SMS failed, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
