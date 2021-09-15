package com.cliffordlab.amoss.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;

import com.cliffordlab.amoss.R;
import com.cliffordlab.amoss.gui.surveys.PHQNineActivity;
import com.cliffordlab.amoss.services.NotificationDismissalService;
import com.cliffordlab.amoss.settings.SettingsUtil;

import java.util.Calendar;
import java.util.TimeZone;

import static com.cliffordlab.amoss.helper.Constants.PHQ9_DAILY_NOTIFICATION_ID;
import static com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSAL;
import static com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSED_PHQ9;


/**
 * Created by ChristopherWainwrightAaron on 2/6/17.
 */

public class DailyPHQNineReceiver extends BroadcastReceiver {
    private AlarmManager am;
    private PendingIntent pendingIntent;
//    private PHQNineReceiver mWeeklyPHQNineReceiver = new PHQNineReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {

        /* if phq9 survey was not completed send notification again
        or cancel if the survey was completed
         */
        if (!SettingsUtil.isPHQ9Completed(context)) {
            int requestID = 99;
            int deleteRequestId = (int) System.currentTimeMillis();

            Intent result = new Intent(context, PHQNineActivity.class);
            result.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            Intent deleteResult = new Intent(context, NotificationDismissalService.class);
            deleteResult.putExtra(DISMISSAL, DISMISSED_PHQ9);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, requestID, result, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent deleteIntent = PendingIntent.getService(context, deleteRequestId, deleteResult, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_phq9_channel")
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.checklist)
                    .setContentTitle("MoYo")
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentText("Please complete the PHQ9 Survey :)")
                    .setDeleteIntent(deleteIntent)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(PHQ9_DAILY_NOTIFICATION_ID, builder.build());
        } else {
            /* if survey completed then set phq9 check back to false
            and run weekly notification for phq9 the next week
             */
            SettingsUtil.setPHQ9Completed(context, false);
            cancel(context);
//            mWeeklyPHQNineReceiver.setRepeatedNotification(context, 0);
        }
    }

    public void setRepeatedNotification(Context context, int id, int hh, int mm, int ss) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarm_intent = new Intent(context, DailyPHQNineReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, id, alarm_intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, hh);
        calendar.set(Calendar.MINUTE, mm);
        calendar.set(Calendar.SECOND, ss);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR * 2, pendingIntent);

        ComponentName receiver = new ComponentName(context, StartupReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancel(Context context) {
        if (am != null) {
            am.cancel(pendingIntent);
        }

        ComponentName receiver = new ComponentName(context, StartupReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
