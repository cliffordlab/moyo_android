package com.cliffordlab.amoss.datacollector.location;

import android.content.Context;
import android.util.Log;

import com.cliffordlab.amoss.helper.CSVCreator;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ChristopherWainwrightAaron on 5/2/16.
 */

public class LocationData {
    private final Context mContext;

    public LocationData(Context context) {
        this.mContext = context;
    }

    public String createData(long time, double[] locationInfo) {
        return time + "," + locationInfo[0] + "," + locationInfo[1] + "," + locationInfo[2] + System.getProperty("line.separator");
    }

    public void close(String data) {
        subscribeToLocationData(data);
    }

    private Boolean writeLocationData (String data) throws IOException {
        CSVCreator csvCreator = new CSVCreator(mContext);
        String fileName = csvCreator.getFileName(System.currentTimeMillis(), "loc", ".csv");
        //filepath is from external file storage instead of private
        File directory = new File(mContext.getFilesDir() + "/amoss");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File locFile = new File(directory.getAbsolutePath() + "/" + fileName);

        if (!locFile.exists()) {
            try {
                locFile.createNewFile();
            } catch (IOException ioe) {
                FirebaseCrashlytics.getInstance().recordException(ioe);
                ioe.printStackTrace();
            }
        }

        try {
            FileWriter fw = new FileWriter(locFile);
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(data);
            writer.close();
        } catch (IOException ioe) {
            FirebaseCrashlytics.getInstance().recordException(ioe);
            ioe.printStackTrace();
        }

        return true;
    }

    private void subscribeToLocationData(String data) {
        getLocationDataObservable(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse);

    }

    private void handleResponse(Boolean success) {
        if (success) {
            Log.i("location data","writer work successful");
        }
        Log.i("location data","writer work done");
    }
    private Observable<Boolean> getLocationDataObservable(final String data) {
        Log.i("location data", data);
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return writeLocationData(data);
            }
        });
    }
}