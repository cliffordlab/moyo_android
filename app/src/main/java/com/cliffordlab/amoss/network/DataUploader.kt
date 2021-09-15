package com.cliffordlab.amoss.network

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.gui.LoginActivity
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.network.json.UploadResponse
import com.cliffordlab.amoss.settings.SettingsUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException

/**
 * Created by ChristopherWainwrightAaron on 10/9/17.
 */
class DataUploader(context: Context) {
    private val mContext = context

    fun uploadData() {
        val root = File(mContext.filesDir.toString() + "/amoss")
        //find all files that have not been uploaded
        val allFiles = root.listFiles { _, filename -> !filename.endsWith(".sent") }
        println(message = "Amount files to be sent:\n ${allFiles.size}")
        allFiles.toFlowable()
                .onBackpressureLatest()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeBy(  // named arguments for lambda Subscribers
                        onNext = { uploadFile(it) },
                        onError =  { it.printStackTrace() },
                        onComplete = { println("upload complete") }
                )

    }

    private fun uploadFile(file: File) {
        val mediaType = "text/html".toMediaTypeOrNull()
        if (!file.isDirectory) {
            val upload = CSVCreator(mContext)
            upload.amossPartList.add(
                MultipartBody.Part.createFormData(
                    "upload",
                    file.name,
                    RequestBody.create(mediaType, file)
                )
            )
            upload.prepareForMultiFileUpload(mContext, file.lastModified())
            Log.i("sync", "syncing data: ${file.name}")
            AmossNetwork.changeBaseURL(BuildConfig.apiBase)

            try {
                val res =
                    AmossNetwork.client.uploadFiles(upload.amossHeaderMap, upload.amossPartList)
                        .execute()
                if (res.isSuccessful) {
                    val uploadResponse = res.body() as UploadResponse
                    if (uploadResponse.logoutUser != null) {
                        runOnUiThread {
                            Toast.makeText(
                                mContext,
                                "Logging out. You are currently logged in from another device!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        SettingsUtil.setPrimaryDeviceForUser(mContext, false)
                        SettingsUtil.addToken(mContext, SettingsUtil.NO_TOKEN)
                        val intent = Intent(mContext, LoginActivity::class.java)
                        mContext.startActivity(intent)
                        return
                    }
                    if (uploadResponse.success != null) {
//                        Log.i("sync", "File was sent the response is:\n${uploadResponse.success}")
                        val renamedFile = File(file.absolutePath + ".sent")
                        file.renameTo(renamedFile)
                    } else if (uploadResponse.tokenError != null) {
                        SettingsUtil.addParticipantID(mContext, uploadResponse.altID)
                        SettingsUtil.addToken(mContext, uploadResponse.newToken)
                    } else {
                        Log.i("sync", "request response is unsuccessful")
                    }
                } else {
                    val uploadResponse = res.body() as UploadResponse
                    if (uploadResponse.logoutUser != null) {
                        Toast.makeText(mContext, "Upload failed!", Toast.LENGTH_LONG).show()
                        SettingsUtil.setPrimaryDeviceForUser(mContext, false)
                        SettingsUtil.addToken(mContext, SettingsUtil.NO_TOKEN)
                        val intent = Intent(mContext, LoginActivity::class.java)
                        mContext.startActivity(intent)
                        return
                    }
                }
            } catch (e: FileNotFoundException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                println(e.message)
            }
        }
    }
}
