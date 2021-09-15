package com.cliffordlab.amoss.settings

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.cliffordlab.amoss.app.AmossApplication
import com.cliffordlab.amoss.datacollector.FileDeletionJob
import com.cliffordlab.amoss.datacollector.FileUploadJob
import com.cliffordlab.amoss.datacollector.accel.AccelService
import com.cliffordlab.amoss.datacollector.accel.StartAccelJob
import com.cliffordlab.amoss.datacollector.calls.CallHistory
import com.cliffordlab.amoss.datacollector.calls.SaveCallHistoryService
import com.cliffordlab.amoss.datacollector.environment.EnvironmentJob
import com.cliffordlab.amoss.datacollector.environment.EnvironmentService
import com.cliffordlab.amoss.datacollector.liwc.LiwcParserJob
import com.cliffordlab.amoss.datacollector.location.LocationService
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit


/**
 * Created by ChristopherWainwrightAaron on 4/4/17.
 */

class ServiceControl(private val mContext: Context, private val mActivity: Activity) {

    fun initServices() {
        if (Build.VERSION.SDK_INT >= 23) {
            val mSettingsUtil = SettingsUtil(mContext)
            if (mActivity.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                mSettingsUtil.setCallDataCollection(true)
                val numWriteCallJobs = JobManager.instance().getAllJobRequestsForTag(SaveCallHistoryService.JOB_TAG).size
                keepUniqueJobCountAtOne(numWriteCallJobs, SaveCallHistoryService.JOB_TAG) { scheduleWriteCallsJob() }
                ServiceControl(mContext, mActivity).startCallActivityServices()
            } else {
                mSettingsUtil.setCallDataCollection(false)
                ServiceControl(mContext, mActivity).stopCallActivityServices()
            }

            if (mActivity.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                mSettingsUtil.setLIWCDataCollection(true)
                val numLiwcParserJobs = JobManager.instance().getAllJobRequestsForTag(LiwcParserJob.JOB_TAG).size
                keepUniqueJobCountAtOne(numLiwcParserJobs, LiwcParserJob.JOB_TAG) { scheduleLiwcJob() }

                ServiceControl(mContext, mActivity).startLIWCActivityServices()
            } else {
                mSettingsUtil.setLIWCDataCollection(false)
                ServiceControl(mContext, mActivity).stopLIWCActivityServices()
            }
            if (mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && mActivity.checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mSettingsUtil.setLocCollection(true)
                val numStartEnvironmentJobs = JobManager.instance().getAllJobRequestsForTag(EnvironmentJob.JOB_TAG).size
                val numStartLocationJobs = JobManager.instance().getAllJobRequestsForTag(LocationService.JOB_TAG).size

                keepUniqueJobCountAtOne(numStartEnvironmentJobs, EnvironmentJob.JOB_TAG) { scheduleEnvironmentJob() }
                keepUniqueJobCountAtOne(numStartLocationJobs, LocationService.JOB_TAG) { scheduleLocationJob() }
                ServiceControl(mContext, mActivity).startLocationServices()
            } else {
                mSettingsUtil.setLocCollection(false)
                ServiceControl(mContext, mActivity).stopLocationServices()
            }

            if (mActivity.checkSelfPermission(Manifest.permission_group.SENSORS) == PackageManager.PERMISSION_GRANTED) {
                mSettingsUtil.setAccCollection(true)
                ServiceControl(mContext, mActivity).startPhysicalActivityServices()
            } else {
                mSettingsUtil.setAccCollection(false)
                ServiceControl(mContext, mActivity).stopPhysicalActivityServices()
            }

            val numFileUploadJobs = JobManager.instance().getAllJobRequestsForTag(FileUploadJob.jobTag).size
            val numFileDeletionJobs = JobManager.instance().getAllJobRequestsForTag(FileDeletionJob.JOB_TAG).size
            keepUniqueJobCountAtOne(numFileUploadJobs, FileUploadJob.jobTag) { scheduleFileUploadJob() }
            keepUniqueJobCountAtOne(numFileDeletionJobs, FileDeletionJob.JOB_TAG) { scheduleFileDeletionJob() }
        }
    }

