package com.cliffordlab.amoss.app

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.multidex.MultiDex
import com.cliffordlab.amoss.datacollector.AmossForegroundService
import com.cliffordlab.amoss.datacollector.FileDeletionJob
import com.cliffordlab.amoss.datacollector.FileUploadJob
import com.cliffordlab.amoss.datacollector.accel.StartAccelJob
import com.cliffordlab.amoss.datacollector.calls.SaveCallHistoryService
import com.cliffordlab.amoss.datacollector.liwc.LiwcParserJob
import com.cliffordlab.amoss.gui.MainActivity
import com.cliffordlab.amoss.helper.AmossAlarms
import com.cliffordlab.amoss.receivers.DailySurveyReceiver
import com.cliffordlab.amoss.receivers.PowerConnectionReceiver
import com.cliffordlab.amoss.receivers.WeeklySurveyReceiver
import com.cliffordlab.amoss.settings.SettingsUtil
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Created by ChristopherWainwrightAaron on 1/30/17.
 */
class AmossApplication : Application() {
    private var mActivityTransitionTimer: Timer? = null
    private var mActivityTransitionTimerTask: TimerTask? = null
    var wasInBackground = false
    private val MAX_ACTIVITY_TRANSITION_TIME_MS = 60000 * 5.toLong()
    private lateinit var mSettingsUtil: SettingsUtil

