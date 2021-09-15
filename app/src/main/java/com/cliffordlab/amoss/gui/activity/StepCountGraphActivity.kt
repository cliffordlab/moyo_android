package com.cliffordlab.amoss.gui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.helper.AmossDialogs
import com.cliffordlab.amoss.settings.SettingsUtil
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.YAxisValueFormatter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_step_count_graph.*
import java.util.*
import java.util.concurrent.TimeUnit

class StepCountGraphActivity : AppCompatActivity() {
    private val MIKES_BLUE = "#1B3775"
    private val REQUEST_OAUTH_REQUEST_CODE = 0x1001
    private val weeklyTotalStepCountList = ArrayList<Int>()
    private lateinit var mSettingsUtil: SettingsUtil

    companion object {
        private const val TAG = "StepCountGraphActivity"
    }

    private val xAxis = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_count_graph)
        mSettingsUtil = SettingsUtil(this)
        if (mSettingsUtil.isGoogleDataCollectionEnabled) {
            textView55.text = "STEP COUNT"
        } else {
            signIntoGoogleFitnessAPI()
            textView55.text = "STEP COUNT(Disabled)"
        }
        textView55.setOnClickListener {
            setGraph()
        }
    }

    fun signIntoGoogleFitnessAPI() {
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


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe()
            }
        } else {
            val dialog = AmossDialogs()
            dialog.showDialog("google", this)
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
                    requestData()
                    setGraph()
                } else {
                    Log.w(TAG, "There was a problem subscribing.", task.exception)
                }
            }
    }

    private fun setGraph() {

        // set axis with past dates
        val data = LineData(xAxis, getDataSet())
        val xAxis = stepCountGraph.xAxis
        textView55.setTextColor(Color.parseColor(MIKES_BLUE))
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        stepCountGraph.data = data
        stepCountGraph.setDescription("")
        stepCountGraph.animateXY(2000, 2000)
        stepCountGraph.invalidate() // refreshes chart
        setYAxis()
    }

    private fun setYAxis() {
        val yAxisRight = stepCountGraph.axisRight
        yAxisRight.isEnabled = false
//        stepCountGraph.axisLeft.setLabelCount(maxYvalue + 2, true)
        stepCountGraph.axisLeft.axisMinValue = 0f
//        stepCountGraph.axisLeft.axisMaxValue = (maxYvalue + 1).toFloat()
        val customYaxisFormatter = YAxisValueFormatter { value, yAxis -> value.toInt().toString() }
        stepCountGraph.axisLeft.valueFormatter = customYaxisFormatter
    }

    private fun getDataSet(): LineDataSet? {
        val stepCountList = ArrayList<Long>()
        val realm = Realm.getDefaultInstance()

        val results = realm.where(StepCountRealmModel::class.java).findAll()
        val entries = ArrayList<Entry>()
        var stepCount = 0L
        if (results.size != 0) {
            for (model in results) {
                if (model.stepCount != null) {
                    stepCount = model.stepCount!!
                    stepCountList.add(stepCount)
                }
            }
            convertToLineDataSet(stepCountList, entries)
        }
        realm.close()
        return LineDataSet(entries, "Total daily step count from past 2 weeks")
    }

    private fun convertToLineDataSet(stepCountList: ArrayList<Long>, entries: ArrayList<Entry>): LineDataSet {

        val tail = stepCountList.subList(Math.max(stepCountList.size - 14, 0), stepCountList.size)
        for (i in tail.indices) {
            val value = tail[i]
            entries.add(BarEntry(value.toFloat(), i))
        }
        return LineDataSet(entries, "Total daily step count from past 2 weeks")
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private fun requestData() {
        var total = 0L// get the start and end date of the urrent mobile
        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -14)
        val startTime = cal.timeInMillis

        //-------------steps-------------------------------
        //code to get last 14 days steps
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
    }

    private fun processData(dataReadResponse: DataReadResponse?) {
        Realm.init(applicationContext)
        var total = 0L
        val config = RealmConfiguration.Builder()
                .name("amoss.realm")
                .deleteRealmIfMigrationNeeded()
                .build()
        val realm = Realm.getInstance(config)

        //Used for aggregated data
        if (dataReadResponse!!.buckets.size > 0) {
            Log.e("History", "Number of buckets: " + dataReadResponse.buckets.size)
            for (bucket in dataReadResponse.buckets) {
                val dataSets = bucket.dataSets
                for (dataSet in dataSets) {
//                    showDataSet(dataSet)
                    if (dataSet.isEmpty) {
                        total = 0
                        Log.i(TAG, "processData: " + 0)
                    } else {
                        Log.i(TAG, "processData: " + dataSet.dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt().toLong())
                        total = dataSet.dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt().toLong()
                    }
                    Log.i(TAG, "processData: currentTotalStepCount: " + total)
                    realm.executeTransaction { realm1 ->
                        realm1.where(StepCountRealmModel::class.java).findAll()
                        val dataModel = realm1.createObject(StepCountRealmModel::class.java)
                        dataModel.stepCount = total
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
        realm.close()
        setGraph()
    }

    private fun showDataSet(dataSet: DataSet?) {
        //in graph?
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
