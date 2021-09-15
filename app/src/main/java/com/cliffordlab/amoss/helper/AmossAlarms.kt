package com.cliffordlab.amoss.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cliffordlab.amoss.receivers.NotificationConstants
import com.cliffordlab.amoss.settings.SettingsUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ChristopherWainwrightAaron on 3/14/112.
 */
class AmossAlarms {
    companion object {
        private const val TAG = "AmossAlarms"
    }
    
    fun weeklyAlarm(context: Context) {
        val user = SettingsUtil(context)
        val intent = Intent("weekly-broadcast")
        val pendingIntent = PendingIntent.getBroadcast(context, NotificationConstants.REQUEST_CODE_WEEKLY, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val currentTime = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 11) //14
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val nextWednesdayEvening = Calendar.getInstance()
        nextWednesdayEvening.timeInMillis = calendar.timeInMillis
        nextWednesdayEvening.set(Calendar.HOUR_OF_DAY, 17)

        val afterAlarmTime = currentTime > calendar.timeInMillis
        val beforeNightTime = currentTime < nextWednesdayEvening.timeInMillis
        val beforeAlarmTime = currentTime < calendar.timeInMillis
        val hasCompletedBothSurveys = user.hasCompletedKCCQ() || user.hasCompletedPHQ9()

        Log.i(TAG, "weeklyAlarm: Setting weekly alarm...")
        if (beforeAlarmTime && !hasCompletedBothSurveys) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, (AlarmManager.INTERVAL_HOUR * 1.5).toLong(), pendingIntent)
            Log.i(TAG, "weeklyAlarm: A survey is not completed and current time is before alarm time.")
            Log.i(TAG, "weeklyAlarm: Setting morning alarm at: " + calendar.time)
        } else if (afterAlarmTime && beforeNightTime && !hasCompletedBothSurveys) {
            val currentMillis = System.currentTimeMillis()
            val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm")
            val resultDate = Date(currentMillis)
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (AlarmManager.INTERVAL_HOUR * 1.5).toLong(), pendingIntent)
//            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 2*60*1000, pendingIntent)
            Log.i(TAG, "weeklyAlarm: A survey is not completed and current time is after alarm time and before the next alarm time.")
            Log.i(TAG, "weeklyAlarm: Setting alarm immediately at: " + sdf.format(resultDate))
        } else if (hasCompletedBothSurveys || (currentTime > nextWednesdayEvening.timeInMillis)) {
            //prevents it from going off if survey is already completed
            //set for next thursday if after thursday in current week
            val nextWeek = Calendar.getInstance()
            nextWeek.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY) //thursday
            nextWeek.set(Calendar.HOUR_OF_DAY, 10)
            nextWeek.set(Calendar.MINUTE, 0)
            nextWeek.add(Calendar.DATE, 7)
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextWeek.timeInMillis, (AlarmManager.INTERVAL_HOUR * 1.5).toLong(), pendingIntent)
            Log.i(TAG, "weeklyAlarm: All weekly surveys completed.")
            Log.i(TAG, "weeklyAlarm: Setting alarm next week at: " + nextWeek.time)
            println("wednesdaEvening time: " + nextWednesdayEvening.time)
        } else {
            Log.i(TAG, "weeklyAlarm: NO ALARMS SET")
        }
    }

    fun dailyAlarms(context: Context) {
        Log.i(TAG, "dailyAlarms: Setting daily alarms..")
        val util = SettingsUtil(context)
        val intent = Intent("daily-broadcast")
        val pendingIntent = PendingIntent.getBroadcast(context, NotificationConstants.REQUEST_CODE_MOODZOOM, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)
        val currentTime = System.currentTimeMillis()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 11) //14
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val calendarNightTime = Calendar.getInstance()
        calendarNightTime.timeInMillis = calendar.timeInMillis
        calendarNightTime.set(Calendar.HOUR_OF_DAY, 17)

        val hasCompletedBothSurveys = util.hasCompletedZoom() || util.hasCompletedSwipe()
        val afterNightTime = currentTime > calendarNightTime.timeInMillis
        val withinWindowForAlarm = currentTime > calendar.timeInMillis && currentTime < calendarNightTime.timeInMillis

        val beforeAlarmTime = currentTime < calendar.timeInMillis
        if (beforeAlarmTime && !hasCompletedBothSurveys) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, (AlarmManager.INTERVAL_HOUR * 1.5).toLong(), pendingIntent)
//            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, 2 * 60 * 1000, pendingIntent)
            Log.i(TAG, "dailyAlarms: One or more surveys are not completed and it is before the alarm time.")
            Log.i(TAG, "dailyAlarms: Setting morning alarm: " + calendar.time)
        } else if (withinWindowForAlarm && !hasCompletedBothSurveys) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (AlarmManager.INTERVAL_HOUR * 1.5).toLong(), pendingIntent)
            Log.i(TAG, "dailyAlarms: One or more surveys are not completed and it is before night time..")
            Log.i(TAG, "dailyAlarms: setting alarm immediately.. ")
        } else if (hasCompletedBothSurveys || afterNightTime) {
            val nextDay = Calendar.getInstance()
            nextDay.add(Calendar.DAY_OF_MONTH, 1)
            nextDay.set(Calendar.HOUR_OF_DAY, 10)
//            am.setRepeating(AlarmManager.RTC_WAKEUP, nextDay.timeInMillis, 2 * 60 * 1000, pendingIntent)
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextDay.timeInMillis, (AlarmManager.INTERVAL_HOUR * 1.5).toLong(), pendingIntent)
            if (hasCompletedBothSurveys) {
                Log.i(TAG, "dailyAlarms: Both surveys completed.")
            } else {
                Log.i(TAG, "dailyAlarms: After night time.")
            }
            Log.i(TAG, "dailyAlarms: Setting alarm for next day: " + nextDay.time)
        } else {
            Log.i(TAG, "dailyAlarms: NO ALARMS SET")
        }

    }
}