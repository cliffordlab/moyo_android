package com.cliffordlab.amoss.gui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.helper.logger.Log
import com.cliffordlab.amoss.helper.logger.LogView
import com.cliffordlab.amoss.helper.logger.LogWrapper
import com.cliffordlab.amoss.helper.logger.MessageOnlyLogFilter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.android.synthetic.main.activity_steps_log.*
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class StepsLogActivity : AppCompatActivity() {

    val TAG = "StepCounter"
    private val REQUEST_OAUTH_REQUEST_CODE = 0x1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps_log)
        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initializeLogging()
        }

        val fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build()
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions)
        } else {
            subscribe()
        }
        sample_logview.setOnClickListener {
            readData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe()
            }
        }
    }

    /** Records step data by requesting a subscription to background step data.  */
    fun subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "Successfully subscribed!")
                    } else {
                        Log.w(TAG, "There was a problem subscribing.", task.exception)
                    }
                }
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private fun readData() {
        var total = 0L// get the start and end date of the urrent mobile
        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -14)
        val startTime = cal.timeInMillis
        val dateFormat = DateFormat.getDateInstance()

        //-------------steps-------------------------------
        //code to get last 10 days steps
        val readRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .readData(readRequest)
                .addOnSuccessListener { dataReadResponse ->
                    processData(dataReadResponse)
                }
                .addOnFailureListener { e -> Log.w(TAG, "There was a problem getting the step count.", e) }


//        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
//                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
//                .addOnSuccessListener { dataSet ->
//                    if (dataSet.isEmpty) {
//                        total = 0
//                    } else {
//                        collectData(dataSet)
//                        total = dataSet.dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt().toLong()
//                        Log.i(TAG, "Total steps: " + total)
//                    }
//                }
//                .addOnFailureListener(
//                        object : OnFailureListener {
//                            override fun onFailure(e: Exception) {
//                                Log.w(TAG, "There was a problem getting the step count.", e)
//                            }
//                        })
     }

    private fun processData(dataReadResponse: DataReadResponse?) {
        // display data
        //Used for aggregated data
        if (dataReadResponse!!.buckets.size > 0) {
            Log.e("History", "Number of buckets: " + dataReadResponse.buckets.size)
            for (bucket in dataReadResponse.buckets) {
                val dataSets = bucket.dataSets
                for (dataSet in dataSets) {
//                    showDataSet(dataSet)
                    if (dataSet.isEmpty) {
                        Log.i(TAG, "processData: " + 0)
                    } else {
                        Log.i(TAG, "processData: " + dataSet.dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt().toLong())
                    }
                }
            }
        } else if (dataReadResponse.dataSets.size > 0)
        //Used for non-aggregated data
        {
            Log.e("History", "Number of returned DataSets: " + dataReadResponse.dataSets.size)
            for (dataSet in dataReadResponse.dataSets) {
                showDataSet(dataSet)
            }
        }
    }

    private fun showDataSet(dataSet: DataSet?) {
        //in graph?
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun collectData(dataSet: DataSet?) {
        //collect weeks worth of data to display on graph in fragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the main; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_read_data) {
            readData()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** Initializes a custom log class that outputs both to in-app targets and logcat.  */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun initializeLogging() {
        // Wraps Android's native log framework.
        val logWrapper = LogWrapper()
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper)
        // Filter strips out everything except the message text.
        val msgFilter = MessageOnlyLogFilter()
        logWrapper.next = msgFilter
        // On screen logging via a customized TextView.
        val logView = findViewById<LogView>(R.id.sample_logview)

        // Fixing this lint error adds logic without benefit.

        logView.setTextAppearance(R.style.Log)

        logView.setBackgroundColor(Color.WHITE)
        msgFilter.next = logView
        Log.i(TAG, "Ready")
    }

}
