package com.cliffordlab.amoss.gui.surveys

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.cliffordlab.amoss.R
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_weight.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class WeightActivity : AppCompatActivity() {
    var entryText: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight)
        ButterKnife.bind(this)
        entryText = findViewById(R.id.editText)
        button2.setOnClickListener {
            submitWeight()
        }
    }

    protected fun submitWeight() {
        if (entryText!!.text.toString() == "") {
            Toast.makeText(this, "Please enter weight in pounds", Toast.LENGTH_SHORT).show()
            return
        }
        val builder = StringBuilder()
        builder.append(entryText!!.text.toString())
        builder.append(System.getProperty("line.separator"))
        val dir = File("$filesDir/amoss")
        if (!dir.exists()) {
            println("creating directory: " + dir.name)
            try {
                dir.mkdir()
            } catch (se: SecurityException) {
                FirebaseCrashlytics.getInstance().recordException(se)
                se.printStackTrace()
            }
        }
        val c = Calendar.getInstance()
        c.time = Date()
        c.firstDayOfWeek = Calendar.MONDAY
        c[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        c.timeZone = TimeZone.getTimeZone("UTC")
        val monday = c.timeInMillis
        val mondayUnixTimeStamp = monday.toString()
        val partialMonTimestamp = mondayUnixTimeStamp.substring(1)
        val fileExtension = ".weight"
        val filename = partialMonTimestamp + fileExtension
        val file = File("$dir/$filename")
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (ioe: IOException) {
                FirebaseCrashlytics.getInstance().recordException(ioe)
                println(ioe.message)
            }
        }
        try {
            val fw = FileWriter(file, true)
            val bw = BufferedWriter(fw)
            bw.write(builder.toString())
            bw.close()
        } catch (ioe: IOException) {
            FirebaseCrashlytics.getInstance().recordException(ioe)
            println(ioe.message)
        }
        finish()
    }
}