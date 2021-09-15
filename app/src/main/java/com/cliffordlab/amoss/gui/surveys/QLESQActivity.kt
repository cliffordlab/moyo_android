package com.cliffordlab.amoss.gui.surveys

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.BuildConfig
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
import kotlinx.android.synthetic.main.activity_qlesq.*

class QLESQActivity : AppCompatActivity() {

    companion object Title {
        val name = "Q LES Q"
    }

    var mDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasNextSurvey = intent!!.getBooleanExtra(Constants.MULTISURVEYS, false)

        if (hasNextSurvey) {
            // Initialize Realm
            val realm = Realm.getDefaultInstance()

            // Query Realm for all dogs younger than 2 years old
            val completedSurveysTable = realm.where(SurveyInits::class.java).findAll()

            if (completedSurveysTable.size > 0) {
                val surveys = realm.where(SurveyInits::class.java).findFirst()
                println(surveys)
                if (surveys != null) {
                    if (surveys.hasCompletedQLesQ()) {
                        val intentPain = Intent(applicationContext, PainActivity::class.java)
                        intentPain.putExtra(Constants.MULTISURVEYS, true)
                        startActivity(intentPain)
                        realm.close()
                        return
                    }
                }
            }
            realm.close()
        }

        setContentView(R.layout.activity_qlesq)

        submitQlesq.setOnClickListener({ _ -> submit() })
    }

    private fun submit() {
        val qlesqSB = StringBuilder()
        val id = qlesqRadioGroup1.checkedRadioButtonId
        if (id == -1) {
            //does not allow use to submit unless moods are filled out
            Toast.makeText(this, "Please complete the survey.", Toast.LENGTH_LONG).show()
            return
        }

        qlesqSB.append(findViewById<RadioButton>(id).tag.toString())


        val upload = CSVCreator(applicationContext)
        val preparedFile = upload.prepareFileUpload(qlesqSB, applicationContext, "qlesq", ".csv")
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
                    surveys.setHasCompletedQLesQ(true)
                }
            }
        }
        realm.close()

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }
}
