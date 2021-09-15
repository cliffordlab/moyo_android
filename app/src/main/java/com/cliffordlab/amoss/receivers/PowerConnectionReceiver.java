package com.cliffordlab.amoss.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ChristopherWainwrightAaron on 9/27/17.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Android power connected or disconnected");

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

        File dir = new File(context.getFilesDir() + "/amoss");
        if (!dir.exists()) {
            System.out.println("creating directory: " + dir.getName());
            try {
                dir.mkdir();
            } catch(SecurityException se){
                FirebaseCrashlytics.getInstance().recordException(se);
                se.printStackTrace();
            }
        }

        Calendar c = Calendar.getInstance();

        c.setTime(new Date());
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.setTimeZone(TimeZone.getTimeZone("UTC"));

        long monday = c.getTimeInMillis();
        String mondayUnixTimeStamp = String.valueOf(monday);
        String partialMonTimestamp = mondayUnixTimeStamp.substring(1);

        String fileExtension = ".charge";
        String filename = partialMonTimestamp + fileExtension;

        File file = new File(dir + "/" + filename);

        StringBuilder builder = new StringBuilder();
        builder.append(System.currentTimeMillis());
        builder.append(",");
        builder.append(isCharging);
        builder.append(System.getProperty("line.separator"));

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                FirebaseCrashlytics.getInstance().recordException(ioe);
                System.out.println(ioe.getMessage());
            }
        }

        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(builder.toString());
            bw.close();
        } catch (IOException ioe) {
            FirebaseCrashlytics.getInstance().recordException(ioe);
            System.out.println(ioe.getMessage());
        }
    }
}
