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

class GAD7Activity: AppCompatActivity() {
    private var mDisposable: Disposable? = null
    private lateinit var adapter: SurveyAdapter

    companion object {
        const val TAG = "GAD7Activity"
    }

    private var possibleResponseList = listOf(
            "Not at all", "Several Days", "More of than half the days", "Nearly everyday"
    )

    private val questionsMap : List<Triple<String, List<String>, Int?>> = listOf(
            Triple("Feeling nervous anxious or on edge?", possibleResponseList, null),
            Triple("Not being able to stop or control worrying?", possibleResponseList, null),
            Triple("Worrying too much about different things?", possibleResponseList, null),
            Triple("Trouble relaxing?" , possibleResponseList, null),
            Triple("Being so restless that it is hard to sit still?" , possibleResponseList, null),
            Triple("Becoming easily annoyed or irritable?" , possibleResponseList, null),
            Triple("Feeling afraid as if something awful might happen?" , possibleResponseList, null)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gad7)

        // set up the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.gad7RecyclerView)
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
        val preparedFile = upload.prepareFileUpload(csv, applicationContext, "gad7", ".csv")
        mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
    }

}
