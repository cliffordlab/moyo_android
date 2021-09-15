package com.cliffordlab.amoss.datacollector;

import androidx.annotation.NonNull;

import com.evernote.android.job.Job;

import java.io.File;
import java.util.Date;

/**
 * Created by ChristopherWainwrightAaron on 2/21/17.
 */

public class FileDeletionJob extends Job {
    public static final String JOB_TAG = "FILE_DELETION_JOB";

    @NonNull
    @Override
    protected Job.Result onRunJob(Job.Params params) {
        File root = new File(getContext().getFilesDir() + "/amoss");
        //find all files that have not been uploaded
        File[] allFiles = root.listFiles((dir, filename) -> filename.endsWith(".sent"));

        if (allFiles == null) {
            System.out.println("Failed because there are no files in folder");
            return Result.FAILURE;
        }

        if (allFiles.length > 0) {
            for (File file : allFiles) {
                if (!file.isDirectory()) {
                    long diff = new Date().getTime() - file.lastModified();
                    //if time since last modified for .sent files is over 2 months then delete
                    if (diff > 60L * 24L * 60L * 60L * 1000L) {
                        file.delete();
                    }
                }
            }
        }
        return Job.Result.SUCCESS;
    }

}
