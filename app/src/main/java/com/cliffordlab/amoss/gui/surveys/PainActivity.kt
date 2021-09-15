package com.cliffordlab.amoss.gui.surveys

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.R.drawable.*
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.helper.SurveyInits
import com.cliffordlab.amoss.network.AmossNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_pain.*


class PainActivity : AppCompatActivity() {
    val MIN_DISTANCE = 150
    var x1: Float = 0.0f
    var x2: Float = 0.0f
    var item = 0
    var mDisposable: Disposable? = null

    lateinit var painDrawables: MutableList<Int>

    companion object Title {
        val name = "Pain Scale"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pain)
        painDrawables = arrayListOf(
                no_hurt,
                hurts_little_bit,
                hurts_little_more,
                hurts_even_more,
                hurts_whole_lot,
                hurts_worst
        )
        swipeDirectionalPad()
        submitPain.setOnClickListener { submit() }
    }

    private fun submit() {
        var painString = StringBuilder()
        var currentPain = item * 2
        painString.append(currentPain)
        val upload = CSVCreator(applicationContext)
        var preparedFile = upload.prepareFileUpload(painString, applicationContext, "pain", ".csv")

        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })

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
                    surveys.setHasCompletedPainMeasurement(true)
                }
            }
        }
        realm.close()
        finish()
    }

    private fun swipeDirectionalPad() {
        painView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    //set x1 as X coordinate when finger pressed down
                    x1 = event.x
                MotionEvent.ACTION_UP -> {
                    //set x2 as  X coordinate when finger lifted up
                    x2 = event.x
                    //get delta of X
                    val deltaX = x2 - x1

                    //if absolute value of delta is greater than
                    //minimun distance which we set as counting as a swipe
                    //register a swipe
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        //swipe to the right if x2 point of finger being lifted up
                        //is greater than x1 point of finger being pressed down
                        if (x2 > x1) {
                            Log.i("touchpad", "left to right")
                            if (item == 5) {
                                item = 0
                            } else {
                                item += 1
                            }
                            painView.setImageResource(painDrawables[item])
                        } else {
                            Log.i("touchpad", "right to left")
                            if (item == 0) {
                                item = 5
                            } else {
                                item -= 1
                            }
                            painView.setImageResource(painDrawables[item])
                        }
                    }
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }
}
