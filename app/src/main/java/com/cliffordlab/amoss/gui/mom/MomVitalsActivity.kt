package com.cliffordlab.amoss.gui.mom

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.vitals.VitalsActivity
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.models.BPModel
import com.cliffordlab.amoss.network.AmossNetwork
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.vitals_activity.*
import okhttp3.MultipartBody
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MomVitalsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MomVitalsActivity"
        val name = "RECORD VITALS"
    }

    private var hasSubmittedRightArm: Boolean = false
    private var hasSubmittedLeft: Boolean = false
    private lateinit var alertDialog: Dialog
    private lateinit var submitRightBtn: View
    private lateinit var submitLeftBtn: View
    private lateinit var mCurrentSubmitBtn: View
    private lateinit var btnColorIndicationTextView: TextView
    private var fifteenMinuteAlertTriggered: Boolean = false
    private lateinit var imageView: View
    private val REQUEST_TAKE_PHOTO = 1
    private var mDisposable: Disposable? = null
    private var mCurrentPhotoPath: String? = null
    private var photoFile: File? = null
    private var photoTaken: Boolean = false
    private var isSPNumberPickerClicked: Boolean = false
    private var isDPNumberPickerClicked: Boolean = false
    private var isPRNumberPickerClicked: Boolean = false
    private lateinit var descriptionDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vitals_activity)
        imageView = findViewById(R.id.imageView3)
        val submitBtn = findViewById<View>(R.id.submitBP)
        submitLeftBtn = findViewById(R.id.submitLeftArmBPBtn)
        submitRightBtn = findViewById(R.id.submitRightArmBPBtn)
        btnColorIndicationTextView = findViewById(R.id.btnColorIndicationTextView)
        btnColorIndicationTextView.visibility = VISIBLE
        submitBtn.visibility = GONE
        submitLeftBtn.visibility = VISIBLE
        submitRightBtn.visibility = VISIBLE
        setNumberPicker()
        snapBPPhotoButton.setOnClickListener { dispatchTakePictureIntent() }
        submitBP.setOnClickListener {
            sendBPInfo(submitRightBtn)
        }
        submitLeftBtn.setOnClickListener {
            sendBPInfo(submitLeftBtn)
        }
        submitRightBtn.setOnClickListener {
            sendBPInfo(submitRightBtn)
        }
        alertDialog = Dialog(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic()
            photoTaken = true
        }
    }

    private fun uploadFile() {
        val csv = StringBuilder()
        val headerRow = StringBuilder()
        val answerRow = StringBuilder()
        headerRow.append("SBP, DBP, Pulse")
        answerRow.append(sPNumberPicker.value.toString() + ", ")
        answerRow.append(dPNumberPicker.value.toString() + ", ")
        answerRow.append(pRNumberPicker.value.toString())
        csv.append(headerRow.toString() + "\n")
        csv.append(answerRow.toString())

        val upload = CSVCreator(applicationContext)
        val preparedFile = upload.prepareFileUpload(csv, photoFile, applicationContext, "vitals")

        upload.amossPartList.add(MultipartBody.Part.createFormData("sbp", sPNumberPicker.value.toString()))
        upload.amossPartList.add(MultipartBody.Part.createFormData("dbp", dPNumberPicker.value.toString()))
        upload.amossPartList.add(MultipartBody.Part.createFormData("pulse", pRNumberPicker.value.toString()))
        upload.amossPartList.add(MultipartBody.Part.createFormData("created_at", System.currentTimeMillis().toString().substring(1)))

        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        mDisposable = AmossNetwork.client.uploadVitals(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })

        saveToRealm()

        when (mCurrentSubmitBtn) {
            submitLeftBtn -> {
                hasSubmittedLeft = true
                submitLeftBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.common_google_signin_btn_text_light_disabled))
            }
            submitRightBtn -> {
                hasSubmittedRightArm = true
                submitRightBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.common_google_signin_btn_text_light_disabled))
            }
        }

        if (hasSubmittedLeft && hasSubmittedRightArm && !fifteenMinuteAlertTriggered) {
            if (!alertDialog.isShowing) {
                super.onBackPressed()
            }
        } else {
            reset()
            sPNumberPicker.value = 120
            dPNumberPicker.value = 80
            pRNumberPicker.value = 50
            imageView3.setImageBitmap(null)
        }
    }

    private fun saveToRealm() {
        val dateFormatted = getDateString()

        val armReported: String = if (mCurrentSubmitBtn == submitRightBtn) {
            "Right Arm Vitals Reported"
        } else {
            "Left Arm Vitals Reported"
        }

        val vitalsString = "Systolic: " + sPNumberPicker.value.toString() + ", Diastolic: " +  dPNumberPicker.value.toString() + ", Pulse: " + pRNumberPicker.value.toString()

        val realm = Realm.getDefaultInstance()

        realm.executeTransaction { realm1 ->
            val realmObject = realm1.createObject(BPModel::class.java)
            realmObject.arm = armReported
            realmObject.vitals = vitalsString
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

    private fun sendBPInfo(submitBtn: View) {
        mCurrentSubmitBtn = submitBtn
        Log.i(VitalsActivity.TAG, "SP = ${sPNumberPicker.value}, PR = ${pRNumberPicker.value}, DP = ${dPNumberPicker.value}" )
        if (!isNumberPickerDashes()) {
            checkThreshold()
        }
        when {
            (isNumberPickerDashes()) && !photoTaken -> showAlertDialog(1)
            (isNumberPickerDashes()) && photoTaken -> showAlertDialog(2)
            (!isNumberPickerDashes()) && !photoTaken -> showAlertDialog(3)
            else -> {
                submitBtn.isEnabled = false
                uploadFile()
            }
        }
    }

    private fun checkThreshold() {
        //systolic BP >160 mm Hg or diastolic BP>110 mm Hg

        if (sPNumberPicker.value > 160 || dPNumberPicker.value > 110) {
            if (fifteenMinuteAlertTriggered) {
                showCallAlert(4)
            } else {
                showCallAlert(3)
            }
            return
        }
        if (sPNumberPicker.value < 100 || dPNumberPicker.value <  70) {
            showCallAlert(2)
            return
        }
        if (sPNumberPicker.value in 140..160  || dPNumberPicker.value in 90..110) {
            showCallAlert(1)
            return
        }
    }

    private fun isNumberPickerDashes(): Boolean {
        if (sPNumberPicker.value == 29 || pRNumberPicker.value == 29 || dPNumberPicker.value == 29) {
            return true
        }
        return false
    }

    private fun setNumberPicker() {
        sPNumberPicker.minValue = 29
        sPNumberPicker.maxValue = 299
        pRNumberPicker.minValue = 29
        pRNumberPicker.maxValue = 299
        dPNumberPicker.minValue = 29
        dPNumberPicker.maxValue = 299
        val npList = arrayOfNulls<String>(271)
        var currentNumber = 30
        for (i in 0..270) {
            if (i == 0) {
                npList[i] = "-"
            } else {
                npList[i] = currentNumber++.toString()
            }
        }

        sPNumberPicker.displayedValues = npList
        pRNumberPicker.displayedValues = npList
        dPNumberPicker.displayedValues = npList

        sPNumberPicker.value = 120
        dPNumberPicker.value = 80
        pRNumberPicker.value = 50

        sPNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            isSPNumberPickerClicked = true
        }
        pRNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            isPRNumberPickerClicked = true
        }
        dPNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            isDPNumberPickerClicked = true
        }
    }

    private fun showAlertDialog(message: Int) {
        when (message) {
            1 -> showDescriptionDialog("No Photo.\n Please take a picture of your vitals.")
            2 -> showDescriptionDialog("Please select vital numbers.")
            3 -> showDescriptionDialog("Please take photo of your vitals.")
        }
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    createImageFile()
                } catch (ex: IOException) {
                    FirebaseCrashlytics.getInstance().recordException(ex)
                    // Error occurred while creating the File
                    return ex.printStackTrace()
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.android.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".png", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun setPic() {
        imageView3.background = null
        Picasso.get().load("file:$mCurrentPhotoPath").into(imageView3)
    }


    private fun showDescriptionDialog(message: String) {
        descriptionDialog = Dialog(this)
        descriptionDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        descriptionDialog.setContentView(R.layout.description_dialog)
        descriptionDialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        val descriptionDialogTextView = descriptionDialog.findViewById<TextView>(R.id.descriptionDialogTextView)

        descriptionDialogTextView.text = message


        val noButton = descriptionDialog.findViewById<Button>(R.id.noButton)
        val whyButton = descriptionDialog.findViewById<Button>(R.id.whyButton)
        val okButton = descriptionDialog.findViewById<Button>(R.id.okButton)

        noButton.setOnClickListener { view -> checkDataCompletionUploadFiles(descriptionDialog) }
        whyButton.setOnClickListener { view ->
            descriptionDialog.dismiss()
            showAreYouSureDialog()
        }
        okButton.setOnClickListener { view ->
            descriptionDialog.dismiss()
            dispatchTakePictureIntent()
        }

        descriptionDialog.setCanceledOnTouchOutside(false)
        descriptionDialog.show()
    }

    private fun checkDataCompletionUploadFiles(dialog: Dialog) {
        mCurrentSubmitBtn.isEnabled = false
        dialog.dismiss()
        if (!isNumberPickerDashes() || photoTaken) {
            uploadFile()
        }
    }

    private fun showAreYouSureDialog() {
        val dialog = Dialog(this)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.are_you_sure_dialog)
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        val descriptionDialogTextView = dialog.findViewById<TextView>(R.id.descriptionDialogTextView)

        descriptionDialogTextView.text = getString(R.string.mom_vitals_message)

        val noButton = dialog.findViewById<Button>(R.id.noButton)
        val okButton = dialog.findViewById<Button>(R.id.okButton)

        noButton.setOnClickListener { view ->
            dialog.dismiss()
            finish()
        }
        okButton.setOnClickListener { view ->
            dialog.dismiss()
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showCallAlert(int: Int) {
        alertDialog = Dialog(this)
        alertDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.are_you_sure_dialog)
        alertDialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        val descriptionDialogTextView = alertDialog.findViewById<TextView>(R.id.descriptionDialogTextView)
        val noButton = alertDialog.findViewById<Button>(R.id.noButton)
        val okButton = alertDialog.findViewById<Button>(R.id.okButton)
        when (int) {
            4 -> {
                descriptionDialogTextView.text = getString(R.string.mom_vitals_warning_4)
                noButton.text = "Cancel"
                okButton.text = "Call Grady Nurse"

                noButton.setOnClickListener { view ->
                    alertDialog.dismiss()
                    super.onBackPressed()
                }
                okButton.setOnClickListener { view ->
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:4046160600")
                    startActivity(intent)
                    alertDialog.dismiss()
                    super.onBackPressed()
                }
            }
            3 -> {
                fifteenMinuteAlertTriggered = true
                descriptionDialogTextView.text = getString(R.string.mom_vitals_warning_3)
                noButton.visibility = GONE
                okButton.text = "OK"
                okButton.setOnClickListener { view ->
                    checkDataCompletionUploadFiles(alertDialog)
                    reset()
                }
            }
            2 -> {
                descriptionDialogTextView.text = getString(R.string.mom_vitals_warning_2)
                noButton.visibility = GONE
                okButton.text = "OK"
                okButton.setOnClickListener { view ->
                    checkDataCompletionUploadFiles(alertDialog)
                }
            }
            else -> {
                descriptionDialogTextView.text = getString(R.string.mom_vitals_warning_1)
                noButton.visibility = GONE
                okButton.text = "OK"
                okButton.setOnClickListener { view ->
                    checkDataCompletionUploadFiles(alertDialog)
                }
            }
        }

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun reset() {
        mCurrentSubmitBtn.isEnabled = true
        imageView.background = ContextCompat.getDrawable(this, R.drawable.bpdevice)
        mCurrentPhotoPath = ""
        photoFile = null
        photoTaken = false
        isSPNumberPickerClicked = false
        isDPNumberPickerClicked = false
        isPRNumberPickerClicked = false
    }
}