    override fun onCreate() {
        super.onCreate()
        context = this
        JobManager.create(this).addJobCreator(AmossJobCreator())
        Realm.init(applicationContext)
        val config = RealmConfiguration.Builder()
            .name("amoss.realm")
            .allowWritesOnUiThread(true)
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)

//        val settingsUtil = SettingsUtil(applicationContext)
//        if (settingsUtil.isAmossLoggedIn) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startMyOwnForeground()
//            } else {
//                scheduleAllJobs()
//            }
//            setReceivers()
//        }
    }

    private fun startMyOwnForeground() {
        if (!isServiceRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, AmossForegroundService::class.java))
            }
        }
    }

    //service run in foreground
    private val isServiceRunning: Boolean
        get() {
            var serviceRunning = false
            val am = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val runningServices = am.getRunningServices(50)
            for (runningServiceInfo in runningServices) {
                Log.i(TAG, "isServiceRunning: classname: " + runningServiceInfo.service.className)
                if (runningServiceInfo.service.className == "AmossForegroundService") {
                    serviceRunning = true
                    if (runningServiceInfo.foreground) {
                        //service run in foreground
                        Log.i(TAG, "isServiceRunning: true")
                    }
                }
            }
            return serviceRunning
        }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun scheduleAllJobs() {
//        val numLiwcParserJobs = JobManager.instance().getAllJobRequestsForTag(LiwcParserJob.JOB_TAG).size
//        val numWriteCallJobs = JobManager.instance().getAllJobRequestsForTag(SaveCallHistoryService.JOB_TAG).size
//        val numFileUploadJobs = JobManager.instance().getAllJobRequestsForTag(FileUploadJob.jobTag).size
//        val numAccelJobs = JobManager.instance().getAllJobRequestsForTag(FileDeletionJob.JOB_TAG).size
//        val numStartAccelJobs = JobManager.instance().getAllJobRequestsForTag(StartAccelJob.JOB_TAG).size
//        keepUniqueJobCountAtOne(numStartAccelJobs, StartAccelJob.JOB_TAG, Callable { scheduleAccelStartJob() })
//        keepUniqueJobCountAtOne(numLiwcParserJobs, LiwcParserJob.JOB_TAG, Callable { scheduleLiwcJob() })
//        keepUniqueJobCountAtOne(numWriteCallJobs, SaveCallHistoryService.JOB_TAG, Callable { scheduleWriteCallsJob() })
//        keepUniqueJobCountAtOne(numAccelJobs, FileDeletionJob.JOB_TAG, this::scheduleFileDeletionJob);
    }

    //start all jobs associated with accelerometer data
    fun startAccelJobs() {
        //get the number all of current jobs running
        val numDeletionJobs = JobManager.instance().getAllJobRequestsForTag(FileDeletionJob.JOB_TAG).size
        val numStartAccelJobs = JobManager.instance().getAllJobRequestsForTag(StartAccelJob.JOB_TAG).size
        val numFileUploadJobs = JobManager.instance().getAllJobRequestsForTag(FileUploadJob.jobTag).size
        when (packageName) {
            "com.cliffordlab.amoss.moyo" -> {
                println("Not uploading files")
                println("Not uploading files")
                //TODO run upload job when working
                println("Uploading files")
                keepUniqueJobCountAtOne(numFileUploadJobs, FileUploadJob.jobTag, Callable { scheduleFileUploadJob() })
            }
            "com.cliffordlab.amoss.moyo.debug" -> {
                println("Not uploading files")
                println("Uploading files")
                keepUniqueJobCountAtOne(numFileUploadJobs, FileUploadJob.jobTag, Callable { scheduleFileUploadJob() })
            }
            else -> {
                println("Uploading files")
                keepUniqueJobCountAtOne(numFileUploadJobs, FileUploadJob.jobTag, Callable { scheduleFileUploadJob() })
            }
        }


        //make sure current job only is running one instance
        keepUniqueJobCountAtOne(numDeletionJobs, FileDeletionJob.JOB_TAG, Callable { scheduleFileDeletionJob() })

        //make sure current job only is running one instance
        keepUniqueJobCountAtOne(numStartAccelJobs, StartAccelJob.JOB_TAG, Callable { scheduleAccelStartJob() })
    }

    fun startCallActJobs() {
        val numWriteCallJobs = JobManager.instance().getAllJobRequestsForTag(SaveCallHistoryService.JOB_TAG).size

        //make sure current job only is running one instance
        keepUniqueJobCountAtOne(numWriteCallJobs, SaveCallHistoryService.JOB_TAG, Callable { scheduleWriteCallsJob() })
    }

    fun startLIWCActJobs() {
        val numLiwcParserJobs = JobManager.instance().getAllJobRequestsForTag(LiwcParserJob.JOB_TAG).size

        //make sure current job only is running one instance
        keepUniqueJobCountAtOne(numLiwcParserJobs, LiwcParserJob.JOB_TAG, Callable { scheduleLiwcJob() })
    }

    fun stopCallActJobs() {
        JobManager.instance().cancelAllForTag(SaveCallHistoryService.JOB_TAG)
    }

    fun stopLIWCActJobs() {
        JobManager.instance().cancelAllForTag(LiwcParserJob.JOB_TAG)
    }

    fun stopAccelJobs() {
        JobManager.instance().cancelAllForTag(FileDeletionJob.JOB_TAG)
        JobManager.instance().cancelAllForTag(StartAccelJob.JOB_TAG)
    }

    fun startActivityTransitionTimer() {
        mActivityTransitionTimer = Timer()
        mActivityTransitionTimerTask = object : TimerTask() {
            override fun run() {
                wasInBackground = true
            }
        }
        mActivityTransitionTimer!!.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS)
    }

    fun stopActivityTransitionTimer() {
        if (mActivityTransitionTimerTask != null) {
            mActivityTransitionTimerTask!!.cancel()
        }
        if (mActivityTransitionTimer != null) {
            mActivityTransitionTimer!!.cancel()
        }
        wasInBackground = false
    }

    fun cancelAllJobs(): Int {
        return JobManager.instance().cancelAll()
    }

    private fun keepUniqueJobCountAtOne(numOfJobs: Int, jobTag: String, callable: Callable<Int>) {
        if (numOfJobs == 1) {
            Log.i(TAG, "keepUniqueJobCountAtOne: ")
            println("1 job currently running")
        } else if (numOfJobs > 1) {
            JobManager.instance().cancelAllForTag(jobTag)
            try {
                callable.call()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                println(e.message)
            }
        } else {
            try {
                callable.call()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                println(e.message)
            }
        }
    }

    private fun scheduleAccelStartJob(): Int {
        return JobRequest.Builder(StartAccelJob.JOB_TAG) //                .startNow()
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1))
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
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1))
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

    fun setReceivers() {
        Log.i(MainActivity.TAG, "setReceivers: Setting survey receivers...")
        val dailySurveyReceiver = DailySurveyReceiver()
        val dailyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                dailySurveyReceiver.onReceiveDailySurvey(context)
            }
        }

        val weeklySurveyReceiver = WeeklySurveyReceiver()
        val weeklyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                weeklySurveyReceiver.onReceiveWeeklySurvey(context)
            }
        }

        val powerConnectionReceiver = PowerConnectionReceiver()
        val powerReceiver = object : BroadcastReceiver() {
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
                Log.i(MainActivity.TAG, "onReceive: reset daily notifications success")
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
        calendar.set(Calendar.HOUR_OF_DAY, 1)
        mgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
        Log.i(MainActivity.TAG, "resetTaskAvailabilityFollowingDay: scheduling reset of notifications: " + calendar.time)
    }

    private fun resetTaskAvailabilityFollowingWeek() {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(MainActivity.TAG, "onReceive: reset weekly notifications success")
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

    companion object {
        private const val TAG = "AmossApplication"
        lateinit var context: Context
            private set
    }
}