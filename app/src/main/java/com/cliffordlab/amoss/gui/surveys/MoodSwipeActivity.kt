package com.cliffordlab.amoss.gui.surveys


import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.app.AmossApplication
import com.cliffordlab.amoss.gui.charts.graphData.GraphRealmRepo
import com.cliffordlab.amoss.helper.AmossAlarms
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.helper.SurveyInits
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.receivers.NotificationConstants
import com.cliffordlab.amoss.settings.SettingsUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_moodswipe.*

class MoodSwipeActivity : AppCompatActivity() {
    var value = 3

    private var moodSwipeCSV: StringBuilder? = null
    private var mDisposable: Disposable? = null
    private var hasNextSurvey = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moodswipe)
        val bar = supportActionBar
        if (bar != null) bar.title = name

        moodSwipeCSV = StringBuilder()
        moodSwipeCSV!!.append("Mood\n")

        button.setOnClickListener({ _ -> submit()})
        val onTouchListener = View.OnTouchListener { _, event -> setMood(event)}
        smileyImageView.setOnTouchListener(onTouchListener)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        hasNextSurvey = intent!!.getBooleanExtra(Constants.MULTISURVEYS, false)
        println("on new intent has multiple: $hasNextSurvey")
        println("Did receive intent multiple surveys: " + hasNextSurvey)
        if (hasNextSurvey) {
            // Initialize Realm
            val realm = Realm.getDefaultInstance()

            // Query Realm for all dogs younger than 2 years old
            val completedSurveysTable = realm.where(SurveyInits::class.java).findAll()

            println("size of table: " + completedSurveysTable.size)
            if (completedSurveysTable.size > 0) {
                val surveys = realm.where(SurveyInits::class.java).findFirst()
                println(surveys)
                if (surveys != null) {
                    if (surveys.hasCompletedMoodSwipe()) {
                        val intentPromis = Intent(applicationContext, MoodZoomActivity::class.java)
                        intentPromis.putExtra(Constants.MULTISURVEYS, true)
                        startActivity(intentPromis)
                        realm.close()
                        return
                    }
                }
            }
            realm.close()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SettingsUtil.isAppUpdated(applicationContext)) {
            val amountOfJobsCancelled = (this.application as AmossApplication).cancelAllJobs()
            print("There were ")
            print(amountOfJobsCancelled)
            println(" jobs cancelled")
        }
//        if (SettingsUtil.isDataCollected(this)) {
//            val serviceRunning = isMyServiceRunning(AccelService::class.java)
//            if (!serviceRunning) {
//                startService(Intent(this, AccelService::class.java))
//            }
//        }
        (this.application as AmossApplication).scheduleAllJobs()
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { serviceClass.name == it.service.className }
    }

    private fun submit() {
        Log.i("network", value.toString() + "")
        moodSwipeCSV!!.append(value.toString() + "\n")

        val upload = CSVCreator(applicationContext)
        val preparedFile = upload.prepareFileUpload(moodSwipeCSV!!, applicationContext, "ms", ".csv")
        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })

        Realm.init(applicationContext)

        val config = RealmConfiguration.Builder()
                .name("amoss.realm")
                .deleteRealmIfMigrationNeeded()
                .build()
        val realm = Realm.getInstance(config)
        val completedSurveysTable = realm.where(SurveyInits::class.java).findAll()
        if (completedSurveysTable.size > 0) {
            realm.executeTransaction { realmUpdate ->
                val surveys = realmUpdate.where(SurveyInits::class.java).findFirst()
                surveys?.setHasCompletedMoodSwipe(true)
            }
        }
        realm.close()

        val graphRealmRepo = GraphRealmRepo()
        graphRealmRepo.saveScoreToRealm("moodSwipe", value)

        val util = SettingsUtil(applicationContext)
        util.setHasCompletedSwipe(true)

        val alarms = AmossAlarms()
        alarms.dailyAlarms(applicationContext)

        val intent = Intent("daily-broadcast")
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, NotificationConstants.REQUEST_CODE_MOODZOOM, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)
        finish()
    }

    private fun setMood(event: MotionEvent): Boolean {
        val y: Float
        val percent20 = smileyImageView.y / 5
        val percent40 = 2 * smileyImageView.y / 5
        val percent60 = 3 * smileyImageView.y / 5
        val percent80 = 4 * smileyImageView.y / 5
        try {
            y = event.getY(event.getPointerId(0))

            when (event.action) {
                MotionEvent.ACTION_MOVE -> if (y <= percent20) {
                    smileyImageView.setImageResource(R.drawable.excited)
                    value = 5
                } else if (y > percent20 && y <= percent40) {
                    smileyImageView.setImageResource(R.drawable.happy)
                    value = 4
                } else if (y > percent40 && y <= percent60) {
                    smileyImageView.setImageResource(R.drawable.neutral)
                    value = 3
                } else if (y > percent60 && y <= percent80) {
                    smileyImageView.setImageResource(R.drawable.sad)
                    value = 2
                } else {
                    smileyImageView.setImageResource(R.drawable.angry)
                    value = 1
                }
                MotionEvent.ACTION_UP -> if (y <= percent20) {
                    smileyImageView.setImageResource(R.drawable.excited)
                    value = 5
                } else if (y > percent20 && y <= percent40) {
                    smileyImageView.setImageResource(R.drawable.happy)
                    value = 4
                } else if (y > percent40 && y <= percent60) {
                    smileyImageView.setImageResource(R.drawable.neutral)
                    value = 3
                } else if (y > percent60 && y <= percent80) {
                    smileyImageView.setImageResource(R.drawable.sad)
                    value = 2
                } else {
                    smileyImageView.setImageResource(R.drawable.angry)
                    value = 1
                }
            }
            return true
        } catch (e: IllegalArgumentException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            e.printStackTrace()
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }

    override fun onBackPressed() {}

    companion object Title {
        val name = "Mood Swipe"
    }
}

