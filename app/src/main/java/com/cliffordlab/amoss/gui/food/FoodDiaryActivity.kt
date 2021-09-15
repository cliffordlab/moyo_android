package com.cliffordlab.amoss.gui.food

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
import kotlinx.android.synthetic.main.food_diary_activity.*
import kotlinx.android.synthetic.main.vitals_activity.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FoodDiaryActivity : AppCompatActivity()  {
    companion object Title {
        val name = "FOOD"
        val TAG = "FoodDiaryActivity"
    }

    private val REQUEST_TAKE_PHOTO = 1
    private var foodDiaryCSV: StringBuilder? = null
    private var mDisposable: Disposable? = null
    private var mCurrentPhotoPath: String? = null
    private var photoFile: File? = null
    private var photoTaken: Boolean = false
    private lateinit var descriptionDialog: Dialog
    private var hasSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.food_diary_activity)
        takePhotoButton.setOnClickListener { dispatchTakePictureIntent() }
        submitFoodInfo.setOnClickListener { sendFoodInfo() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic()
            photoTaken = true
        }
    }

    private fun sendFoodInfo() {
        // if food name field is blank
        // toast please put food name
        Log.i(TAG, "sendFoodInfo: " + foodNameField.text.toString())
        when {
            foodNameField.text.toString() == "" && !photoTaken -> showToast(1)
            foodNameField.text.toString() == "" && photoTaken -> showToast(2)
            foodNameField.text.toString() != "" && !photoTaken -> showToast(3)
            else -> uploadFile()
        }
    }

    private fun uploadFile() {
        foodDiaryCSV = StringBuilder()
        foodDiaryCSV!!.append(foodNameField.text.toString())
        val upload = CSVCreator(applicationContext)
        val preparedFile = upload.prepareFileUpload(foodDiaryCSV!!, photoFile, applicationContext,"food")

        AmossNetwork.changeBaseURL(BuildConfig.apiBase)
        Log.i(TAG, "this is the Header: " + upload.amossHeaderMap)
        Log.i(TAG, "this is the part List: " + upload.amossPartList)
        //todo  upload may need to point to utsw
        showToast(4)
        mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
        if (hasSubmitted) {
            super.onBackPressed()
        }
    }

    private fun returnToMainActivity() {
        val handler = Handler()
        handler.postDelayed({
            val intentMain = Intent(applicationContext, MainActivity::class.java)
            startActivity(intentMain)
        }, 500)

    }

    private fun showToast(message: Int) {
        when (message) {
            1 -> showDescriptionDialog("Please take photo of your food and write food name into field.")
            2 -> showDescriptionDialog("Please enter name of food into field.")
            3 -> showDescriptionDialog("Please take photo of your food.")
        }

    }

    private fun showDescriptionDialog(message: String) {
        descriptionDialog = Dialog(this@FoodDiaryActivity)
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

    private fun showAreYouSureDialog() {
        descriptionDialog.dismiss()
        val dialog = Dialog(this@FoodDiaryActivity)
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

    private fun checkDataCompletionUploadFiles() {
        descriptionDialog.dismiss()
        if (foodNameField.text.toString() != "" || photoTaken) {
            uploadFile()
        } else {
            returnToMainActivity()
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
        imageView2.background = null
        Picasso.get().load("file:$mCurrentPhotoPath").into(imageView2)
    }
}
