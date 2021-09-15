package com.cliffordlab.amoss.datacollector

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.cliffordlab.amoss.gui.MainActivity
import com.cliffordlab.amoss.settings.SettingsUtil


class NotificationCreator {

    object NotificationObject {
        const val notificationId = 1094
        private var notification: Notification? = null

        @RequiresApi(Build.VERSION_CODES.O)
        fun getNotification(context: Context): Notification? {
            val NOTIFICATION_CHANNEL_ID = "AMOSS Foreground ID"
            val channelName = "Amoss Foreground Service"
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val intent = Intent(context, MainActivity::class.java)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)

            val pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0)
            val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(com.cliffordlab.amoss.R.drawable.moyo_logo)
                .setContentTitle("App is running in background: ${getDataCollected(context)}")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build()
            return notification
        }

        private fun getDataCollected(context: Context): String {
            val string = StringBuilder()
            val settingsUtil = SettingsUtil(context)

            when {
                settingsUtil.isAccCollectedEnabled -> {
                    string.append("Accel, ")
                }
                settingsUtil.isCallDataCollectionEnabled -> {
                    string.append("Call, ")

                }
                settingsUtil.isLIWCDataCollectionEnabled -> {
                    string.append("SMS, ")

                }
                settingsUtil.isLocCollectionEnabled -> {
                    string.append("Environment, ")

                }
            }

            return string.toString().substring(0, string.length -2)
        }

    }
}