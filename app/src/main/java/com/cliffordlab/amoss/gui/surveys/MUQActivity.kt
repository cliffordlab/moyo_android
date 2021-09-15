package com.cliffordlab.amoss.gui.surveys

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.models.RxNormResponse
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.helper.CSVCreator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_muq.*
import okhttp3.OkHttpClient


class MUQActivity : AppCompatActivity() {
    private lateinit var currentSelectedRadioButton: RadioButton
    private lateinit var acTextView: AutoCompleteTextView
    private var mDisposable: Disposable? = null
    private var adapter: ArrayAdapter<String>? = null
    var client = OkHttpClient()
    private var isAfterFirstMedicationQuestion = false
    private var answerToFirstQuestion = 0
    private var answerToMedicationQuestion: Int = 0
    private var medications: MutableList<String> = mutableListOf()
    private var medicationsForgotten: MutableList<String> = mutableListOf()
    private var timesForgottenMedications: MutableList<Int> = mutableListOf()
    private var isAnswerSelected = false

    companion object {
        private const val TAG = "MUQActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muq)
        ButterKnife.bind(this)
        acTextView = findViewById<View>(R.id.rxEditField) as AutoCompleteTextView
        acTextView.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count >= 3) {
                    getMedications(s)
                }
                toggleButtons()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })
        acTextView.onItemClickListener = OnItemClickListener { arg0, arg1, arg2, arg3 ->
            val inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(arg1.applicationWindowToken, 0)
            toggleButtons()
        }
        acTextView.threshold = 3
    }

    private fun getMedications(searchMedicationSpelling: Any) {
        val url = "https://rxnav.nlm.nih.gov/REST/"
        AmossNetwork.changeBaseURL(url)
        mDisposable = AmossNetwork.client.getRXSpellingSuggestions(searchMedicationSpelling.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<RxNormResponse> { this.handleResponse(it) }, Consumer<Throwable> { this.handleError(it) })

    }

    private fun handleError(e: Throwable) {
        e.printStackTrace()
    }

    private fun handleResponse(response: RxNormResponse) {
        val suggestions = response.suggestionGroup?.suggestionList?.suggestion
        runOnUiThread {
            medications.removeAll(medications)
            adapter?.clear()
            if (suggestions != null) {
                for (name in suggestions) {
                    medications.add(name)
                }
            }
            adapter = medications.let {
                ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, it)
            }
            acTextView.setAdapter(adapter)
            adapter?.notifyDataSetChanged()
        }
    }

    fun addMedication(view: View) {
        if (view is Button) {
            submitBtn.visibility = VISIBLE
            addMedicationLayout.visibility = VISIBLE
            firstQuestionLayout.visibility = GONE
            isAnswerSelected = false
            if (isAfterFirstMedicationQuestion) {
                timesForgottenMedications.add(answerToMedicationQuestion)
                medicationsForgotten.add(acTextView.text.toString())
            } else {
                isAfterFirstMedicationQuestion = true
            }
            adapter?.clear()
            acTextView.setText("")
            acTextView.hint = "Please enter other medicine name"
            currentSelectedRadioButton.isChecked = false
            submitBtn.isEnabled = false
            addBtn.isEnabled = false
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            if (firstQuestionLayout.visibility == VISIBLE) {
                // Check which radio button was clicked
                when (view.getId()) {
                    R.id.levelGroup1_0 ->
                        if (checked) {
                            answerToFirstQuestion = 0
                            submitBtn.visibility = VISIBLE
                            submitBtn.isEnabled = true
                            addBtn.isEnabled = false
                        }
                    R.id.levelGroup1_1 ->
                        if (checked) {
                            answerToFirstQuestion = 1
                            submitBtn.isEnabled = false
                            addBtn.visibility = VISIBLE
                            addBtn.isEnabled = true
                        }
                    R.id.levelGroup1_2 ->
                        if (checked) {
                            answerToFirstQuestion = 2
                            submitBtn.isEnabled = false
                            addBtn.visibility = VISIBLE
                            addBtn.isEnabled = true
                        }
                    R.id.levelGroup1_3 ->
                        if (checked) {
                            answerToFirstQuestion = 3
                            submitBtn.isEnabled = false
                            addBtn.visibility = VISIBLE
                            addBtn.isEnabled = true
                        }
                }
                currentSelectedRadioButton = findViewById<View>(view.id) as RadioButton
            }
        }
    }

    fun onMedicationRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked
            // Check which radio button was clicked
            when (view.getId()) {
                R.id.levelGroup2_0 ->
                    if (checked) {
                        answerToMedicationQuestion = 0
                    }
                R.id.levelGroup2_1 ->
                    if (checked) {
                        answerToMedicationQuestion = 1
                    }
                R.id.levelGroup2_2 ->
                    if (checked) {
                        answerToMedicationQuestion = 2
                    }
                R.id.levelGroup2_3 ->
                    if (checked) {
                        answerToMedicationQuestion = 3
                    }
            }
            isAnswerSelected = view.isChecked
            currentSelectedRadioButton = findViewById<View>(view.id) as RadioButton
            toggleButtons()
        }
    }

    fun toggleButtons() {
        if (acTextView.text.isNotEmpty() && isAnswerSelected) {
            submitBtn.isEnabled = true
            addBtn.isEnabled = true
        } else {
            submitBtn.isEnabled = false
            addBtn.isEnabled = false
        }
    }

    fun onSubmitBtnPressed(view: View) {
        if (view is Button) {
            timesForgottenMedications.add(answerToMedicationQuestion)
            medicationsForgotten.add(acTextView.text.toString())
            endSurvey()
        }
    }

    private fun endSurvey() {
        uploadCSV()
        finish()
    }

    private fun uploadCSV() {
        val csvContents = StringBuilder()
        val headerRow = StringBuilder()
        val answerRow = StringBuilder()
        headerRow.append("Over the past two weeks how many times did you forget to take your medication?")
        answerRow.append(answerToFirstQuestion)

        if (answerToFirstQuestion > 0) {
            headerRow.append(", ")
            answerRow.append(", ")
            for (i in medicationsForgotten.indices) {
                if (i == medicationsForgotten.size - 1) {
                    headerRow.append(medicationsForgotten[i])
                    answerRow.append(timesForgottenMedications[i].toString())
                } else {
                    headerRow.append(medicationsForgotten[i] + ", ")
                    answerRow.append(timesForgottenMedications[i].toString() + ", ")
                }
            }
        }
        csvContents.append(headerRow.toString() + "\n")
        csvContents.append(answerRow)

        val upload = CSVCreator(applicationContext)
        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        val preparedFile = upload.prepareFileUpload(csvContents, applicationContext, "muq", ".csv")
        Log.d(TAG, "uploadCSV: $csvContents")
        Log.d(TAG, "uploadCSV: $preparedFile")
        mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
    }
}
