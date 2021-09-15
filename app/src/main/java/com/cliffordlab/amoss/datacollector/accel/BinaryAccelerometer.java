package com.cliffordlab.amoss.datacollector.accel;

import android.content.Context;

import com.cliffordlab.amoss.helper.CSVCreator;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ChristopherWainwrightAaron on 9/6/16.
 */
public class BinaryAccelerometer {
    private static Accelerometer[] accelerometer;
    private static int index = 0;
    private boolean okToWrite = true;
    private static long fileTime;
    private final Context mContext;

    public BinaryAccelerometer(Context context) {
        this.mContext = context;
    }

    //continueWrite is called in AccelService on sensorChanged it is used to store the values of
    //the sensor data into an array and that write that array as binary to a file
    void continueWrite(long timeValue, double xValue, double yValue, double zValue) {
        //accelerometer which is an array will be set to null after each file write
        //each file will be 10,000 rows of data
        if (accelerometer == null) {
            accelerometer = new Accelerometer[5000];
        }

        //okToWrite is to make sure a bunch of files do not get opened while this is still writing on separate thread
        if (accelerometer[4999] != null && okToWrite) {
            //set okToWrite to false so when this function get called again it hits
            //the else statement and does nothing until okToWrite is set back to true
            okToWrite = false;
            //set fileTime with timestamp of file creation which should be around the time
            //the current sample is created this means that all timestamps in the file are
            //before the timestamp used to create the file name
            //not that we get this time from the time variable created in onSensorChanged
            fileTime = timeValue;

            //run the function that this observable returns on a separate thread
            //observe the result on the main thread
            getAccelerometerObservable(accelerometer)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse, this::handleError);

        } else {
            if (!okToWrite) {
                //if okToWrite is false and this hits it means no accelerometer
                //data is being collected while the file is being written
                System.out.println("File is being written cannot write accel data to array");
            } else if(index < accelerometer.length) {
                //setting the accelerometer array with a new row of data from
                //onSensorChanged in the accelerometer service
                accelerometer[index] = new Accelerometer(timeValue, xValue, yValue, zValue);
                //increment the index for new data
                index += 1;
            }
        }
    }

    private void handleResponse(Accelerometer accelSample) {
        System.out.println("This is the value at last index of accelerometer array " + accelSample);
        //set accelerometer to null so that when function is called again it makes a new array
        accelerometer = null;
        index = 0;
        //allow the addition of the data in a new accelerometer array
        okToWrite = true;
    }

    private void handleError(Throwable e) {
        e.printStackTrace();
        accelerometer = null;
        // setting index must be before okToWrite becomes true because
        // sensor sample rate might catch at wrong time
        index = 0;
        okToWrite = true;
    }

    private Accelerometer writeAccelerometerData(Accelerometer[] accelerometer) {
        //create data output stream in order to write binary data to file
        DataOutputStream accelerometerOutput = createFile();

        //loop through all 10,000 objects in array
        for(Accelerometer data : accelerometer) {
            createAccelerometer(data, accelerometerOutput);
        }

        try {
            accelerometerOutput.close();
        } catch (IOException ioe) {
            FirebaseCrashlytics.getInstance().recordException(ioe);
            ioe.printStackTrace();
        }

        return accelerometer[accelerometer.length - 1];
    } // end of writeAccelerometerData

    //creates a dataoutputsteam to write
    //binary data to
    private DataOutputStream createFile() {
        try {
            CSVCreator csvCreator = new CSVCreator(mContext);
            String fileName = csvCreator.getFileName(fileTime, "acc", ".csv");
            //filepath is from external file storage instead of private
            File accDirectory = new File(mContext.getFilesDir() + "/amoss");
            //if directory does not exist create one
            if (!accDirectory.exists()) {
                accDirectory.mkdirs();
            }
            //file to be written to
            File listOfData = new File(accDirectory,  fileName);
            //use dataoutputstream with file to be able to write binary data
            //true param allows file to be appended to instead of rewritten
            return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(listOfData, true)));
        } catch(IOException ioe) {
            FirebaseCrashlytics.getInstance().recordException(ioe);
            ioe.printStackTrace();
        }
        return null;
    } // end of createFile

    private void createAccelerometer(Accelerometer accelerometer, DataOutputStream accelerometerOutput) {
        try {
            //time values are milliseconds but default for matlab is seconds
            //getting difference between fileTime and accelerometer time
            //use filetime - difference (which is the output) to get time of sample
            //difference should never be more than an int primitives max value
            //if for some reason difference is to big for int the cast will ensure consistency
            //in differences, in this implementation we are using the reverse (timeValue - fileTime)
            //because I mistakenly did this initially and because the data current produced
            //data is all in that format
            accelerometerOutput.writeInt((int) (accelerometer.getTimeValue() - fileTime));
            //multiply by 1000 in order to save a short primitive
            //when parsing the data just divide by 1000 to get back value
            accelerometerOutput.writeShort((short) (accelerometer.getxValue() * 1000));
            accelerometerOutput.writeShort((short) (accelerometer.getyValue() * 1000));
            accelerometerOutput.writeShort((short) (accelerometer.getzValue() * 1000));
        } catch (IOException ioe) {
            FirebaseCrashlytics.getInstance().recordException(ioe);
            ioe.printStackTrace();
        }
    }

    //observable needed to write accelerometer file on a separate thread
    private Observable<Accelerometer> getAccelerometerObservable(final Accelerometer[] accelerometers) {
        return Observable.fromCallable(() -> {
            //uses to current accelerometer array of accelerometer data
            //to write the data in the array to a file
            return writeAccelerometerData(accelerometers);
        });
    }
} // end of writeSensorBinaryStreams
