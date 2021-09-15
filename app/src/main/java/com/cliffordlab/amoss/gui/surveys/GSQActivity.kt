package com.cliffordlab.amoss.gui.surveys

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.adapters.SurveyAdapter
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.network.AmossNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class GSQActivity: AppCompatActivity() {
    private var mDisposable: Disposable? = null
    private lateinit var adapter: SurveyAdapter

    companion object {
        const val TAG = "GSQActivity"
    }

    private var possibleResponseList = listOf(
            "Not at all", "Some of the time", "More than half of the time", "All of the time"
    )

    private var possibleResponseList2 = listOf(
            "0-15 min", "16-30 min", "31-45 min", "46-60 min", "60+ min"
    )

    private var possibleResponseList3 = listOf(
            "Very bad", "Bad", "Okay", "Good", "Very Good"
    )

    private var possibleResponseList4 = listOf(
            "No", "Yes"
    )

    private var possibleResponseList5 = listOf(
            ""
    )

    private val questionsMap : List<Triple<String, List<String>, Int?>> = listOf(
            Triple("In the last three days I have taken my medications as scheduled", possibleResponseList, null),
            Triple("Today I have heard voices or saw things others cannot", possibleResponseList, null),
            Triple("Today I have thoughts racing through my head", possibleResponseList, null),
            Triple("Today I feel I have special powers" , possibleResponseList, null),
            Triple("Today I feel people are watching me" , possibleResponseList, null),
            Triple("Today I feel people are against me" , possibleResponseList, null),
            Triple("Today I feel consumed or puzzled" , possibleResponseList, null),
            Triple("In the last three days during the daytime I have gone outside my home" , possibleResponseList, null),
            Triple("In the last three days I have preferred to spend time alone" , possibleResponseList, null),
            Triple("In the last three days I have had arguments with other people" , possibleResponseList, null),
            Triple("In the last three days I have had someone to talk to" , possibleResponseList, null),
            Triple("In the last three days I have felt uneasy with groups of people" , possibleResponseList, null),
            Triple("How much exercise have you gotten today? " , possibleResponseList2, null),
            Triple("How did you feel this week?" , possibleResponseList3, null),
            Triple("Have you been admitted to the hospital for psychiatric reasons?" , possibleResponseList4, null),
            Triple("Use this space to write down your thoughts and feelings about the week?" , possibleResponseList5, null)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        // set up the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.surveyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SurveyAdapter(this, questionsMap, TAG)
        recyclerView.adapter = adapter

    }

    fun onSubmitBtnPressed(view: View) {
        if (view is Button) {
            submitCSV()
            finish()
        }
    }

    private fun writeCSV(): StringBuilder {
        val map = adapter.getSurveyResults()
        val headerRow = StringBuilder()
        val answerRow = StringBuilder()
        val csv = StringBuilder()
        var totalScore = 0
        for ((key, value) in map) {
            headerRow.append("$key, ")
            answerRow.append(value.toString() + ", ")
            if (value.toString().toIntOrNull() != null) {
                totalScore += value.toString().toInt()
            }
        }
        csv.append(headerRow.toString() + "Score" + "\n")
        csv.append(answerRow.toString() + totalScore)
        return csv
    }

    private fun submitCSV() {
        val csv = writeCSV()
        Log.d(TAG, "submitCSV: csv contents: $csv")
        val upload = CSVCreator(applicationContext)
        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        val preparedFile = upload.prepareFileUpload(csv, applicationContext, "gsq", ".csv")
        mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
    }

}
