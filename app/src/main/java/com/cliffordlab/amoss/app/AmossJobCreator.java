package com.cliffordlab.amoss.app;

import com.cliffordlab.amoss.datacollector.FileDeletionJob;
import com.cliffordlab.amoss.datacollector.accel.StartAccelJob;
import com.cliffordlab.amoss.datacollector.calls.SaveCallHistoryService;
import com.cliffordlab.amoss.datacollector.FileUploadJob;
import com.cliffordlab.amoss.datacollector.environment.EnvironmentJob;
import com.cliffordlab.amoss.datacollector.liwc.LiwcParserJob;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by ChristopherWainwrightAaron on 1/30/17.
 */

public class AmossJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case FileDeletionJob.JOB_TAG:
                return new FileDeletionJob();
            case StartAccelJob.JOB_TAG:
                return new StartAccelJob();
            case LiwcParserJob.JOB_TAG:
                return new LiwcParserJob();
            case SaveCallHistoryService.JOB_TAG:
                return new SaveCallHistoryService();
            case EnvironmentJob.JOB_TAG:
                return new EnvironmentJob();
            case FileUploadJob.jobTag:
                return new FileUploadJob();
            default:
                return null;
        }
    }
}
