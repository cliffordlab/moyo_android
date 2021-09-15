package com.cliffordlab.amoss.gui.surveys

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.helper.SurveyInits
import com.cliffordlab.amoss.network.AmossNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_fatigue.*


class PROMISActivity : AppCompatActivity() {
    lateinit var mood_groups: MutableList<RadioGroup>

    private var promisCSV: StringBuilder? = null
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasNextSurvey = intent!!.getBooleanExtra(Constants.MULTISURVEYS, false)
        println("Promis7 has next survey: " + hasNextSurvey)
        if (hasNextSurvey) {
            val realm = Realm.getDefaultInstance()

            // Query Realm for all dogs younger than 2 years old
            val completedSurveysTable = realm.where(SurveyInits::class.java).findAll()

            if (completedSurveysTable.size > 0) {
                val surveys = realm.where(SurveyInits::class.java).findFirst()
                println(surveys)
                if (surveys != null) {
                    if (surveys.hasCompletedPromis()) {
                        val intentQlesQ = Intent(applicationContext, QLESQActivity::class.java)
                        intentQlesQ.putExtra(Constants.MULTISURVEYS, true)
                        startActivity(intentQlesQ)
                        realm.close()
                        return
                    }
                }
            }

            realm.close()
        }

        setContentView(R.layout.activity_fatigue)
        ButterKnife.bind(this)
        mood_groups = arrayListOf<RadioGroup>(q1, elated_group, sad_group, sad_group2, angry_group, irritable_group, energetic_group)
        promisCSV = StringBuilder()

        buttonSubmitFatigue.setOnClickListener({ _ -> submitSurvey()})
    }

    private fun submitSurvey() {
        var promis_completed = true

        for (mood_group in mood_groups) {
            val id = mood_group.checkedRadioButtonId
            if (id == -1) {
                //does not allow use to submit unless moods are filled out
                promis_completed = false
                break
            }
            //the tag of the radio button is set to equal its value
            val answerValue = findViewById<RadioButton>(id).tag.toString()
            promisCSV!!.append(answerValue)

            if (mood_group !== mood_groups[mood_groups.size - 1]) {
                promisCSV!!.append(",")
            }
        }

        if (promis_completed) {
            val upload = CSVCreator(applicationContext)
            val preparedFile = upload.prepareFileUpload(promisCSV!!, applicationContext, "promis", ".csv")

            disposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
        } else {
            Toast.makeText(this, "Please complete the survey.", Toast.LENGTH_LONG).show()
            return
        }

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
                    surveys.setHasCompletedPromis(true)
                }
            }
        }
        realm.close()

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null && !disposable!!.isDisposed) {
            disposable!!.dispose()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    companion object Title {
        val name = "Promise Survey"
    }
}