    //start accelerometer services/jobs
    fun startPhysicalActivityServices(): Void? {
        
        requestGrantofPermissionsForActivityServices()
        if (isMyServiceRunning(AccelService::class.java)) {
            println("Accelerometer service is already running")
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "startPhysicalActivityServices: starting")
                mContext.startForegroundService(Intent(mContext, AccelService::class.java))
            }
            val numStartAccelJobs = JobManager.instance().getAllJobRequestsForTag(StartAccelJob.JOB_TAG).size
            keepUniqueJobCountAtOne(numStartAccelJobs, StartAccelJob.JOB_TAG, Callable { scheduleAccelStartJob() })
        }
        return null
    }

    private fun requestGrantofPermissionsForActivityServices() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (isSignedIn()) {

                Log.v(TAG, "Activity permission is granted")

            } else {
                val util = SettingsUtil(mContext)
                util.setAccCollection(false)
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
        }
    }

    //stop accelerometer services/jobs
    fun stopPhysicalActivityServices() {
        if (isMyServiceRunning(AccelService::class.java)) {
            Log.d(TAG, "stopPhysicalActivityServices: ")
            mContext.stopService(Intent(mContext, AccelService::class.java))
        }
        AmossApplication().stopAccelJobs()
    }


    private fun isSignedIn(): Boolean {
        Log.d(TAG, "isSignedIn: ")
        return GoogleSignIn.getLastSignedInAccount(mContext) != null
    }

    //start social job
    fun startCallActivityServices(): Void? {
        Log.d(TAG, "startCallActivityServices: ")
        requestGrantOfPermissionsForCallServices()
        AmossApplication().startCallActJobs()
        return null
    }

    //start social job
    fun startLIWCActivityServices(): Void? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "startLIWCActivityServices: ")
            mContext.startForegroundService(Intent(mContext, CallHistory::class.java))
        }
        AmossApplication().startLIWCActJobs()
        return null
    }

    private fun requestGrantOfPermissionsForCallServices() {
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d(TAG, "requestGrantOfPermissionsForCallServices: ")
            if (mActivity.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "CALL permission is granted")

            } else {
                Log.v(TAG, "Permission is not granted now requesting permission from user")
                ActivityCompat.requestPermissions(mActivity,
                        arrayOf(Manifest.permission.READ_CALL_LOG),
                        CALL_PERMISSION_REQUEST_CODE)
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
        }
    }

    private fun requestGrantOfPermissionsForLIWCServices() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mActivity.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "LIWC permission is granted")

            } else {
                Log.v(TAG, "Permission is not granted now requesting permission from user")
                ActivityCompat.requestPermissions(mActivity,
                        arrayOf(Manifest.permission.READ_SMS),
                        LIWC_PERMISSION_REQUEST_CODE)
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
        }
    }

    //stop call job
    fun stopCallActivityServices() {
        Log.d(TAG, "stopCallActivityServices: ")
        AmossApplication().stopCallActJobs()
    }

    //stop sms job
    fun stopLIWCActivityServices() {
        Log.d(TAG, "stopLIWCActivityServices: ")
        AmossApplication().stopLIWCActJobs()
    }

    private fun showAlert() {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            val dialog = AlertDialog.Builder(mActivity)
            dialog.setTitle("Enable Battery Saving Location")
                    .setMessage("Your battery saving location setting is set to 'Off'.\nPlease enable  this location setting.")
                    .setPositiveButton("OK") { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        mActivity.startActivity(intent)
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
            dialog.show()
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
    }


    //start the location service
    fun startLocationServices(): Void? {
        requestGrantOfPermissionsForLocationServices()
        if (isLocationPermissionGranted) {
            if (!isLocationEnabled) {
                //show alert and ask user to enable location
                if (!didRequestToEnableLocation) {
                    didRequestToEnableLocation = true
                    showAlert()
                }
            }
            //if service is not running start service
            if (!isMyServiceRunning(LocationService::class.java)) {
                Log.d(TAG, "startLocationServices: ")
                mContext.startService(Intent(mContext, LocationService::class.java))
                mContext.startService(Intent(mContext, EnvironmentService::class.java))
            }
        }
        return null
    }

    //stop the location service
    fun stopLocationServices() {
        if (isMyServiceRunning(LocationService::class.java)) {
            Log.d(TAG, "stopLocationServices: ")
            mContext.stopService(Intent(mContext, EnvironmentService::class.java))
            mContext.stopService(Intent(mContext, LocationService::class.java))
        }
    }

    //request privileges from user for location services
    private fun requestGrantOfPermissionsForLocationServices() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && mActivity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Log.v(TAG, "Location permission is granted")

            } else {

                Log.v(TAG, "Permission is not granted now requesting permission from user")
                ActivityCompat.requestPermissions(mActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
        }
    }

    private //permission is automatically granted on sdk<23 upon installation
    val isLocationPermissionGranted: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= 23) {
                return mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            }
            return true
        }

    private val isLocationEnabled: Boolean
        get() {
            val mLocationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

    companion object {
        private val TAG = ServiceControl::class.java.simpleName
        private var didRequestToEnableLocation: Boolean = false
        val LOCATION_PERMISSION_REQUEST_CODE = 1
        val ACTIVITY_PERMISSION_REQUEST_CODE = 2
        val CALL_PERMISSION_REQUEST_CODE = 3
        val LIWC_PERMISSION_REQUEST_CODE = 4
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
