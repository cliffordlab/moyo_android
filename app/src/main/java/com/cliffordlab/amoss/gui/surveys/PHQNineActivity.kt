package com.cliffordlab.amoss.gui.surveys

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.charts.graphData.GraphRealmRepo
import com.cliffordlab.amoss.helper.AmossAlarms
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.receivers.NotificationConstants
import com.cliffordlab.amoss.settings.SettingsUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_phqnine.*
import java.util.*

class PHQNineActivity : AppCompatActivity() {
    private val questions = arrayOf("Little interest or pleasure in doing things", "Feeling down, depressed, or hopeless", "Trouble falling or staying asleep, or sleeping too much", "Feeling tired or having little energy", "Poor appetite or overeating", "Feeling bad about yourself — or that you are a failure or\n" + "have let yourself or your family down", "Trouble concentrating on things, such as reading the\n" + "newspaper or watching television", "Moving or speaking so slowly that other people could have\n" +
            "noticed? Or the opposite — being so fidgety or restless\n" +
            "that you have been moving around a lot more than usual", "Thoughts that you would be better off dead or of hurting\n" + "yourself in some way")

    private var phq9Map: MutableMap<Int, Int>? = null
    private var mStringBuilder: StringBuilder? = null
    private var disposable: Disposable? = null

    private var levelGroups = listOf<RadioGroup>()
    private var totalScore = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phqnine)
        ButterKnife.bind(this)

        phq9Map = TreeMap()
        mStringBuilder = StringBuilder()
        levelGroups = listOf<RadioGroup>(level_group1, level_group2,
                level_group3, level_group4, level_group5, level_group6,
                level_group7, level_group8, level_group9)

        for (group in levelGroups) {
            group.setOnCheckedChangeListener { _, _ ->
                levelGroups.forEach { group ->
                    val isNotChecked = group.checkedRadioButtonId == -1
                    if (isNotChecked) {
                        submitPHQ9.text = "Submit Incomplete"
                        return@setOnCheckedChangeListener
                    }
                    submitPHQ9.text = "Submit"
                }
            }
        }
        submitPHQ9.setOnClickListener {
            submit()
        }
    }


    fun submit() {
        for ((index, level_group) in levelGroups. withIndex()) {
            val isNotChecked = level_group.checkedRadioButtonId == -1

            if (isNotChecked) {
                if (index < 8) {
                    phq9Map!!.put(index, -1)
                    continue
                }
                // when the 8th is not checked
                phq9Map!!.put(index, -1)
            }

            if (!isNotChecked) {
                val id = level_group.checkedRadioButtonId

                val answer = (findViewById<RadioButton>(id)).text.toString()
                var points = 0

                //so it does not pop in notifications
                when {
                    answer.startsWith("Several") -> points = 1
                    answer.startsWith("More") -> points = 2
                    answer.startsWith("Nearly") -> points = 3
                }

                phq9Map!![index] = points
                totalScore += points
            }

            val endOfGame = index == 8
            if (endOfGame) {
                //so it does not pop in notifications
                SettingsUtil.setPHQ9Completed(applicationContext, true)
                val util = SettingsUtil(applicationContext)
                util.setHasCompletedPHQ9(true)
                createPHQCSV()
                saveTotalScoreToRealm()
                val upload = CSVCreator(applicationContext)
                AmossNetwork.changeBaseURL(BuildConfig.apiBase)

                val preparedFile = upload.prepareFileUpload(mStringBuilder!!, applicationContext, "phq", ".csv")

                disposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })

                util.setHasCompletedPHQ9(true)

                resetTaskAvailabilityFollowingWeek(util)
                val alarms = AmossAlarms()
                alarms.dailyAlarms(applicationContext)
                val intent = Intent("weekly-broadcast")
                val pendingIntent = PendingIntent.getBroadcast(applicationContext, NotificationConstants.REQUEST_CODE_MOODZOOM, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.cancel(pendingIntent)
                finish()
            }
        }
    }

    private fun resetTaskAvailabilityFollowingWeek(util: SettingsUtil) {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                println("phq9 reset successful")

                util.setHasCompletedPHQ9(false)
                val alarms = AmossAlarms()
                alarms.weeklyAlarm(applicationContext)
            }
        }

        registerReceiver(br, IntentFilter("task-availability-weekly"))
        val mgr: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(), Intent("task-availability-weekly"), 0)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0) //14
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DATE, 7)
        mgr.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
    }

    private fun saveTotalScoreToRealm() {
        val graphRealmRepo = GraphRealmRepo()
        graphRealmRepo.savePHQ9ScoreToRealm(totalScore)
    }

    private fun createPHQCSV() {
        val it = phq9Map!!.entries.iterator()
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
        val name = "PHQ9 Survey"
        private const val TAG = "PHQNineActivity"
    }
}
