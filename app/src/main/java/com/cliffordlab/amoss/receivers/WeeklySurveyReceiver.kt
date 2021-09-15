package com.cliffordlab.amoss.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.surveys.KCCQActivity
import com.cliffordlab.amoss.gui.surveys.PHQNineActivity
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSAL
import com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSED_WEEKLY
import com.cliffordlab.amoss.services.NotificationDismissalService
import com.cliffordlab.amoss.settings.SettingsUtil

/**
 * Created by ChristopherWainwrightAaron on 1/10/18.
 */
class WeeklySurveyReceiver {
    private var result: Intent? = null
    //TODO: combine receiver notification creation classes based on different constructiors
    fun onReceiveWeeklySurvey(context: Context) {
        val requestID = 2
        val deleteRequestID = 2
        System.out.println("Notification weekly survey function ran")
        val settingsUtil = SettingsUtil(context)
        if (settingsUtil.hasCompletedKCCQ()) {
            result = Intent(context, PHQNineActivity::class.java)
        } else {
            result = Intent(context, KCCQActivity::class.java)
        }
        result!!.putExtra(Constants.NOTIFICATION_FLAG, Constants.WEEKLY_SURVEY_NOTIFICATION_ID)
        result!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val deleteResult = Intent(context, NotificationDismissalService::class.java)
        deleteResult.putExtra(DISMISSAL, DISMISSED_WEEKLY)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, requestID, result, PendingIntent.FLAG_UPDATE_CURRENT)
        val deleteIntent: PendingIntent = PendingIntent.getService(context, deleteRequestID, deleteResult, PendingIntent.FLAG_UPDATE_CURRENT)
        val CHANNEL_ID = "amoss_heart_channel_weekly"
        val notification: Notification = getNotification(context, deleteIntent, pendingIntent, CHANNEL_ID)
        val notificationManager: NotificationManager? = getNotificationManager(context, CHANNEL_ID)
        notificationManager?.notify(Constants.WEEKLY_SURVEY_NOTIFICATION_ID, notification)
    }

    private fun getNotificationManager(context: Context, channelID: String): NotificationManager? {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { /* Create or update. */
            val channel = NotificationChannel(channelID, "channel_1", NotificationManager.IMPORTANCE_DEFAULT)
            System.out.println("android O notification created for KCCQ")
            notificationManager.createNotificationChannel(channel)
        }
        return notificationManager
    }

    private fun getNotification(context: Context, deleteIntent: PendingIntent, pendingIntent: PendingIntent, channelID: String): Notification {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, channelID)
        return builder.setContentTitle("Healthy Aging Study")
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.checklist)
                .setContentTitle("AMoSS Heart")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("Time to take your weekly survey!")
                .setDeleteIntent(deleteIntent)
                .setContentIntent(pendingIntent).build()
    }
}