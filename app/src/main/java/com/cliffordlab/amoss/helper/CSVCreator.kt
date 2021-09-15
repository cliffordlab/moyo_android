package com.cliffordlab.amoss.helper

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.cliffordlab.amoss.gui.LoginActivity
import com.cliffordlab.amoss.network.json.UploadResponse
import com.cliffordlab.amoss.settings.SettingsUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by ChristopherWainwrightAaron on 7/18/17.
 */
class CSVCreator(context: Context) {
    lateinit var amossHeaderMap: Map<String, String>
    var amossPartList: MutableList<MultipartBody.Part>
    private val mContext: Context

    companion object {
        private const val TAG = "AmossUpload"
    }

    init {
        amossPartList = ArrayList()
        mContext = context
    }

    fun prepareForMultiFileUpload(context: Context?, fileLastModified: Long) {
        val map: MutableMap<String, String> = HashMap()
        map["Authorization"] = "Mars " + SettingsUtil.authToken(context)
        val c = Calendar.getInstance()
        c.timeInMillis = fileLastModified
        c.firstDayOfWeek = Calendar.MONDAY
        c[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        c.timeZone = TimeZone.getTimeZone("UTC")
        val monday = c.timeInMillis
        val mondayString = monday.toString()
        val partialMonTimestamp = mondayString.substring(1)
        map["weekMillis"] = partialMonTimestamp
        amossHeaderMap = map
    }

    fun prepareFileUpload(csvBody: StringBuilder, context: Context, fileType: String, fileExtension: String): File {
        val file = createFile(csvBody, context, fileType, fileExtension)
        val headerMap: MutableMap<String, String> = setHeader(context, file)

        //create RequestBody instance from file
        val requestFile = RequestBody.create(null, file)
        val partList: MutableList<MultipartBody.Part> = ArrayList()
        partList.add(MultipartBody.Part.createFormData("upload", file.name, requestFile))
        amossHeaderMap = headerMap
        amossPartList = partList
        return file
    }

    fun prepareFileUpload(csvBody: StringBuilder, photoFile: File?, context: Context, fileType: String): File {

        val csvFile = createFile(csvBody, context, fileType, ".csv")
        val headerMap: MutableMap<String, String> = setHeader(context, csvFile)
        val partList: MutableList<MultipartBody.Part> = ArrayList()

        //create RequestBody instance from file
        val requestFile = RequestBody.create(null, csvFile)
        partList.add(MultipartBody.Part.createFormData("upload", csvFile.name, requestFile))

        if (photoFile != null) {
            val imgFileName = getFileName(System.currentTimeMillis(), fileType,".jpg")
            Log.d(TAG, "prepareFileUpload: $imgFileName")
            val photoFileRB = RequestBody.create(null, photoFile)
            partList.add(MultipartBody.Part.createFormData("upload", imgFileName, photoFileRB))
        }

        amossHeaderMap = headerMap
        amossPartList = partList
        return csvFile
    }

    private fun setHeader(context: Context, file: File): MutableMap<String, String> {
        val map: MutableMap<String, String> = HashMap()
        map["Authorization"] = "Mars " + SettingsUtil.authToken(context)
        val c = Calendar.getInstance()
        c.timeInMillis = file.lastModified()
        c.firstDayOfWeek = Calendar.MONDAY
        c[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        c.timeZone = TimeZone.getTimeZone("UTC")
        val monday = c.timeInMillis
        val mondayString = monday.toString()
        val partialMonTimestamp = mondayString.substring(1)
        map["weekMillis"] = partialMonTimestamp
        return map
    }

    private fun createFile(csv: StringBuilder, context: Context, fileType: String, fileExtension: String): File {
        val dir = File(context.filesDir.toString() + "/amoss")
        if (!dir.exists()) {
            println("creating directory: " + dir.name)
            try {
                dir.mkdir()
            } catch (se: SecurityException) {
                FirebaseCrashlytics.getInstance().recordException(se)
                se.printStackTrace()
            }
        }
        val filename = getFileName(System.currentTimeMillis(), fileType, fileExtension)
        val file = File("$dir/$filename")
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (ioe: IOException) {
                FirebaseCrashlytics.getInstance().recordException(ioe)
                ioe.printStackTrace()
            }
        }
        try {
            val fw = FileWriter(file)
            val writer = BufferedWriter(fw)
            writer.write(csv.toString())
            writer.close()
        } catch (ioe: IOException) {
            FirebaseCrashlytics.getInstance().recordException(ioe)
            ioe.printStackTrace()
        }
        return file
    }

    fun getFileName(currentTimeInMillis: Long, fileType: String, fileExtension: String): String {
        return SettingsUtil.getParticipantId(mContext).toString() + "_" + currentTimeInMillis.toString().substring(1) + "_" + "android_" + fileType + fileExtension
    }

    fun handleResponse(uploadResponse: UploadResponse, file: File?) {
        if (uploadResponse.logoutUser != null) {
            SettingsUtil.setPrimaryDeviceForUser(mContext, false)
            SettingsUtil.addToken(mContext, SettingsUtil.NO_TOKEN)
            val intent = Intent(mContext, LoginActivity::class.java)
            mContext.startActivity(intent)
            return
        }
        when {
            uploadResponse.success != null -> {
                if (file != null) {
                    val renamedFile = File(file.absolutePath + ".sent")
                    file.renameTo(renamedFile)
                }
                Log.i(TAG, "handleResponse: successful upload")
            }
            uploadResponse.partialSuccess != null -> {
                Log.i(TAG, "handleResponse: partially successful upload")
            }
            uploadResponse.tokenError != null -> {
                Log.i(TAG, "handleResponse: setting new token and alt id")
                SettingsUtil.addParticipantID(mContext, uploadResponse.altID)
                SettingsUtil.addToken(mContext, uploadResponse.newToken)
                Toast.makeText(mContext, "Please try again.", Toast.LENGTH_LONG).show()
            }
            else -> {
                Log.i("sync", "request response is unsuccessful")
            }
        }
    }

    fun handleError(e: Throwable?) {
        Log.e(TAG, "handleError: ", e)
    }
}