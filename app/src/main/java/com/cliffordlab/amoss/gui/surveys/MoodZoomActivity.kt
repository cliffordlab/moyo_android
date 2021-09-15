package com.cliffordlab.amoss.gui.surveys

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.MainActivity
import com.cliffordlab.amoss.gui.charts.graphData.GraphRealmRepo
import com.cliffordlab.amoss.helper.AmossAlarms
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.helper.SurveyInits
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.receivers.NotificationConstants
import com.cliffordlab.amoss.settings.SettingsUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_moodzoom.*

class MoodZoomActivity : AppCompatActivity() {

    lateinit var mood_groups: Array<RadioGroup>
    lateinit var stress_groups: Array<CheckBox>

    private var moodZoomCSV: StringBuilder? = null
    private var mDisposable: Disposable? = null
    private var totalScore = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasNextSurvey = intent!!.getBooleanExtra(Constants.MULTISURVEYS, false)

        if (hasNextSurvey) {
            val realm = Realm.getDefaultInstance()

            val completedSurveysTable = realm.where(SurveyInits::class.java).findAll()

            if (completedSurveysTable.size > 0) {
                val surveys = realm.where(SurveyInits::class.java).findFirst()
                print(surveys)
                if (surveys != null) {
                    if (surveys.hasCompletedMoodZoom()) {
                        val intentMain = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intentMain)
                        realm.close()
                        return
                    }
                }
            }
            realm.close()
        }

        setContentView(R.layout.activity_moodzoom)
        ButterKnife.bind(this)
        moodZoomCSV = StringBuilder()

        val bar = supportActionBar
        bar?.title = name
        mood_groups = arrayOf<RadioGroup>(anxious_group, elated_group, sad_group, anxious_group, irritable_group, energetic_group)
        stress_groups = arrayOf<CheckBox>(radioStressCause_1, radioStressCause_2, radioStressCause_3, radioStressCause_4, radioStressCause_5, radioStressCause_6)

        buttonSubmit.setOnClickListener {
            submit()
        }
    }

    fun submit() {
        var mood_completed = true

        for (mood_group in mood_groups) {
            val id = mood_group.checkedRadioButtonId
            if (id == -1) {
                //does not allow use to submit unless moods are filled out
                //does not allow use to submit unless moods are filled out
                mood_completed = false
                break
            }
            //the tag of the radio button is set to equal its value
            val answerValue = findViewById<RadioButton>(id).tag.toString()
            totalScore = totalScore.plus(Integer.valueOf(findViewById<RadioButton>(id).tag.toString()))
            Log.i(TAG, "submit: totalScore $totalScore")
            moodZoomCSV!!.append(answerValue)
            moodZoomCSV!!.append(",")
        }

        val graphRealmRepo = GraphRealmRepo()
        graphRealmRepo.saveScoreToRealm("moodZoom", totalScore)

        stress_groups
                .filter { it.isChecked }
                .forEach { moodZoomCSV!!.append(it.tag.toString()) }

        if (mood_completed) {
            val upload = CSVCreator(applicationContext)
            val preparedFile = upload.prepareFileUpload(moodZoomCSV!!, applicationContext, "mz", ".csv")
            AmossNetwork.changeBaseURL(BuildConfig.apiBase)
            Log.i(TAG, "this is the Header: " + upload.amossHeaderMap)
            Log.i(TAG, "this is the part List: " + upload.amossPartList)
            //todo  upload may need to point to utsw
//            Toast.makeText(applicationContext, "Uploading...", Toast.LENGTH_LONG).show()

            mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })

            val hasNextSurveyHF = intent.getBooleanExtra(Constants.MULTISURVEYSHF, false)

            if (!hasNextSurveyHF) {
                Realm.init(applicationContext)
                // Get a Realm instance for this thread
                val config = RealmConfiguration.Builder()
                        .name("amoss.realm")
                        .deleteRealmIfMigrationNeeded()
                        .build()
                val realm = Realm.getInstance(config)
                val completedSurveysTable = realm.where(SurveyInits::class.java).findAll()
                if (completedSurveysTable.size > 0) {
                    realm.executeTransaction { realmUpdate ->
                        val surveys = realmUpdate.where(SurveyInits::class.java).findFirst()
                        if (surveys != null) {
                            surveys.setHasCompletedMoodZoom(true)
                        }
                    }
                }
                realm.close()
            }
            // set pref to say mood zoom complete
            val util = SettingsUtil(applicationContext)
            util.setHasCompletedZoom(true)

            val intent = Intent("daily-broadcast")
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, NotificationConstants.REQUEST_CODE_MOODZOOM, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pendingIntent)

//            resetTaskAvailabilityFollowingDay(util)
            val alarms = AmossAlarms()
            alarms.dailyAlarms(applicationContext)

            finish()
        } else {
            Toast.makeText(this, "Please complete the survey.", Toast.LENGTH_LONG).show()
        }
    }
//
//    private fun resetTaskAvailabilityFollowingDay(util: SettingsUtil) {
//        val br = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                println("mood zoom reset successful")
//
//                util.setHasCompletedZoom(false)
//                val alarms = AmossAlarms()
//                alarms.moodZoomAlarm(applicationContext)
//            }
//        }
//
//        registerReceiver(br, IntentFilter("task-availability-mz"))
//        val mgr: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val pi = PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(), Intent("task-availability-mz"), 0);
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, 1) //0
//        calendar.set(Calendar.MINUTE, 59)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.add(Calendar.DAY_OF_MONTH, 1)
//        mgr.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
//    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }

    companion object Title {
        val name = "Mood Zoom"
        private const val TAG = "MoodZoomActivity"
    }
}
