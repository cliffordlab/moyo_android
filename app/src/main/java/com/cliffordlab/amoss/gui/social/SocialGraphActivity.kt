package com.cliffordlab.amoss.gui.social

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.text.format.DateFormat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.R
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.YAxisValueFormatter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.social_graph_activity.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SocialGraphActivity : AppCompatActivity()  {
    companion object Title {
        val name = "SOCIAL"
        val TAG = "SocialGraphActivity"
    }
    private val MIKES_BLUE = "#1B3775"

    private val xAxis = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14")
    private val incomingCallList = ArrayList<Int>()
    private val outgoingCallList = ArrayList<Int>()
    val callDateList = ArrayList<String>()
    val incomingCallDateList = ArrayList<String>()
    val outgoingCallDateList = ArrayList<String>()
    val incomingEntries = ArrayList<Entry>()
    val outgoingEntries = ArrayList<Entry>()
    private lateinit var incomingSet: LineDataSet
    private lateinit var outgoingSet: LineDataSet
    private val incomingSMSList = ArrayList<Int>()
    private val outgoingSMSList = ArrayList<Int>()
    val incomingSMSEntries = ArrayList<Entry>()
    val smsDateList = ArrayList<String>()
    val outgoingSMSEntries = ArrayList<Entry>()
    private lateinit var incomingSMSSet: LineDataSet
    private lateinit var outgoingSMSSet: LineDataSet
    private var incomingGraphPoints: MutableList<CallHistoryGraphPoints> = ArrayList()
    private var outgoingGraphPoints: MutableList<CallHistoryGraphPoints> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.social_graph_activity)
        setCallGraph()
        setTextGraph()
        textView25.text = "INCOMING CALL HISTORY"
        textView48.text = "OUTGOING CALL HISTORY"
        textView49.text = "TEXT HISTORY"
    }

    private fun setTextGraph() {
        getTextDataSet(context = applicationContext)
        val textList = ArrayList<LineDataSet>()
        textList.add(incomingSMSSet)
        textList.add(outgoingSMSSet)
        val xAxisText = ArrayList<String>()
        var counter = 0
        val textData: LineData
        if (smsDateList.size < 14) {
            textData = LineData(xAxis, textList)
        } else {
        Log.i(TAG, "setTextGraph: text date list: " + smsDateList)
            for (item in smsDateList) {
                xAxisText.add(item)
                counter +=1
            }
            textData = LineData(xAxisText, textList)
        }
        val xAxis = textGraph.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        textGraph.data = textData
        textGraph.setDescription("")
        textGraph.invalidate()
        setTextGraphYAxis()
    }

    private fun setTextGraphYAxis() {
        val yAxisRight = textGraph.axisRight
        yAxisRight.isEnabled = false
        textGraph.axisLeft.axisMinValue = 0f
        val customYAxisFormatter = YAxisValueFormatter { value, axis -> value.toInt().toString() }
        textGraph.axisLeft.valueFormatter = customYAxisFormatter
    }

    private fun setCallGraph() {
        getCallGraphSetByDate()
        val outgoingCallList = ArrayList<LineDataSet>()
        outgoingCallList.add(outgoingSet)
        setIncomingXAxis()
        setOutgoingXAxis()
        setIncomingYAxis()
        setOutgoingYAxis()
    }

    private fun setOutgoingXAxis() {
        val outgoingCallList = ArrayList<LineDataSet>()
        outgoingCallList.add(outgoingSet)
        val xAxisCall = ArrayList<String>()
        val callData: LineData
        callData = if (outgoingCallDateList.size < 14) {
            LineData(xAxis, outgoingCallList)
        } else {
            Log.i(TAG, "setCallGraph: call date list: " + callDateList)
            for (item in outgoingCallDateList) {
                xAxisCall.add(item)
            }
            LineData(xAxisCall, outgoingCallList)
        }
        val xAxis = outgoingCallGraph.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        outgoingCallGraph.data = callData
        outgoingCallGraph.setDescription("")
        outgoingCallGraph.invalidate()
    }

    private fun setIncomingXAxis() {
        val incomingCallList = ArrayList<LineDataSet>()
        incomingCallList.add(incomingSet)
        val xAxisCall = ArrayList<String>()
        val callData: LineData
        callData = if (incomingCallDateList.size < 14) {
            LineData(xAxis, incomingCallList)
        } else {
            Log.i(TAG, "setCallGraph: call date list: " + callDateList)
            for (item in incomingCallDateList) {
                xAxisCall.add(item)
            }
            LineData(xAxisCall, incomingCallList)
        }
        val xAxis = incomingCallGraph.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        incomingCallGraph.data = callData
        incomingCallGraph.setDescription("")
        incomingCallGraph.invalidate()
    }

    private fun setOutgoingYAxis() {
        val yAxisRight = outgoingCallGraph.axisRight
        yAxisRight.isEnabled = false
        outgoingCallGraph.axisLeft.axisMinValue = 0f
        outgoingCallGraph.axisLeft.setValueFormatter { value, axis -> value.toInt().toString() }
    }

    private fun setIncomingYAxis() {
        val yAxisRight = incomingCallGraph.axisRight
        yAxisRight.isEnabled = false
        incomingCallGraph.axisLeft.axisMinValue = 0f
        incomingCallGraph.axisLeft.setValueFormatter { value, axis -> value.toInt().toString() }
    }

    fun getCallGraphSetByDate(){
        val cr = applicationContext.contentResolver
        val headerMap = java.util.HashMap<String, String>()
        headerMap["Authorization"] = "Bearer "
        if (cr != null) {
            try {
                var currentDate = -14
                for (i in 0..14) {
                    val pastDate = getCalculatedDate("yyyy-MM-dd HH:mm:ss", currentDate)
                    val pastDateString = getDate(pastDate)
                    val pastDateFloat = java.lang.Float.parseFloat(pastDateString)
                    val pastDateEnd = pastDate + 86400001
                    val projection = arrayOf("type", "date", "duration")
                    val cursor = cr.query(android.provider.CallLog.Calls.CONTENT_URI, projection, "date " + "between " + pastDate + " and " + pastDateEnd, null, "date DESC") // using cursor to iterate over inbox
                    while (cursor!!.moveToNext()) {
                        val duration = cursor.getColumnIndex(CallLog.Calls.DURATION)
                        val type = cursor.getColumnIndex(CallLog.Calls.TYPE)
                        val callDuration = cursor.getString(duration)
                        val callType = cursor.getString(type)
                        val dircode = Integer.parseInt(callType)
                        val callDurationInMinutes = Integer.valueOf(callDuration) / 60
                        when (dircode) {
                            CallLog.Calls.INCOMING_TYPE -> {
                                incomingCallList.add(callDurationInMinutes)
                                val graphPoints = CallHistoryGraphPoints(callDurationInMinutes, pastDateFloat, pastDateString)
                                incomingGraphPoints.add(graphPoints)
                                incomingCallDateList.add(pastDateString)
                            }
                            CallLog.Calls.OUTGOING_TYPE -> {
                                outgoingCallList.add(callDurationInMinutes)
                                val graphPoints = CallHistoryGraphPoints(callDurationInMinutes, pastDateFloat, pastDateString)
                                outgoingGraphPoints.add(graphPoints)
                                outgoingCallDateList.add(pastDateString)
                            }
                        }
                        callDateList.add(pastDateString)
                    }
                    currentDate += 1
                    cursor.close()
                }
                for (i in incomingCallList.indices) {
                    val incomingItem = incomingCallList[i]
                    incomingEntries.add(Entry(incomingItem.toFloat(), i))
                }

                for (i in outgoingCallList.indices) {
                    val outgoingItem = outgoingCallList[i]
                    outgoingEntries.add(Entry(outgoingItem.toFloat(), i))
                }

                incomingSet = LineDataSet(incomingEntries, "Last 14 days of incoming calls in minutes")
                outgoingSet = LineDataSet(outgoingEntries, "Last 14 days of outgoing calls in minutes")
                incomingSet.color = Color.parseColor(MIKES_BLUE)
                outgoingSet.color = Color.RED
            } catch (e: SecurityException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }


    }

    fun getTextDataSet(context: Context) {
        val cr = context.contentResolver
        var currentDate = -14
        for (i in 0..13) {
            val pastDate = getCalculatedDate("yyyy-MM-dd HH:mm:ss", currentDate)
            val pastDateString = getDate(pastDate)
            Log.i(TAG, "getTextDataSet: this is the past string date: " + pastDateString)
            val pastDateEnd = pastDate + 86400001
            val projection = arrayOf("type", "date")
            val incomingCursor = cr.query(Uri.parse("content://sms/inbox"), projection, "date " + "between " + pastDate + " and " + pastDateEnd, null, "date DESC") // using cursor to iterate over inbox
            val outgoingCursor = cr.query(Uri.parse("content://sms/sent"), projection, "date " + "between " + pastDate + " and " + pastDateEnd, null, "date DESC") // using cursor to iterate over inbox
            incomingCursor!!.count
            currentDate += 1
            incomingSMSList.add(incomingCursor.count)
            smsDateList.add(pastDateString)
            outgoingSMSList.add(outgoingCursor!!.count)
        }
        val incomingSMSTail = incomingSMSList.subList(Math.max(incomingSMSList.size - 14, 0), incomingSMSList.size) // totalIncomingSMSCount was 14
        for (i in incomingSMSTail.indices) {
            val value = incomingSMSTail[i]
            incomingSMSEntries.add(Entry(value.toFloat(), i))
        }

        val outgoingSMSTail = outgoingSMSList.subList(Math.max(outgoingSMSList.size - 14, 0), outgoingSMSList.size)
        for (i in outgoingSMSTail.indices) {
            val value = outgoingSMSTail[i]
            outgoingSMSEntries.add(Entry(value.toFloat(), i))
        }

        incomingSMSSet = LineDataSet(incomingSMSEntries, "Past 14 days of incoming text")
        outgoingSMSSet = LineDataSet(outgoingSMSEntries, "Past 14 days of outgoing text")
        incomingSMSSet.color = Color.parseColor(MIKES_BLUE)
        outgoingSMSSet.color = Color.RED
    }

    /**
     * Pass your date format and no of days for minus from current
     * If you want to get previous date then pass days with minus sign
     * else you can pass as it is for next date
     * @param dateFormat
     * @param days
     * @return Calculated Date
     */
    fun getCalculatedDate(dateFormat: String, days: Int): Long {
        val cal = Calendar.getInstance()
        SimpleDateFormat(dateFormat)
        cal.add(Calendar.DAY_OF_YEAR, days)
        cal.add(Calendar.HOUR_OF_DAY, 0)
        cal.add(Calendar.MINUTE, 0)
        cal.add(Calendar.SECOND, 0)
        cal.add(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getDate(time: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        return DateFormat.format("MM.dd", cal).toString()
    }

}
