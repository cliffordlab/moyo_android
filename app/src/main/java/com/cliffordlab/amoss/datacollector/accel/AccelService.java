package com.cliffordlab.amoss.datacollector.accel;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.cliffordlab.amoss.datacollector.NotificationCreator;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccelService extends Service implements SensorEventListener {

	private static final String TAG = AccelService.class.getSimpleName();
	private SensorManager mSensorManager;
	//writes accelerometer data to storage
	private BinaryAccelerometer mBinaryAccelerometer;
	private PowerManager.WakeLock mWakeLock;
	private long startTime;
	private static List<ActivityGraphPoints> data;
	private static final int THRESHOLD = 180;
//	private static final int THRESHOLD = 4;

	public AccelService() {}

    @Override
    public void onCreate() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startMyOwnForeground();
		}

        super.onCreate();
    }

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void startMyOwnForeground() {
		startForeground(NotificationCreator.NotificationObject.notificationId, NotificationCreator.NotificationObject.INSTANCE.getNotification(this));
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire(10*60*1000L /*10 minutes*/);
		data = new ArrayList<>();
		startTime = System.currentTimeMillis();

		mBinaryAccelerometer = new BinaryAccelerometer(getApplicationContext());
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_NORMAL);
		return START_STICKY;
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		//generating time in millis because time from accelerometer
		//is not consistent between phones and is in nano seconds
		long time = System.currentTimeMillis();
		mBinaryAccelerometer.continueWrite(time,
				event.values[0],
				event.values[1],
				event.values[2]);

		long diff = time - startTime;
		if (Math.abs(diff) > 60000) {
			//reset start time to account for new minute
			startTime = time;
			if (data.size() <= THRESHOLD) {
				//add transformed data into array
				Accelerometer acc = new Accelerometer(time, event.values[0], event.values[1], event.values[2]);
				ActivityGraphPoints graphPoints = new ActivityGraphPoints(acc.getTimeValue(), acc.transformRawData());
				data.add(graphPoints);
			}
		}

		if (data.size() > THRESHOLD) {
			String filename = System.currentTimeMillis() + ".graph";
			//filepath is from external file storage instead of private
			File graphDirectory = new File(getApplicationContext().getFilesDir() + "/graph");
			Log.i(TAG, "onSensorChanged: path: " + graphDirectory);
			//if directory does not exist create one
			if (!graphDirectory.exists()) {
				graphDirectory.mkdirs();
			}
			//file to be written to
			File listOfData = new File(graphDirectory,  filename);
			//use dataoutputstream with file to be able to write binary data
			//true param allows file to be appended to instead of rewritten
			try {
				DataOutputStream graphOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(listOfData, true)));
				for (ActivityGraphPoints points : data) {
					graphOutput.writeLong(points.getTimeVal());
					graphOutput.writeFloat(points.getActivityVal());
				}
				graphOutput.close();
			} catch (IOException ioe) {
				FirebaseCrashlytics.getInstance().recordException(ioe);
				ioe.printStackTrace();
			}
			//remove all objects from array
			data.clear();
		}


	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mWakeLock != null) {
			mWakeLock.release();
		}
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
