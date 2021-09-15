package com.cliffordlab.amoss.gui.misc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.LoginActivity
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.network.json.EmailRequest
import com.cliffordlab.amoss.network.json.EmailResponse
import com.cliffordlab.amoss.settings.SettingsUtil
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_email.*

class EmailActivity : AppCompatActivity() {
    private var mDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        button3.setOnClickListener {
            if (emailText.text.toString() == "") {
                Toast.makeText(applicationContext, "Please enter text", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!emailText.text.toString().contains("@")) {
                Toast.makeText(applicationContext, "Please enter valid email address", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val util = SettingsUtil(applicationContext)
            util.email = emailText.text.toString()

            //use retrofit to attempt login

            val request = EmailRequest(emailText.text.toString())
            mDisposable = AmossNetwork.client.loginParticipant(request)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        this.handleLoginResponse(it)
                        if (mDisposable != null && !mDisposable!!.isDisposed) {
                            mDisposable!!.dispose()
                            AmossNetwork.changeBaseURL(BuildConfig.apiBase)
                        }
                    }, { this.handleLoginError(it) })

            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLoginError(e: Throwable) {
        e.printStackTrace()
    }

    private fun handleLoginResponse(emailResponse: EmailResponse) {
        println(emailResponse.success)
        println(emailResponse.failure)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }

}
