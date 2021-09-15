package com.cliffordlab.amoss.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by ChristopherWainwrightAaron on 5/31/16.
 */
public class SmsReceiver extends BroadcastReceiver {

    SmsMessage smsMessage;
    String message;
    String address;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("sms received", "new message");
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) { //KITKAT
            SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            smsMessage = msgs[0];
            message = smsMessage.getMessageBody();
            address = smsMessage.getOriginatingAddress();
        } else if(Build.VERSION.SDK_INT >= 23) {
            String format = intent.getStringExtra("format");
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0], format);
            message = smsMessage.getMessageBody();
            address = smsMessage.getOriginatingAddress();
        } else {
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
            message = smsMessage.getMessageBody();
            address = smsMessage.getOriginatingAddress();
        }

    }
}
