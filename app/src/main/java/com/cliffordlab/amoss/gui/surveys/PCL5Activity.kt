package com.cliffordlab.amoss.gui.surveys

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.MainActivity
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.settings.SettingsUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_pcl5.*
import java.util.*

class PCL5Activity : AppCompatActivity() {
    private var pcl5Map: MutableMap<Int, Int>? = null
    private var mStringBuilder: StringBuilder? = null
    private var disposable: Disposable? = null

    private var levelGroups = listOf<RadioGroup>()
    private var totalScore = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pcl5)
        pcl5Map = TreeMap()
        mStringBuilder = StringBuilder()
        levelGroups = listOf<RadioGroup>(level_group1, level_group2,
                level_group3, level_group4)

        for (group in levelGroups) {
            group.setOnCheckedChangeListener { _, _ ->
                levelGroups.forEach { group ->
                    val isNotChecked = group.checkedRadioButtonId == -1
                    if (isNotChecked) {
                        submitPCL5.text = "Submit Incomplete"
                        return@setOnCheckedChangeListener
                    }
                    submitPCL5.text = "Submit"
                }
            }
        }
        submitPCL5.setOnClickListener {
            next()
        }
    }

    fun next() {
        for ((index, level_group) in levelGroups. withIndex()) {
            val isNotChecked = level_group.checkedRadioButtonId == -1

            if (isNotChecked) {
                if (index < 3) {
                    pcl5Map!!.put(index, -1)
                    continue
                }
                // when the 8th is not checked
                pcl5Map!!.put(index, -1)
            }

            if (!isNotChecked) {
                val id = level_group.checkedRadioButtonId

                val answer = (findViewById<RadioButton>(id)).text.toString()
                var points = 0

                //so it does not pop in notifications
                when {
                    answer.startsWith("A") -> points = 1
                    answer.startsWith("Moderately") -> points = 2
                    answer.startsWith("Quite") -> points = 3
                    answer.startsWith("Extremely") -> points = 4
                }

                pcl5Map!![index] = points
                totalScore += points
            }

            val endOfGame = index == 3
            if (endOfGame) {
                //so it does not pop in notifications
                SettingsUtil.setPCL5Completed(applicationContext, true)
                createPCLCSV()
                val upload = CSVCreator(applicationContext)
                AmossNetwork.changeBaseURL(BuildConfig.apiBase)

                val preparedFile = upload.prepareFileUpload(mStringBuilder!!, applicationContext, "pcl5", ".csv")
//                Toast.makeText(applicationContext, "Uploading...", Toast.LENGTH_LONG).show()

                disposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })

                val hasNextSurveyHF = intent.getBooleanExtra(Constants.MULTISURVEYSHF, false)

                if (hasNextSurveyHF) {
                    val intentKccq = Intent(applicationContext, KCCQActivity::class.java)
                    intentKccq.putExtra(Constants.MULTISURVEYSHF, true)
                    startActivity(intentKccq)
                } else {
                    val intentMain = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intentMain)
                }
                return
            }
        }
    }

    private fun createPCLCSV() {
        val it = pcl5Map!!.entries.iterator()
        mStringBuilder!!.append("question,answer\n")
        while (it.hasNext()) {
            val pair = it.next()
            mStringBuilder!!.append(pair.key)
            mStringBuilder!!.append(",")
            mStringBuilder!!.append(pair.value)
            mStringBuilder!!.append("\n")
            it.remove() // avoids a ConcurrentModificationException
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null && !disposable!!.isDisposed) {
            disposable!!.dispose()
        }
    }

    companion object Title {
        val name = "PCL5"
        private const val TAG = "PCL5Activity"
    }
}
