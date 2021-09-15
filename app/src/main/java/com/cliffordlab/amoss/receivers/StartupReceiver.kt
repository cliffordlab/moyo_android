package com.cliffordlab.amoss.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.cliffordlab.amoss.app.AmossApplication
import com.cliffordlab.amoss.datacollector.AmossForegroundService
import com.cliffordlab.amoss.datacollector.accel.AccelService
import com.cliffordlab.amoss.datacollector.location.LocationService
import com.cliffordlab.amoss.settings.SettingsUtil
import com.evernote.android.job.JobProxy

class StartupReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "StartupReceiver"
    }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED" || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            if (SettingsUtil(context).isLocCollectionEnabled ||
                SettingsUtil(context).isLIWCDataCollectionEnabled ||
                SettingsUtil(context).isCallDataCollectionEnabled ||
                SettingsUtil(context).isAccCollectedEnabled ||
                SettingsUtil(context).isSocialDataCollectionEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AmossApplication.context.startForegroundService(Intent(AmossApplication.context, AmossForegroundService::class.java))
                } else {
                    AmossApplication.context.startService(Intent(AmossApplication.context, AccelService::class.java))
                    JobProxy.Common.startWakefulService(context, Intent(context, AccelService::class.java))
                    JobProxy.Common.startWakefulService(context, Intent(context, LocationService::class.java))
                }
            }
        }
    }
}