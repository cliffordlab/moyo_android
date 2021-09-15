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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.helper.AmossAlarms
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.receivers.NotificationConstants
import com.cliffordlab.amoss.settings.SettingsUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_kccq.*
import java.util.*

class KCCQActivity : AppCompatActivity() {
    companion object Title {
        val name = "KCCQ"
    }

    lateinit var radioGroupList: ArrayList<RadioGroup>

    var mDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kccq)
        radioGroupList = arrayListOf(
                radioGroup1a, radioGroup1b, radioGroup1c,
                radioGroup2, radioGroup3, radioGroup4, radioGroup5, radioGroup6, radioGroup7,
                radioGroup8a, radioGroup8b, radioGroup8c
        )

        submitKCCQ.setOnClickListener({ _ -> submit() })
    }


    private fun submit() {
        //does not allow use to submit unless survey questions are filled out
        val kccqCSV = StringBuilder()
        kccqCSV.append("1,")
        for ((index, value) in radioGroupList.withIndex()) {
            val id = value.checkedRadioButtonId
            if (id == -1) {
                //does not allow use to submit unless moods are filled out
                Toast.makeText(this, "Please complete the survey.", Toast.LENGTH_LONG).show()
                return
            }

            //index less than 3 are all questions one input
            //get values from tag which correspond to the answer key for the question
            if (index < 3) {
                kccqCSV.append(findViewById<RadioButton>(id).tag.toString())
                continue
            }

            if (index == 9) {
                kccqCSV.append("\n8,")
                kccqCSV.append(findViewById<RadioButton>(id).tag.toString())
                continue
            }

            if (index > 9) {
                kccqCSV.append(findViewById<RadioButton>(id).tag.toString())
                continue
            }

            kccqCSV.append("\n")
            kccqCSV.append(index - 1)
            kccqCSV.append(",")
            kccqCSV.append(findViewById<RadioButton>(id).tag.toString())
        }

        val util = SettingsUtil(applicationContext)
        util.setHasCompletedKCCQ(true)

        val upload = CSVCreator(applicationContext)
        val preparedFile = upload.prepareFileUpload(kccqCSV, applicationContext, "kccq", ".csv")
        AmossNetwork.changeBaseURL(BuildConfig.apiBase)

        mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })

        resetTaskAvailabilityFollowingWeek(util)

        val alarms = AmossAlarms()
        alarms.weeklyAlarm(applicationContext)
        val intent = Intent("weekly-broadcast")
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, NotificationConstants.REQUEST_CODE_MOODZOOM, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        finish()
    }
    private fun resetTaskAvailabilityFollowingWeek(util: SettingsUtil) {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                println("kccq reset successful")
                util.setHasCompletedKCCQ(false)
                val alarms = AmossAlarms()
                alarms.weeklyAlarm(applicationContext)
            }
        }

        registerReceiver(br, IntentFilter("task-availability-kccq"))
        val mgr: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(), Intent("task-availability-kccq"), 0)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0) //14
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DATE, 7)
        mgr.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }
}
