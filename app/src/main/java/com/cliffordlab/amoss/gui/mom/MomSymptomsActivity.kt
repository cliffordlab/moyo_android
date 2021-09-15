package com.cliffordlab.amoss.gui.mom

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.adapters.SurveyAdapter
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.models.SymptomsModel
import com.cliffordlab.amoss.network.AmossNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import okhttp3.MultipartBody
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MomSymptomsActivity : AppCompatActivity() {
    private var answerKey: StringBuilder = StringBuilder()
    private var mDisposable: Disposable? = null
    private lateinit var adapter: SurveyAdapter
    private lateinit var alertDialog: Dialog

    companion object {
        const val TAG = "MomSymptomsActivity"
        val name = "RECORD SYMPTOMS"
    }

    private var possibleResponseList = listOf(
            "Yes", "No"
    )

    private val questionsMap : List<Triple<String, List<String>, Int?>> = listOf(
            Triple("A pounding or throbbing pain in your head that does not go away with rest or medications.", possibleResponseList, R.drawable.headache),
            Triple("Seeing spots or flashing lights in front of your eyes, blurry vision, or sensitivity to light.", possibleResponseList, R.drawable.vision_changes),
            Triple("Pain under the ribs on your right side that may be accompanied by nausea or vomiting.", possibleResponseList, R.drawable.abdominal_pain),
            Triple("Difficulty breathing or a tightness in your chest." , possibleResponseList, R.drawable.short_breath)
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
        }
    }

    private fun submitCSV() {
        val csv = StringBuilder()
        val headerRow = StringBuilder()
        val answerRow = StringBuilder()
        val map = adapter.getSurveyResults()
        val answers = mutableListOf<Boolean>()
        val answerList: MutableList<String> = ArrayList()

        for ((_, value) in map) {
            if (value == 0) {
                answers.add(true)
                answerRow.append("TRUE, ")
                answerList.add("TRUE")
            } else {
                answers.add(false)
                answerRow.append("FALSE, ")
                answerList.add("FALSE")
            }
        }
        headerRow.append("Breathing, Headache, Vision, Pain")

        csv.append(headerRow.toString() + "\n")
        csv.append(answerRow.toString().substring(0, answerRow.length - 2))

        val upload = CSVCreator(applicationContext)

        val preparedFile = upload.prepareFileUpload(csv, applicationContext, "symptoms", ".csv")

        upload.amossPartList.add(MultipartBody.Part.createFormData("blurried_vision", answers[0].toString()))
        upload.amossPartList.add(MultipartBody.Part.createFormData("headache", answers[1].toString()))
        upload.amossPartList.add(MultipartBody.Part.createFormData("difficulty_breathing", answers[2].toString()))
        upload.amossPartList.add(MultipartBody.Part.createFormData("side_paid", answers[3].toString()))
        upload.amossPartList.add(MultipartBody.Part.createFormData("created_at", System.currentTimeMillis().toString().substring(1)))

        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        mDisposable = AmossNetwork.client.uploadSymptoms(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
        saveToRealm(answerList)
        if (answerList.contains("TRUE")) {
            showCallAlert()
        } else {
            super.onBackPressed()
        }
    }

    private fun saveToRealm(answerList: MutableList<String>) {
        val dateFormatted = getDateString()
        val symptomsStringBuilder = StringBuilder()
        for ((index, value) in answerList.withIndex()) {
            if (value == "TRUE") {
                when (index) {
                    0 -> {
                        symptomsStringBuilder.append("difficulty breathing, ")
                        answerKey.append("Breathing, ")
                    }
                    1 -> {
                        symptomsStringBuilder.append("severe headache, ")
                        answerKey.append("Headache, ")
                    }
                    2 -> {
                        symptomsStringBuilder.append("blurry vision, ")
                        answerKey.append("Vision, ")
                    }
                    3 -> {
                        symptomsStringBuilder.append("pain in ribs, ")
                        answerKey.append("Pain, ")
                    }
                }
            }
        }
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm1 ->
            val realmObject = realm1.createObject(SymptomsModel::class.java)
            if (symptomsStringBuilder.isNotEmpty()) {
                realmObject.symptoms = symptomsStringBuilder.toString().substring(0, symptomsStringBuilder.toString().length - 2)
            } else {
                realmObject.symptoms = "No symptoms reported"
            }
            realmObject.createdAt = dateFormatted
        }
        realm.close()
    }

    private fun getDateString(): String {
        val formatter: DateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        val milliSeconds = System.currentTimeMillis()
        val date = Date(milliSeconds)
        val dateFormatted = formatter.format(date)
        return dateFormatted
    }

    private fun showCallAlert() {
        val (descriptionDialogTextView, noButton, okButton) = getAlertDialog()
        descriptionDialogTextView.text = getString(R.string.mom_symptoms_warning_1) + answerKey.substring(0, answerKey.length-2)
        noButton.text = "Dismiss"
        okButton.text = "Call Grady Nurse"

        noButton.setOnClickListener { view ->
            alertDialog.dismiss()
            showDismissedAlert()
        }
        okButton.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:4046160600") //grady nurse hotline
            startActivity(intent)
            alertDialog.dismiss()
            super.onBackPressed()
        }

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun showDismissedAlert() {
        val (descriptionDialogTextView, accidentButton, mistakeButton) = getAlertDialog()
        val thirdButton = alertDialog.findViewById<Button>(R.id.btn3)
        thirdButton.visibility = VISIBLE

        descriptionDialogTextView.text = "Why did you click dismiss?"
        accidentButton.text = "I entered my symptoms incorrectly"
        mistakeButton.text = "I clicked dismiss by mistake"
        thirdButton.text = "I do not feel like talking"

        accidentButton.setOnClickListener { view ->
            alertDialog.dismiss()
            finish()
            val intent = Intent(this, MomSymptomsActivity::class.java)
            startActivity(intent)
        }

        mistakeButton.setOnClickListener { view ->
            alertDialog.dismiss()
            showCallAlert()
        }

        thirdButton.setOnClickListener { view ->
            alertDialog.dismiss()
            showReasonAlert()
        }

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun showReasonAlert() {
        val (descriptionDialogTextView, reason1Btn, reason2Btn) = getAlertDialog()
        val reason3Btn = alertDialog.findViewById<Button>(R.id.btn3)
        val reason4Btn = alertDialog.findViewById<Button>(R.id.btn4)
        reason3Btn.visibility = VISIBLE
        reason4Btn.visibility = VISIBLE

        descriptionDialogTextView.text = "Please select a reason"
        reason1Btn.text = "I would rather talk to my own doctor."
        reason2Btn.text = "I am not concerned by these symptoms."
        reason3Btn.text = "I do not like talking about my health on the phone."
        reason4Btn.text = "I did not find the Grady Nurse very useful last time."

        reason1Btn.setOnClickListener { view ->
            alertDialog.dismiss()
            val csv = StringBuilder()
            csv.append(reason1Btn.text)
            uploadReasonCSV(csv)
            finish()
        }

        reason2Btn.setOnClickListener { view ->
            alertDialog.dismiss()
            val csv = StringBuilder()
            csv.append(reason2Btn.text)
            uploadReasonCSV(csv)
            finish()
        }

        reason3Btn.setOnClickListener { view ->
            alertDialog.dismiss()
            val csv = StringBuilder()
            csv.append(reason3Btn.text)
            uploadReasonCSV(csv)
            finish()
        }

        reason4Btn.setOnClickListener { view ->
            alertDialog.dismiss()
            val csv = StringBuilder()
            csv.append(reason4Btn.text)
            uploadReasonCSV(csv)
            finish()
        }

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun uploadReasonCSV(csv: StringBuilder) {
        val upload = CSVCreator(applicationContext)

        val preparedFile =
            upload.prepareFileUpload(csv, applicationContext, "symptomsreasonswhy", ".csv")

        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        mDisposable =
            AmossNetwork.client.uploadSymptoms(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
    }

    private fun getAlertDialog(): Triple<TextView, Button, Button> {
        alertDialog = Dialog(this)
        alertDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.are_you_sure_dialog)
        alertDialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        val descriptionDialogTextView =
            alertDialog.findViewById<TextView>(R.id.descriptionDialogTextView)
        val noButton = alertDialog.findViewById<Button>(R.id.noButton)
        val okButton = alertDialog.findViewById<Button>(R.id.okButton)
        return Triple(descriptionDialogTextView, noButton, okButton)
    }
}
