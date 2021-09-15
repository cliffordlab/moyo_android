package com.cliffordlab.amoss.gui.surveys.graphs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.gui.charts.graphData.GraphDataModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.YAxisValueFormatter
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import io.realm.Realm
import kotlinx.android.synthetic.main.survey_graph.*
import java.util.*


class SurveyGraph: AppCompatActivity() {
    private val MIKES_BLUE = "#1B3775"
    private val xAxis = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    private lateinit var survey: String
    val yAxisIconLabels = ArrayList<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cliffordlab.amoss.R.layout.survey_graph)
        //set survey from bundle
        val intent = intent
        survey = intent.getStringExtra("survey").toString()
        setYAxisValues()

        setGraph()
    }

    private fun setGraph() {
        val data = LineData(xAxis, getDataSet())
        val xAxis = surveyGraph.xAxis
        textView48.setTextColor(Color.parseColor(MIKES_BLUE))
        textView48.text = survey.uppercase(Locale.getDefault())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        surveyGraph.data = data
        surveyGraph.setDescription("")
        surveyGraph.animateXY(2000, 2000)
        surveyGraph.extraTopOffset = 20f
        surveyGraph.invalidate() // refreshes chart
        setYAxis()
    }

    private fun setYAxisValues() {
        val angry = BitmapFactory.decodeResource(applicationContext.resources,
                com.cliffordlab.amoss.R.drawable.angry)
        val sad = BitmapFactory.decodeResource(applicationContext.resources,
                com.cliffordlab.amoss.R.drawable.sad)
        val neutral = BitmapFactory.decodeResource(applicationContext.resources,
                com.cliffordlab.amoss.R.drawable.neutral)
        val happy = BitmapFactory.decodeResource(applicationContext.resources,
                com.cliffordlab.amoss.R.drawable.happy)
        val excited = BitmapFactory.decodeResource(applicationContext.resources,
                com.cliffordlab.amoss.R.drawable.excited)

        yAxisIconLabels.add(angry)
        yAxisIconLabels.add(sad)
        yAxisIconLabels.add(neutral)
        yAxisIconLabels.add(happy)
        yAxisIconLabels.add(excited)
    }

    private fun setYAxis() {
        val yAxis = surveyGraph.axisLeft
        surveyGraph.axisRight.isEnabled = false
        yAxis.setStartAtZero(false)
        when (survey) {
            "MOOD SWIPE" -> {
                yAxis.axisMinValue = 1f
                yAxis.setLabelCount(5, true)
                yAxis.axisMaxValue = 5f
                val customYaxisFormatter = YAxisValueFormatter { value, yAxis -> "       " }
                surveyGraph.axisLeft.valueFormatter = customYaxisFormatter
                surveyGraph.rendererLeftYAxis = YAxisCustomRenderer(yAxis, surveyGraph.getTransformer(yAxis.axisDependency), surveyGraph.viewPortHandler, this)
            }
            "MOOD ZOOM" -> {
                yAxis.axisMinValue = 0f
                yAxis.setLabelCount(42, true)
                yAxis.axisMaxValue = 42f
                val customYaxisFormatter = YAxisValueFormatter { value, yAxis -> value.toInt().toString() }
                surveyGraph.axisLeft.valueFormatter = customYaxisFormatter
            }
            "PHQ9" -> {
                yAxis.axisMinValue = 0f
                yAxis.setLabelCount(25, true)
                yAxis.axisMaxValue = 25f
                val customYaxisFormatter = YAxisValueFormatter { value, yAxis -> value.toInt().toString() }
                surveyGraph.axisLeft.valueFormatter = customYaxisFormatter
            }
        }
    }

    private fun getDataSet(): LineDataSet? {
        // pull data form realm and set to data like valuesOfPastGAmes in ehas
        val scoreList = ArrayList<Int>()
        val phq9ScoreList = ArrayList<Double>()
        val realm = Realm.getDefaultInstance()

        val results = realm.where(GraphDataModel::class.java).findAll()
        var score = 0
        var phq9Score: Double
        val entries = ArrayList<Entry>()

        if (results.size != 0) {
            for (model in results) {
                when (survey) {
                    "moodSwipe" -> {
                        if (model.moodSwipeScore != null) {
                            score = model.moodSwipeScore!!
                            scoreList.add(score)
                        }
                    }
                    "moodZoom" -> {
                        if (model.moodZoomScore != null) {
                            score = model.moodZoomScore!!
                            scoreList.add(score)
                        }
                    }
                    "phq9" -> {
                        if (model.pHQ9Score != null) {
                            phq9Score = model.pHQ9Score!!
                            phq9ScoreList.add(phq9Score)
                        }
                    }
                }
            }
            return when (survey) {
                "phq9" -> {
                    convertPHQ9ScoreToBarDataSet(phq9ScoreList, entries)
                }
                else -> {
                    convertToLineDataSet(scoreList, entries)
                }
            }
        }
        realm.close()
        return LineDataSet(entries, "Last 10 surveys")
    }

    private fun convertPHQ9ScoreToBarDataSet(phq9ScoreList: ArrayList<Double>, entries: ArrayList<Entry>): LineDataSet? {

        val tail = phq9ScoreList.subList(Math.max(phq9ScoreList.size - 10, 0), phq9ScoreList.size)
        for (i in tail.indices) {
            val value = tail.get(i)
            entries.add(BarEntry(value.toFloat(), i))
        }
        return LineDataSet(entries, "Last 10 surveys")
    }

    private fun convertToLineDataSet(scoreList: ArrayList<Int>, entries: ArrayList<Entry>): LineDataSet? {

        val tail = scoreList.subList(Math.max(scoreList.size - 10, 0), scoreList.size)
        for (i in tail.indices) {
            val value = tail[i]
            entries.add(BarEntry(value.toFloat(), i))
        }
        return LineDataSet(entries, "Last 10 surveys")
    }

    inner class YAxisCustomRenderer(yAxis: YAxis, transformer: Transformer, viewPortHandler: ViewPortHandler, private val context: Context) : YAxisRenderer(viewPortHandler, yAxis, transformer) {

        override fun drawYLabels(c: Canvas?, fixedPosition: Float, positions: FloatArray?, offset: Float) {
            super.drawYLabels(c, fixedPosition, positions, offset)

            for (i in 0 until yAxisIconLabels.size) {


                if (!mYAxis.isDrawTopYLabelEntryEnabled && i >= yAxisIconLabels.size - 1)
                    return

                c!!.drawBitmap(getScaledBitmap(yAxisIconLabels[i]), (fixedPosition) -55, (positions!![i * 2 + 1] + offset) -50, mAxisLabelPaint)
            }
        }

        private fun getScaledBitmap(bitmap: Bitmap): Bitmap {
//            val width = context.getResources().getDimension(R.dimen.abc_action_bar_content_inset_material) as Int
//            val height = context.getResources().getDimension(R.dimen.dimen_18) as Int
            return Bitmap.createScaledBitmap(bitmap, 75, 75, true)
        }


    }
}

