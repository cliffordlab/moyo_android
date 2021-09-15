package com.cliffordlab.amoss.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cliffordlab.amoss.settings.SettingsUtil;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ChristopherWainwrightAaron on 7/21/17.
 */

public class TimezoneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SettingsUtil util = new SettingsUtil(context);
        String oldTimezone = util.timezone();
        String newTimezone = TimeZone.getDefault().getID();

        long now = System.currentTimeMillis();

        if (oldTimezone == null || TimeZone.getTimeZone(oldTimezone).getOffset(now) != TimeZone.getTimeZone(newTimezone).getOffset(now)) {
            util.setNewTimezone(newTimezone);

            StringBuilder builder = new StringBuilder();
            builder.append(now);
            builder.append(",");
            builder.append(newTimezone);
            builder.append(System.getProperty("line.separator"));

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

            String fileExtension = ".tz";
            String filename = partialMonTimestamp + fileExtension;

            File file = new File(dir + "/" + filename);
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
}
