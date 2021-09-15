package com.cliffordlab.amoss.gui.surveys

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R.layout.activity_suds
import com.cliffordlab.amoss.helper.CSVCreator
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.settings.SettingsUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_suds.*

class SUDSActivity : AppCompatActivity() {

    private lateinit var mDisposable: Disposable
    private lateinit var csv : StringBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_suds)
        seekBar.max = 9
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var progress = 0

            override fun onProgressChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
                progress = progresValue + 1
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                displayNumber.visibility = View.VISIBLE
                submit.isEnabled = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                displayNumber.text = progress.toString()
            }
        })

        submit.setOnClickListener {
            SettingsUtil.setSUDSCompleted(applicationContext, true)
            createCSV()
            val upload = CSVCreator(applicationContext)
            AmossNetwork.changeBaseURL(BuildConfig.apiBase)
            val preparedFile = upload.prepareFileUpload(csv, applicationContext, "suds", ".csv")

            mDisposable = AmossNetwork.client.upload(upload.amossHeaderMap, upload.amossPartList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ upload.handleResponse(it, preparedFile) }, { upload.handleError(it) })
            finish()
        }
    }

    private fun createCSV() {
        csv = StringBuilder()
        csv.append("suds score: " + displayNumber.text)
    }

    companion object Title {
        val name = "SUDS"
        private const val TAG = "SUDSActivity"
    }
}
