package com.cliffordlab.amoss.gui.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.datacollector.accel.ActivityGraphPoints
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.YAxisValueFormatter
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

class AccelGraphActivity : AppCompatActivity() {

    companion object Title {
        val name = "ACTIVITY"
        private const val TAG = "GraphActivity"
    }

    private lateinit var chart: LineChart

    private val dataList: MutableList<ActivityGraphPoints> = arrayListOf()
    private var entries: MutableList<Entry> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        chart = findViewById(R.id.chart)
        getAccData()
    }

    private fun getAccData() {
        val directory = File(applicationContext.filesDir.toString() + "/graph")

        Log.i("graph", "file dir: " + directory)
        if (!directory.exists()) {
            print("Sorry no activity graph available more data is needed")
        } else {
            if (directory.isDirectory) {
                val files = directory.listFiles()
                Log.i("graph", "size of files: " + files.size)
                for (i in files.indices) {
                    var accFileCounter = 0
                    val isAccFile = files[i].name.endsWith(".graph")

                    println("Graph file names: ${files[i].name} and is acc: ${isAccFile}")
                    if (accFileCounter < 8 && isAccFile) {
                        accFileCounter += 1
                        val dataInputStream = DataInputStream(BufferedInputStream(FileInputStream(files[i])))

                        while (dataInputStream.available() > 0) {
                            val acc = ActivityGraphPoints(dataInputStream.readLong(), dataInputStream.readFloat())
                            dataList.add(acc)
                        }
                    }
                }
                Log.i("graph", "before gathering of data points")
                dataList.map { Entry(it.timeVal.toFloat(), it.activityVal.toInt()) }
                        .forEach { entries.add(it) }
                Log.i("graph", "size of datalist: " + dataList.size)
                if (dataList.size == 0) {
                    print("Sorry no activity graph available more data is needed")
                    return
                }
                setGraph()
            }
        }

    }

    private fun setGraph() {
        for (i in entries.indices) {
//            var formatedNumber = DecimalFormat("#").format(entries[i].toString())
            Log.i(TAG, "setGraph: this is the entires double parsed: " + entries[i])
        }
        val dataSet = LineDataSet(entries, "Activity Data")
        val xAxisList = ArrayList<String>()
//        val minValue = getMinValue(entries)
//        val maxValue = getMaxValue(entries)

        for (i in entries.indices) {
            xAxisList.add(i.toString())
        }
        val lineData = LineData(xAxisList, dataSet)
        chart.data = lineData
        chart.isAutoScaleMinMaxEnabled = true
        chart.setDescription("")
        chart.animateXY(2000,2000)
        chart.invalidate()
        setAxis()
    }

    private fun setAxis() {
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        //        val maxYvalue = 50
        val yAxisRight = chart.axisRight
        yAxisRight.isEnabled = false
//        surveyGraph.axisLeft.setLabelCount(maxYvalue + 2, true)
        chart.axisLeft.axisMinValue = 0f
//        surveyGraph.axisLeft.axisMaxValue = (maxYvalue + 1).toFloat()
        val customYaxisFormatter = YAxisValueFormatter { value, _ -> value.toInt().toString() }
        chart.axisLeft.valueFormatter = customYaxisFormatter

    }

    // getting the maximum value
    fun getMaxValue(array: MutableList<Entry>): Int {
        var maxValue = array[0].xIndex
        for (i in 1 until array.size) {
            if (array[i].toString().toInt() > maxValue.toString().toInt()) {
                maxValue = array[i].xIndex
            }
        }
        return maxValue.toString().toInt()
    }

    // getting the miniumum value
    fun getMinValue(array: MutableList<Entry>): Int {
        var minValue = array[0].xIndex
        for (i in 1 until array.size) {
            if (array[i].toString().toInt() < minValue.toString().toInt()) {
                minValue = array[i].xIndex
            }
        }
        return minValue.toString().toInt()
    }

}



