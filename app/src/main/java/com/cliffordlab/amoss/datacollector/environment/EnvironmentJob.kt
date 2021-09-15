package com.cliffordlab.amoss.datacollector.environment

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.cliffordlab.amoss.settings.SettingsUtil
import com.evernote.android.job.Job

class EnvironmentJob: Job() {
    companion object {
        const val JOB_TAG = "START_ENVIRONMENT_JOB"
    }

    override fun onRunJob(params: Params?): Result {
        if (SettingsUtil.isDataCollected(context)) {
            val serviceRunning = isMyServiceRunning(EnvironmentService::class.java)
            if (!serviceRunning) {
                Log.i(JOB_TAG, "onRunJob: service not running")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(Intent(context, EnvironmentService::class.java))
                } else {
                    context.startService(Intent(context, EnvironmentService::class.java))
                }
            }
        } else {
            context.stopService(Intent(context, EnvironmentService::class.java))
        }
        return Result.SUCCESS
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}