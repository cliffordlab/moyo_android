package com.cliffordlab.amoss.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.cliffordlab.amoss.R;
import com.cliffordlab.amoss.gui.surveys.PHQNineActivity;
import com.cliffordlab.amoss.helper.Constants;
import com.cliffordlab.amoss.services.NotificationDismissalService;

import java.util.Calendar;

import static com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSAL;
import static com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSED_KCCQ;
import static com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSED_PHQ9;

/**
 * Created by ChristopherWainwrightAaron on 2/2/17.
 */

public class PHQNineReceiver extends BroadcastReceiver {
    private AlarmManager am;
    private PendingIntent pendingIntent;
//    private DailyPHQNineReceiver mDailyPHQNineReceiver = new DailyPHQNineReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("PHQ9 has not been completed in over a week");
        int contentRequestID = (int) System.currentTimeMillis();
        int deleteRequestId = (int) System.currentTimeMillis();

        Intent contentResult = new Intent(context, PHQNineActivity.class);
        contentResult.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent deleteResult = new Intent(context, NotificationDismissalService.class);
        deleteResult.putExtra(DISMISSAL, DISMISSED_PHQ9);

        PendingIntent contentIntent = PendingIntent.getActivity(context, contentRequestID, contentResult, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent deleteIntent = PendingIntent.getService(context, deleteRequestId, deleteResult, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.checklist)
                .setContentTitle("AMoSS HF")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("Time to take your weekly PHQ9!")
                .setDeleteIntent(deleteIntent)
                .setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.PHQ9_NOTIFICATION_ID, builder.build());

        /**
         * @Param context  -> to access notifcations
         * @Param request code -> code specific broadcast
         * @Param hour -> hour in day military time
         * @Param minute -> minute in hour
         * @Param second -> second in minute
         **/
        /* set notification to run 2 hours from this point */
//        mDailyPHQNineReceiver.setRepeatedNotification(context,3, 13, 0, 0);
    }

    public void onReceivePHQ9(Context context) {
        int requestID = (int) System.currentTimeMillis();
        int deleteRequestID = (int) System.currentTimeMillis();
        System.out.println("Notification phq9 function ran");

        Intent result = new Intent(context, PHQNineActivity.class);
        result.putExtra(Constants.NOTIFICATION_FLAG, Constants.PHQ9_NOTIFICATION_ID);
        result.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent deleteResult = new Intent(context, NotificationDismissalService.class);
        deleteResult.putExtra(DISMISSAL, DISMISSED_KCCQ);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestID, result, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent deleteIntent = PendingIntent.getService(context, deleteRequestID, deleteResult, PendingIntent.FLAG_UPDATE_CURRENT);

        String CHANNEL_ID = "amoss_heart_channel_phq9";
        Notification notification = getNotification(context,deleteIntent, pendingIntent, CHANNEL_ID);

        NotificationManager notificationManager = getNotificationManager(context, CHANNEL_ID);
        notificationManager.notify(Constants.PHQ9_NOTIFICATION_ID, notification);
    }

    private NotificationManager getNotificationManager(Context context, String channelID) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            NotificationChannel channel = new NotificationChannel(channelID, "channel_1", NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                System.out.println("android O notification created for PHQNINE");
                notificationManager.createNotificationChannel(channel);
            }
        }
        return notificationManager;
    }

    private Notification getNotification(Context context, PendingIntent deleteIntent, PendingIntent pendingIntent, String channelID) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID);
        return builder.setContentTitle("Healthy Aging Study")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.checklist)
                .setContentTitle("AMoSS Heart")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("Time to take your weekly PHQ9!")
                .setDeleteIntent(deleteIntent)
                .setContentIntent(pendingIntent).build();
    }

    public void setRepeatedNotification(Context context, int id) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarm_intent = new Intent(context, PHQNineReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, id, alarm_intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        /*  wednesday at 10am */
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);

        ComponentName receiver = new ComponentName(context, StartupReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

}
