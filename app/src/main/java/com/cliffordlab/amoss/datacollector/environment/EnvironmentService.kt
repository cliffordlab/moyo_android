package com.cliffordlab.amoss.datacollector.environment

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.cliffordlab.amoss.app.AmossApplication.Companion.context
import com.cliffordlab.amoss.datacollector.NotificationCreator
import com.cliffordlab.amoss.datacollector.NotificationCreator.NotificationObject.getNotification
import com.cliffordlab.amoss.datacollector.calls.SaveCallHistoryService
import com.cliffordlab.amoss.datacollector.location.GPSTracker
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.network.AmossNetwork
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

class EnvironmentService : Service() {
    companion object {
        private const val TAG = "EnvironmentService"
    }

    private var mDisposable: Disposable? = null

    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        }
        super.onCreate()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        startForeground(
            NotificationCreator.NotificationObject.notificationId,
            getNotification(this)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val gps = GPSTracker(context)

        if (gps.canGetLocation()) {

            val lat = gps.latitude
            val lon = gps.longitude
            AmossNetwork.changeBaseURL("https://amoss.emory.edu/")
            val mapQuery = HashMap<String, String>()
            mapQuery["lat"] = lat.toString()
            mapQuery["long"] = lon.toString()
            mDisposable = AmossNetwork.client.getEnvironmentalJson(mapQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ handleResponse(it) }, { handleError(it) })
        }
        return START_STICKY
    }

    private fun handleResponse(response: JsonObject) {
        Log.d(TAG, "handleResponse: $response")
        writeFile(response)
    }

    private fun handleError(e: Throwable) {
        Log.e(SaveCallHistoryService.JOB_TAG, e.message, e)
    }

    private fun writeFile(response: JsonObject): Boolean {
        try {
            var file = File(context.filesDir.toString() + "/amoss")
            if (!file.exists()) {
                file.mkdirs()
            }
            val csvCreator = CSVCreator(context)
            val fileName = csvCreator.getFileName(System.currentTimeMillis(), "environment", ".csv")
            file = File(context.filesDir.toString() + "/amoss/" + fileName)
            if (!file.exists()) {
                println("file for calls is created" + file.createNewFile())
            }
            val f = RandomAccessFile(file, "rw")
            f.seek(0) // to the beginning
            f.write(response.toString().toByteArray())
            f.close()
            println("Write call history job completed")
            return true
        } catch (ioe: IOException) {
            FirebaseCrashlytics.getInstance().recordException(ioe)
            ioe.printStackTrace()
        }
        println("Write call history job incomplete")
        return false
    }

    override fun onBind(intent: Intent?): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }
}
