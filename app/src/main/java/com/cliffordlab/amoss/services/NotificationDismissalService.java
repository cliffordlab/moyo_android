package com.cliffordlab.amoss.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSAL;

/**
 * Created by ChristopherWainwrightAaron on 5/11/17.
 */

public class NotificationDismissalService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("intent has been dismissed");
        if (intent != null) {
            String notifDismissed = intent.getStringExtra(DISMISSAL);


            File dir = new File(getFilesDir() + "/amoss");
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

            String fileExtension = ".dismiss";
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

            StringBuilder sb = new StringBuilder();
            sb.append(System.currentTimeMillis());
            sb.append(",");
            sb.append(notifDismissed);
            sb.append(System.getProperty("line.separator"));

            try {
                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(sb.toString());
                bw.close();
            } catch (IOException ioe) {
                FirebaseCrashlytics.getInstance().recordException(ioe);
                System.out.println(ioe.getMessage());
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Dismissal service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
