package com.cliffordlab.amoss.receivers;

import android.content.Context;
import android.content.Intent;

import androidx.legacy.content.WakefulBroadcastReceiver;

import java.io.File;

/**
 * Created by ChristopherWainwrightAaron on 4/10/16.
 */
public class LowStorageReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.DEVICE_STORAGE_LOW")) {
            File root = new File(context.getFilesDir() + "/amoss");

            File[] allFiles = root.listFiles();

            if (allFiles == null) {
                System.out.println("Failed because there are no files in folder");
                return;
            }

            for (File file : allFiles) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        }
    }
}
