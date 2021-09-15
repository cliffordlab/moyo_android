package com.cliffordlab.amoss.datacollector.accel;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cliffordlab.amoss.settings.SettingsUtil;
import com.evernote.android.job.Job;

/**
 * Created by ChristopherWainwrightAaron on 2/23/17.
 */

public class StartAccelJob extends Job {
    public static final String JOB_TAG = "START_ACCEL_JOB";
    private static final String TAG = "StartAccelJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        if(SettingsUtil.isDataCollected(getContext())) {
            boolean serviceRunning = isMyServiceRunning(AccelService.class);
            if (!serviceRunning) {
                Log.i(TAG, "onRunJob: service not running");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getContext().startForegroundService(new Intent(getContext(), AccelService.class));
                } else {
                    getContext().startService(new Intent(getContext(), AccelService.class));
                }
            }
        } else {
            getContext().stopService(new Intent(getContext(), AccelService.class));
        }

        return Result.SUCCESS;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
