package com.cliffordlab.amoss.datacollector.calls;


import android.util.Log;

import androidx.annotation.NonNull;

import com.cliffordlab.amoss.app.AmossApplication;
import com.cliffordlab.amoss.helper.CSVCreator;
import com.evernote.android.job.Job;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ChristopherWainwrightAaron on 7/13/16.
 */
//TODO
public class SaveCallHistoryService extends Job {
    public static final String JOB_TAG = "WRITE_CALLS_JOB";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        getWriteCallObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse, this::handleError);
        return Result.SUCCESS;
    }

    private void handleResponse(Boolean complete) {
        System.out.println("Write calls complete: " + complete);
    }
    private void handleError(Throwable e) {
        Log.e(JOB_TAG, e.getMessage(), e);
    }

    private Observable<Boolean> getWriteCallObservable() {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws IOException {
                return writeCallHistory();
            }
        });
    }

    private boolean writeCallHistory() {
        CallHistory callHistory = new CallHistory();
        String callLog = callHistory.getCallDetails(getContext());

        try {

            File file = new File(getContext().getFilesDir() + "/amoss");
            if (!file.exists()) {
                file.mkdirs();
            }

            CSVCreator csvCreator = new CSVCreator(AmossApplication.Companion.getContext());
            String fileName = csvCreator.getFileName(System.currentTimeMillis(), "call", ".csv");

            file = new File(getContext().getFilesDir() + "/amoss/" + fileName);

            if(!file.exists()) {
                System.out.println("file for calls is created" + file.createNewFile());
            }

            RandomAccessFile f = new RandomAccessFile(file, "rw");
            f.seek(0); // to the beginning
            f.write(callLog.getBytes());
            f.close();

            System.out.println("Write call history job completed");
            return true;
        } catch (IOException ioe) {
            FirebaseCrashlytics.getInstance().recordException(ioe);
            ioe.printStackTrace();
        }
        System.out.println("Write call history job incomplete");
        return false;
    }
}
