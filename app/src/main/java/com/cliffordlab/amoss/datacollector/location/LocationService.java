package com.cliffordlab.amoss.datacollector.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.cliffordlab.amoss.models.GAService;
import com.cliffordlab.amoss.settings.SettingsUtil;

/**
 * Created by ChristopherWainwrightAaron on 5/2/16.
 */
public class LocationService extends Service {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager mLocationManager;
    private LocationData mLocationData;
    private final long initTime = System.currentTimeMillis();
    private PowerManager.WakeLock mWakeLock;
    private static final String TAG = LocationService.class.getSimpleName();
    public static final String JOB_TAG = "LOCATION_JOB";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationData = new LocationData(this);

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            System.out.println("Permission to collect location is not granted");
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 60 * 1000, 100, locationListenerNetwork);
        }
        return START_STICKY;
    }

    public LocationService(){}

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            SettingsUtil util = new SettingsUtil(getApplicationContext());
            if (util.isGoogleDataCollectionEnabled()) {
                startService(new Intent(getApplicationContext(), GAService.class));
            }
            double[] locationInfo = {location.getLatitude() - 20, location.getLongitude() - 20, location.getAltitude()};
            String data = mLocationData.createData(location.getTime(), locationInfo);
            mLocationData.close(data);
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
        @Override
        public void onProviderEnabled(String s) {
        }
        @Override
        public void onProviderDisabled(String s) {
        }
    };
}
