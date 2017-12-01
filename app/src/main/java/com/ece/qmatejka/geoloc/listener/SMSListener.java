package com.ece.qmatejka.geoloc.listener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.ece.qmatejka.geoloc.MainActivity;

/**
 * Created by qmatejka on 22/11/2017.
 */

public class SMSListener extends BroadcastReceiver {

    private MainActivity activity;
    private String MY_ALERT_MESSAGE = "Je suis là !";

    public SMSListener(Context context){
        activity = (MainActivity)context;
    }

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
                message = smsMessage.getMessageBody().toString();

                Log.d("SMSListener", message + "en int:" + Integer.getInteger(message));

                activity.getMessageView().setText(message);
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer player = MediaPlayer.create(activity, notification);
                player.setLooping(true);
                player.start();
                if(message.equalsIgnoreCase("geoloc")) {
                    activity.getMessageView().setText(MY_ALERT_MESSAGE);
                    for (int sec = 0; i < 5; i++) {
                        //try {
                            activity.getVibrator().vibrate(3000);

                            /*Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
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
