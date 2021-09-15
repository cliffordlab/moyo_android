package com.cliffordlab.amoss.gui.vitals

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.MainActivity
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.network.AmossNetwork
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.vitals_activity.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class VitalsActivity : AppCompatActivity(){

    companion object Title {
        val name = "VITALS"
        val TAG = "VitalsActivity"
    }

    private val REQUEST_TAKE_PHOTO = 1
    private var bpCSV: StringBuilder? = null
    private var mDisposable: Disposable? = null
    private var mCurrentPhotoPath: String? = null
    private var photoFile: File? = null
    private var photoTaken: Boolean = false
    private var isSPNumberPickerClicked: Boolean = false
    private var isDPNumberPickerClicked: Boolean = false
    private var isPRNumberPickerClicked: Boolean = false
    private lateinit var descriptionDialog: Dialog
    private var hasSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vitals_activity)
        textView.text = resources.getString(R.string.vitals_picture_instructions)
        imageView3.background = null
        Picasso.get().load(R.drawable.bp).into(imageView3)
        setNumberPicker()
        snapBPPhotoButton.setOnClickListener { dispatchTakePictureIntent() }
        submitBP.setOnClickListener {
            hasSubmitted = true
            sendBPInfo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic()
            photoTaken = true
        }
    }

    private fun returnToMainActivity() {
        val handler = Handler()
        handler.postDelayed({
            val intentMain = Intent(applicationContext, MainActivity::class.java)
            startActivity(intentMain)
        }, 500)
    }

    private fun uploadFile() {
        bpCSV = StringBuilder()
        bpCSV!!.append(sPNumberPicker.value.toString())
        bpCSV!!.append(",")
        bpCSV!!.append(dPNumberPicker.value.toString())
        bpCSV!!.append(",")
        bpCSV!!.append(pRNumberPicker.value.toString())
        bpCSV!!.append(",")

        val upload = CSVCreator(applicationContext)
        val preparedFile = upload.prepareFileUpload(bpCSV!!, photoFile, applicationContext,"vitals")

        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        Log.i(TAG, "this is the Header: " + upload.amossHeaderMap)
        Log.i(TAG, "this is the part List: " + upload.amossPartList)
        //todo  upload may need to point to utsw
        showAlertDialog(4)
        mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
        if (hasSubmitted) {
            super.onBackPressed()
        }
    }

    private fun sendBPInfo() {
        Log.i(TAG, "SP = ${sPNumberPicker.value}, PR = ${pRNumberPicker.value}, DP = ${dPNumberPicker.value}" )
        when {
            (isNumberPickerDashes()) && !photoTaken -> showAlertDialog(1)
            (isNumberPickerDashes()) && photoTaken -> showAlertDialog(2)
            (!isNumberPickerDashes()) && !photoTaken -> showAlertDialog(3)
            else -> uploadFile()
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
            1 -> showDescriptionDialog("Please take photo of your vitals and select vitals number into field.")
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
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
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
        descriptionDialog = Dialog(this@VitalsActivity)
        descriptionDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        descriptionDialog.setContentView(R.layout.description_dialog)
        descriptionDialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        val descriptionDialogTextView = descriptionDialog.findViewById<TextView>(R.id.descriptionDialogTextView)

        descriptionDialogTextView.text = message


        val noButton = descriptionDialog.findViewById<Button>(R.id.noButton)
        val whyButton = descriptionDialog.findViewById<Button>(R.id.whyButton)
        val okButton = descriptionDialog.findViewById<Button>(R.id.okButton)

        noButton.setOnClickListener { view -> checkDataCompletionUploadFiles() }
        whyButton.setOnClickListener { view -> showAreYouSureDialog()}
        okButton.setOnClickListener { view -> descriptionDialog.dismiss() }

        descriptionDialog.setCanceledOnTouchOutside(false)
        descriptionDialog.show()
    }

    private fun checkDataCompletionUploadFiles() {
        descriptionDialog.dismiss()
        if (!isNumberPickerDashes() || photoTaken) {
            uploadFile()
        } else {
            returnToMainActivity()
        }
    }

    private fun showAreYouSureDialog() {
        descriptionDialog.dismiss()
        val dialog = Dialog(this@VitalsActivity)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.are_you_sure_dialog)
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        val descriptionDialogTextView = dialog.findViewById<TextView>(R.id.descriptionDialogTextView)

        descriptionDialogTextView.text = "We are making an app to automatically read your blood pressure and heart rate from the photo. By taking a photo and selecting the right blood pressure and heart rate you will help us create this new app and save everyone time..."

        val noButton = dialog.findViewById<Button>(R.id.noButton)
        val okButton = dialog.findViewById<Button>(R.id.okButton)

        noButton.setOnClickListener { view -> checkDataCompletionUploadFiles() }
        okButton.setOnClickListener { view -> dialog.dismiss() }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}
