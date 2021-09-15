package com.cliffordlab.amoss.models;

/**
 * Created by Corey R Shaw on 1/13/17.
 */

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.cliffordlab.amoss.receivers.CombinedFenceBroadcastReceiver;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class GAService extends Service {
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "Awareness";
    private SQLiteDatabase sqLiteDatabase;
    private static final String FENCE_RECEIVER_ACTION = "FENCE_RECEIVE";
    private CombinedFenceBroadcastReceiver fenceReceiver;
    private PendingIntent mFencePendingIntent;
    private FileOutputStream fileOutputStream;


    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            sqLiteDatabase = getBaseContext().openOrCreateDatabase("sqlite-awareness.db", MODE_PRIVATE, null);
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS weather(id INTEGER PRIMARY KEY,condition TEXT,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            Cursor weatherCursor = sqLiteDatabase.rawQuery("SELECT * FROM weather", null);
            int weatherIdIndex =  weatherCursor.getColumnIndex("id");
            int weatherConditionIndex = weatherCursor.getColumnIndex("condition");
            int weatherDateIndex = weatherCursor.getColumnIndex("created_at");

            File Dir = new File( getFilesDir() + "/amoss");

            if(!Dir.exists()) {
                Dir.mkdir();
            }

            String stringFileTime = String.valueOf(System.currentTimeMillis());
            //take the first digit off of the timestamp starting from the left
            String reformattedFileTime = stringFileTime.substring(1);
            String filename = reformattedFileTime;

            File placeFile = new File(Dir, filename + ".places");
            File weatherFile = new File(Dir,filename + ".weather");
            File pollutionFile = new File(Dir,filename + ".pollution");

            if(weatherFile.exists()) {
                weatherFile.delete();
            }

            Realm realm = Realm.getDefaultInstance();
            // Build the query looking at all users:
            RealmResults<PlacesModel> results = realm.where(PlacesModel.class).findAll();
            for (PlacesModel places : results){
                if (!isExternalStorageWritable()) {
                    Log.i("ExternalStorage Status:"," SD card not found");
                } else {
                    //Write each place from realm to file
                    String place = places.getTimestamp() + ","+ places.getName() +","+ places.getPlaceType() +"," + places.getPlaceLikelihood() + "\n";
                    try {
                        fileOutputStream= new FileOutputStream(placeFile,true);
                        fileOutputStream.write(place.getBytes());
                        fileOutputStream.close();
                    }catch(FileNotFoundException e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        e.printStackTrace();
                    }
                }
            }
            realm.executeTransaction(realm1 -> {
                RealmResults<PlacesModel> result = realm1.where(PlacesModel.class).findAll();
                result.deleteAllFromRealm();
            });
            realm.close();

            //Go through all weather columns in database
            weatherCursor.moveToLast();
            while (!weatherCursor.isBeforeFirst()){
                Log.i("Weather Data","Weather ID:"+ weatherCursor.getString(weatherIdIndex) + "  Weather Condition:"+ weatherCursor.getString(weatherConditionIndex)  + "  Created At:"+ weatherCursor.getString(weatherDateIndex));

                if (!isExternalStorageWritable()) {
                    Log.i("ExternalStorage Staus:"," SD card not fonund");
                } else {
                    //Write each weather report from database to file
                    String weatherheader =("Timestamp,Temperature,Feels Like,Dew, Humidity, Cond #");
                    String weather = ("\n"+ weatherCursor.getString(weatherDateIndex) + ","+ weatherCursor.getString(weatherConditionIndex)+ ",");
                    try {
                        fileOutputStream= new FileOutputStream(weatherFile,true);
                        if (weatherCursor.isLast()){
                            fileOutputStream.write(weatherheader.getBytes());
                        }
                        fileOutputStream.write(weather.getBytes());
                        fileOutputStream.close();
                    }catch(FileNotFoundException e){
                        FirebaseCrashlytics.getInstance().recordException(e);
                        e.printStackTrace();
                    }
                }
                weatherCursor.moveToPrevious();
            }

        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();

        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();
        initSnapshots();

        fenceReceiver = new CombinedFenceBroadcastReceiver();
        Intent fenceIntent = new Intent(FENCE_RECEIVER_ACTION);
        mFencePendingIntent = PendingIntent.getBroadcast(this,
                10001,
                fenceIntent,
                0);

        registerFences();
        registerReceiver(fenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));

        return START_STICKY;
    }



    //Initiate Google Awareness's SnapshotApi to register fences to make a call from User's Activity
    private void registerFences() {
        AwarenessFence stillFence = DetectedActivityFence.during(DetectedActivityFence.STILL);
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence("combinedFenceKey", stillFence, mFencePendingIntent)
                        .build())
                .setResultCallback(status -> {
                    if (status.isSuccess()) {
                        Log.i(TAG, "Fence was successfully registered.");
                        initSnapshots();
                    } else {
                        Log.e(TAG, "Fence could not be registered: " + status);
                    }
                });

    }

    private void unregisterFences() {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence("combinedFenceKey")
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence " + "combinedFenceKey" + " successfully removed.");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence " + "combinedFenceKey" + " could NOT be removed.");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterFences();
        unregisterReceiver(fenceReceiver);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }


    //Initiate Google Awareness's SnapshotApi to call for desired results, e.g. location,places,weather
    private void initSnapshots() {
        // First Check permission for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // Return User's Current Location
        Awareness.SnapshotApi.getLocation(mGoogleApiClient)
                .setResultCallback(locationResult -> {
                    if (!locationResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Could not get location.");
                        return;
                    }
                    Location location = locationResult.getLocation();
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addressList != null && addressList.size() > 0) {
                            Log.i(TAG, " Current Location: " + addressList.get(0).toString());
                        }
                    } catch (IOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        e.printStackTrace();
                    }
                });

        // Return All Places Visited by User within 100 meter radius
        Awareness.SnapshotApi.getPlaces(mGoogleApiClient)
                .setResultCallback(placesResult -> {
                    if (!placesResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Could not get places.");
                        return;
                    }
                    Realm.init(getApplicationContext());
                    RealmConfiguration config = new RealmConfiguration.Builder()
                            .name("amoss.realm")
                            .deleteRealmIfMigrationNeeded()
                            .build();
                    Realm realm = Realm.getInstance(config);
                    // Get list of all places visited
                    List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                    if (placeLikelihoodList != null) {
                        for (int i = 0; i <placeLikelihoodList.size(); i++) {
                            realm.beginTransaction();
                            PlaceLikelihood place = placeLikelihoodList.get(i);
                            PlacesModel placesModel = realm.createObject(PlacesModel.class);
                            placesModel.setTimestamp(System.currentTimeMillis());
                            placesModel.setName(place.getPlace().getName().toString().replaceAll("[0-9]",""));

                            StringBuilder builder = new StringBuilder();
                            builder.append("(");
                            int size = place.getPlace().getPlaceTypes().size();
                            List<Integer> placesTypes = place.getPlace().getPlaceTypes();
                            for (int j = 0;j < size;j++) {
                                if (j == (size - 1)) {
                                    builder.append(placesTypes.get(j));
                                } else {
                                    builder.append(placesTypes.get(j));
                                    builder.append(",");
                                }
                            }
                            builder.append(")");
                            placesModel.setPlaceType(builder.toString());

                            placesModel.setPlaceLikelihood(place.getLikelihood()*100);
                            realm.commitTransaction();
                        }
                    } else {
                        Log.e(TAG, "Place is null.");
                    }

                    realm.close();
                });

        // Return User's Current Weather Conditions
        Awareness.SnapshotApi.getWeather(mGoogleApiClient)
                .setResultCallback(weatherResult -> {
                    if (!weatherResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Could not get weather.");
                        return;
                    }
                    Weather weather = weatherResult.getWeather();
                    sqLiteDatabase.execSQL("INSERT INTO weather (condition) VALUES ('" + weather +"')");
                });

        // Return User's Current Activity
        Awareness.SnapshotApi.getDetectedActivity(mGoogleApiClient)
                .setResultCallback(detectedActivityResult -> {
                    if (!detectedActivityResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Could not get the current activity.");
                        return;
                    }
                    ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                    DetectedActivity probableActivity = ar.getMostProbableActivity();
                    Log.i(TAG, "Current Activity: " + probableActivity.toString());
                });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

