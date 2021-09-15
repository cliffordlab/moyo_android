package com.cliffordlab.amoss.datacollector

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.annotation.RequiresApi
import com.cliffordlab.amoss.app.AmossJobCreator
import com.cliffordlab.amoss.datacollector.NotificationCreator.NotificationObject.getNotification
import com.cliffordlab.amoss.datacollector.accel.StartAccelJob
import com.cliffordlab.amoss.datacollector.calls.SaveCallHistoryService
import com.cliffordlab.amoss.datacollector.environment.EnvironmentJob
import com.cliffordlab.amoss.datacollector.liwc.LiwcParserJob
import com.cliffordlab.amoss.datacollector.location.LocationService
import com.cliffordlab.amoss.helper.AmossAlarms
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.receivers.DailySurveyReceiver
import com.cliffordlab.amoss.receivers.PowerConnectionReceiver
import com.cliffordlab.amoss.receivers.WeeklySurveyReceiver
import com.cliffordlab.amoss.settings.SettingsUtil
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class AmossForegroundService : Service() {
    private lateinit var mWakeLock: WakeLock
    private var powerReceiver: BroadcastReceiver? = null
    private var weeklyReceiver: BroadcastReceiver? = null
    private var dailyReceiver: BroadcastReceiver? = null
    private lateinit var mSettingsUtil: SettingsUtil

    companion object {
        private var TAG = AmossForegroundService::class.java.simpleName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        mSettingsUtil = SettingsUtil(this)
        JobManager.create(this).addJobCreator(AmossJobCreator())
        startMyOwnForeground()
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!!.action.equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            stopForeground(true)
            stopSelf()
            stopSelfResult(startId)
        }
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        setReceivers()
        scheduleAllJobs()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        startForeground(NotificationCreator.NotificationObject.notificationId, getNotification(this))
    }

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    private fun setReceivers() {
        Log.i(TAG, "setReceivers: Setting survey receivers...")
        val dailySurveyReceiver = DailySurveyReceiver()
        dailyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                dailySurveyReceiver.onReceiveDailySurvey(context)
            }
        }

        val weeklySurveyReceiver = WeeklySurveyReceiver()
        weeklyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                weeklySurveyReceiver.onReceiveWeeklySurvey(context)
            }
        }

        val powerConnectionReceiver = PowerConnectionReceiver()
        powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                powerConnectionReceiver.onReceive(context, intent)
            }
        }

        registerReceiver(weeklyReceiver, IntentFilter("weekly-broadcast"))
        registerReceiver(dailyReceiver, IntentFilter("daily-broadcast"))
        registerReceiver(powerReceiver, IntentFilter(Intent.ACTION_POWER_CONNECTED))

        val alarms = AmossAlarms()
        alarms.weeklyAlarm(applicationContext)
        alarms.dailyAlarms(applicationContext)
        resetTaskAvailabilityFollowingDay()
        resetTaskAvailabilityFollowingWeek()
    }

    private fun resetTaskAvailabilityFollowingDay() {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(TAG, "onReceive: reset daily notifications success")
                val util = SettingsUtil(context)
                util.setHasCompletedZoom(false)
                util.setHasCompletedSwipe(false)
                val amossAlarms = AmossAlarms()
                amossAlarms.dailyAlarms(context)
            }
        }

        applicationContext.registerReceiver(br, IntentFilter("task-availability-daily"))

        val mgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(applicationContext, 999, Intent("task-availability-daily"), 0)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 1) //0
        mgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
        Log.i(TAG, "resetTaskAvailabilityFollowingDay: scheduling reset of notifications: " + calendar.time)
    }

    private fun resetTaskAvailabilityFollowingWeek() {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(TAG, "onReceive: reset weekly notifications success")
                mSettingsUtil.setHasCompletedPHQ9(false)
                val alarms = AmossAlarms()
                alarms.weeklyAlarm(applicationContext)
            }
        }

        registerReceiver(br, IntentFilter("task-availability-weekly"))
        val mgr: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(), Intent("task-availability-weekly"), 0)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 1) //14
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DATE, 7)
        mgr.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
    }


    fun scheduleAllJobs() {
        Log.i(TAG, "scheduleAllJobs: scheduling all jobs for Amoss Foreground service.")

        val numLiwcParserJobs = JobManager.instance().getAllJobRequestsForTag(LiwcParserJob.JOB_TAG).size
        val numWriteCallJobs = JobManager.instance().getAllJobRequestsForTag(SaveCallHistoryService.JOB_TAG).size
        val numFileUploadJobs = JobManager.instance().getAllJobRequestsForTag(FileUploadJob.jobTag).size
        val numFileDeletionJobs = JobManager.instance().getAllJobRequestsForTag(FileDeletionJob.JOB_TAG).size
        val numStartAccelJobs = JobManager.instance().getAllJobRequestsForTag(StartAccelJob.JOB_TAG).size
        val numStartEnvironmentJobs = JobManager.instance().getAllJobRequestsForTag(EnvironmentJob.JOB_TAG).size
        val numStartLocationJobs = JobManager.instance().getAllJobRequestsForTag(LocationService.JOB_TAG).size

        keepUniqueJobCountAtOne(numStartAccelJobs, StartAccelJob.JOB_TAG, Callable { scheduleAccelStartJob() })
        keepUniqueJobCountAtOne(numLiwcParserJobs, LiwcParserJob.JOB_TAG, Callable { scheduleLiwcJob() })
        keepUniqueJobCountAtOne(numWriteCallJobs, SaveCallHistoryService.JOB_TAG, Callable { scheduleWriteCallsJob() })
        keepUniqueJobCountAtOne(numStartEnvironmentJobs, EnvironmentJob.JOB_TAG, Callable { scheduleEnvironmentJob() })
        keepUniqueJobCountAtOne(numStartLocationJobs, LocationService.JOB_TAG, Callable { scheduleLocationJob() })

        when (packageName) {
            "com.cliffordlab.amoss.moyo" -> {
                Log.i(TAG, "scheduleAllJobs: uploading files...")
                keepUniqueJobCountAtOne(numFileUploadJobs, FileUploadJob.jobTag, Callable { scheduleFileUploadJob() })
            }
            "com.cliffordlab.amoss.moyo.debug" -> {
                Log.i(TAG, "scheduleAllJobs: uploading files...")
                keepUniqueJobCountAtOne(numFileUploadJobs, FileUploadJob.jobTag, Callable { scheduleFileUploadJob() })
            }
            else -> {
                Log.i(TAG, "scheduleAllJobs: uploading files...")
                keepUniqueJobCountAtOne(numFileUploadJobs, FileUploadJob.jobTag, Callable { scheduleFileUploadJob() })
            }
        }
        keepUniqueJobCountAtOne(numFileDeletionJobs, FileDeletionJob.JOB_TAG, Callable { scheduleFileDeletionJob() })
    }

    private fun keepUniqueJobCountAtOne(numOfJobs: Int, jobTag: String, callable: Callable<Int>) {
        Log.i(TAG, "keepUniqueJobCountAtOne: jobtag: " + jobTag)
        when {
            numOfJobs == 1 -> {
                Log.i(TAG, "keepUniqueJobCountAtOne: ")
                println("1 job currently running")
            }
            numOfJobs > 1 -> {
                JobManager.instance().cancelAllForTag(jobTag)
                try {
                    Log.i(TAG, "keepUniqueJobCountAtOne: more than 1 job. calling new job" + jobTag)
                    callable.call()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.e(TAG, "keepUniqueJobCountAtOne: " , e)
                }
            }
            else -> {
                try {
                    Log.i(TAG, "keepUniqueJobCountAtOne: no job. calling new job" + jobTag)
                    callable.call()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.e(TAG, "keepUniqueJobCountAtOne: " , e)
                }
            }
        }
    }

    private fun scheduleAccelStartJob(): Int {
        return JobRequest.Builder(StartAccelJob.JOB_TAG)
                .startNow()
                .setRequiresCharging(false)
                .setUpdateCurrent(true)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule()
    }

    private fun scheduleLiwcJob(): Int {
        return JobRequest.Builder(LiwcParserJob.JOB_TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1))
                .setRequiresCharging(false)
                .setUpdateCurrent(true)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule()
    }


    private fun scheduleEnvironmentJob(): Int {
        return JobRequest.Builder(EnvironmentJob.JOB_TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1))
                .setRequiresCharging(false)
                .setUpdateCurrent(true)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule()
    }

    private fun scheduleLocationJob(): Int {
        return JobRequest.Builder(EnvironmentJob.JOB_TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1))
                .setRequiresCharging(false)
                .setUpdateCurrent(true)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule()
    }


    private fun scheduleWriteCallsJob(): Int {
        return JobRequest.Builder(SaveCallHistoryService.JOB_TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1))
                .setUpdateCurrent(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule()
    }

    private fun scheduleFileUploadJob(): Int {
        return JobRequest.Builder(FileUploadJob.jobTag)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                .setUpdateCurrent(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .setRequirementsEnforced(true)
                .build()
                .schedule()
    }

    private fun scheduleFileDeletionJob(): Int {
        return JobRequest.Builder(FileDeletionJob.JOB_TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1))
                .setUpdateCurrent(true)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.ANY)
                .build()
                .schedule()
    }
}