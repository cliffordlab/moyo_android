package com.cliffordlab.amoss.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.surveys.MoodSwipeActivity
import com.cliffordlab.amoss.gui.surveys.MoodZoomActivity
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSAL
import com.cliffordlab.amoss.receivers.NotificationConstants.DISMISSED_MOOD_ZOOM
import com.cliffordlab.amoss.services.NotificationDismissalService
import com.cliffordlab.amoss.settings.SettingsUtil

/**
 * Created by michael on 2/2/16.
 */
class DailySurveyReceiver : BroadcastReceiver() {
    private var result: Intent? = null
    fun onReceiveDailySurvey(context: Context) {
        val requestID = 1
        val deleteRequestID = 1
        val settingsUtil = SettingsUtil(context)
        if (settingsUtil.hasCompletedSwipe()) {
            result = Intent(context, MoodZoomActivity::class.java)
        } else {
            result = Intent(context, MoodSwipeActivity::class.java)
        }
        result!!.putExtra(Constants.NOTIFICATION_FLAG, Constants.MOOD_ZOOM_NOTIFICATION_ID)
        result!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val deleteResult = Intent(context, NotificationDismissalService::class.java)
        deleteResult.putExtra(DISMISSAL, DISMISSED_MOOD_ZOOM)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, requestID, result, PendingIntent.FLAG_UPDATE_CURRENT)
        val deleteIntent: PendingIntent = PendingIntent.getService(context, deleteRequestID, deleteResult, PendingIntent.FLAG_UPDATE_CURRENT)
        val CHANNEL_ID = "amoss_heart_channel_mz"
        val notification: Notification = getNotification(context, deleteIntent, pendingIntent, CHANNEL_ID)
        val notificationManager: NotificationManager? = getNotificationManager(context, CHANNEL_ID)
        notificationManager!!.notify(Constants.MOOD_ZOOM_NOTIFICATION_ID, notification)
        //		resetTaskAvailabilityFollowingDay(context);
    }

    //	private void resetTaskAvailabilityFollowingDay(Context context) {
//		System.out.println("setting reset task availability from receiver");
//		BroadcastReceiver br = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				System.out.println("mood survey reset successful");
//				SettingsUtil util = new SettingsUtil(context);
//				util.setHasCompletedZoom(false);
//				AmossAlarms amossAlarms = new AmossAlarms();
//				amossAlarms.moodZoomAlarm(context);
//			}
//		};
//
//		context.registerReceiver(br, new IntentFilter("task-availability-mz"));
//
//		AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		PendingIntent pi = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), new Intent("task-availability-mz"), 0);
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.HOUR_OF_DAY, 15); //0
//		calendar.set(Calendar.MINUTE, 10);
//		calendar.set(Calendar.SECOND, 0);
////		calendar.add(Calendar.DAY_OF_MONTH, 1);
//		mgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 2*60*1000, pi);
//	}
    private fun getNotificationManager(context: Context, channelID: String): NotificationManager? {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { /* Create or update. */
            val channel = NotificationChannel(channelID, "channel_2", NotificationManager.IMPORTANCE_DEFAULT)
            System.out.println("android O notification created for Mood Surveys")
            notificationManager.createNotificationChannel(channel)
        }
        return notificationManager
    }

    private fun getNotification(context: Context, deleteIntent: PendingIntent, pendingIntent: PendingIntent, channelID: String): Notification {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, channelID)
        return builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.checklist)
                .setContentTitle("AMoSS Heart")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("Time to take your daily survey!")
                .setDeleteIntent(deleteIntent)
                .setContentIntent(pendingIntent).build()
    }

    override fun onReceive(context: Context?, intent: Intent?) {

    }
}