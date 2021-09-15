package com.cliffordlab.amoss.gui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.OnClick
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.misc.MoyoBrowser
import com.cliffordlab.amoss.helper.AmossDialogs
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.network.json.LoginRequest
import com.cliffordlab.amoss.network.json.TokenResponse
import com.cliffordlab.amoss.receivers.LoginReceiver
import com.cliffordlab.amoss.settings.SettingsUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var progressText: TextView
    private lateinit var progressLayout: ConstraintLayout
    internal var mParticipantId: EditText? = null
    internal var mPassword: EditText? = null
    private var wrongLoginAlert: AlertDialog.Builder? = null
    private var formIncompleteAlert: AlertDialog.Builder? = null
    private var tooManyLoginAttemptsAlert: AlertDialog.Builder? = null
    private lateinit var progressBar: ProgressBar
    private var loginAttempts = 3

    private var mDisposable: Disposable? = null


    internal lateinit var mUtil: SettingsUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)
        if (intent.extras != null && intent.extras?.getBoolean("EXIT", false)!!) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
                System.exit(0)
                return
            } else {
                finishAffinity()
                System.exit(0)
                return
            }
        }
        mUtil = SettingsUtil(applicationContext)

        wrongLoginAlert = AlertDialog.Builder(this)
        wrongLoginAlert?.setTitle("Wrong patient id or password")
        wrongLoginAlert?.setPositiveButton("OK") { dialog, whichButton -> dialog.dismiss() }

        formIncompleteAlert = AlertDialog.Builder(this)
        formIncompleteAlert?.setTitle("Please complete login form")
        formIncompleteAlert?.setPositiveButton("OK") { dialog, whichButton -> dialog.dismiss() }

        tooManyLoginAttemptsAlert = AlertDialog.Builder(this)
        tooManyLoginAttemptsAlert?.setTitle("To many login attempts")
        tooManyLoginAttemptsAlert?.setMessage("Please try again tomorrow you have attempted to login to many times")
        tooManyLoginAttemptsAlert?.setPositiveButton("OK") { dialog, whichButton -> dialog.dismiss() }

        progressBar = ProgressBar(this, null,android.R.attr.progressBarStyleLarge)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.textView38)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar.isIndeterminate = true
        val params = RelativeLayout.LayoutParams(100,100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        progressBar.visibility = View.GONE     // To Hide ProgressBar
        progressLayout.visibility = View.GONE     // To Hide ProgressBar
        progressText.visibility = View.GONE     // To Hide ProgressBar

        mParticipantId = this.findViewById(R.id.uid)
        mPassword = this.findViewById(R.id.password)
        val loginButton = this.findViewById<TextView>(R.id.login_button)
        loginButton.setOnClickListener {
            login()
        }

    }

    @OnClick(R.id.textView28)
    protected fun toForgotPasswordWebView() {
        Log.i(TAG, "toForgotPasswordWebView: " + "onclick is working")
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        if (isConnected) {
            val intent = Intent(applicationContext, MoyoBrowser::class.java)
            intent.putExtra("url", "https://amoss.emory.edu/moyo-beta/")
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "Please enable internet connection.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (SettingsUtil.isAuthenticated(this)) {
            //Do specific came-here-from-background code
            SettingsUtil.setPrimaryDeviceForUser(applicationContext, true)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else if (!SettingsUtil.isPrimaryUserDevice(applicationContext)) {
            val dialog = AmossDialogs()
            dialog.showDialog("logout", this@LoginActivity)
        }
    }

    @OnClick(R.id.login_button)
    internal fun login() {
        //delete loginattamptes = 0
        mUtil.setIsAuthorizedToLogin(true)
        loginAttempts = 3
        /////

        if (loginAttempts != 0 && mUtil.isAuthorizedToLogin) {
            val loginField = mParticipantId?.text.toString()
            val password = mPassword?.text.toString()
            var participantID = 0L
            var email : String? = null

            if (loginField == "" || password == "") {
                //make sure user fills in username and password
                formIncompleteAlert?.show()
            } else {
                //check to make sure participants id only contains numbers and is of length 4 or greater
                if (loginField.matches("[0-9]+".toRegex())) {
                    participantID = java.lang.Long.valueOf(loginField)
                } else {
                    email = loginField.trim()
                }

                //let user know app is attempting to authenticate
                progressBar.visibility = View.VISIBLE  //To show ProgressBar
                progressLayout.visibility = View.VISIBLE  //To show ProgressBar
                progressText.visibility = View.VISIBLE  //To show ProgressBar
                //use retrofit to attempt login
                val request = LoginRequest(participantID, email, password)
                AmossNetwork.changeBaseURL(BuildConfig.apiBase)
                Log.i(TAG, "login: this is the request $request")
                Log.i(TAG, "login: this is the url: " + BuildConfig.apiBase)
                mDisposable = AmossNetwork.client.loginParticipant(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ this.handleLoginResponse(it) }, { this.handleLoginError(it) })
            }
        } else {
            if (loginAttempts == 0) {
                //schedule app to be authorized to login in the next day if
                //to many login attempts were made
                val alarm_intent = Intent(applicationContext, LoginReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(applicationContext, 1000, alarm_intent, 0)

                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getDefault()
                calendar.add(Calendar.DAY_OF_MONTH, 1)

                val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
            //set attempts back to 3 so that we do not reschedule the alarm manager event
            loginAttempts = 3
            mUtil.setIsAuthorizedToLogin(false)
            //TODO remove id key
            tooManyLoginAttemptsAlert?.show()
        }
    }

    override fun onBackPressed() {

    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDisposable != null && mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
    }

    //TODO need to make proper alert for user
    private fun handleLoginError(e: Throwable) {
        e.printStackTrace()
        progressBar.visibility = View.GONE     // To Hide ProgressBar
        progressLayout.visibility = View.GONE     // To Hide ProgressBar
        progressText.visibility = View.GONE     // To Hide ProgressBar
    }

    private fun handleLoginResponse(tokenResponse: TokenResponse) {
        Log.d(TAG, "handleLoginResponse: $tokenResponse")
        Log.d(TAG, "handleLoginResponse token: ${tokenResponse.token}")

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        if (tokenResponse.token.isNotEmpty()) {
            SettingsUtil.addToken(applicationContext, tokenResponse.token)
            SettingsUtil.addParticipantID(applicationContext, java.lang.Long.valueOf(tokenResponse.participantID))

            progressBar.visibility = View.GONE     // To Hide ProgressBar
            progressLayout.visibility = View.GONE     // To Hide ProgressBar
            progressText.visibility = View.GONE     // To Hide ProgressBar

            val view = this@LoginActivity.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            val settingsUtil = SettingsUtil(applicationContext)
            settingsUtil.isLoggedInViaAmoss(true)
            settingsUtil.addStudyId(tokenResponse.study)
            FirebaseCrashlytics.getInstance().setCustomKey("Participant ID", tokenResponse.participantID)
            FirebaseCrashlytics.getInstance().setCustomKey("Study ID", settingsUtil.studyId)

            //TODO might need to setup navigation
            val intentMain = Intent(applicationContext, MainActivity::class.java)
            startActivity(intentMain)
        } else {
            //TODO remove id key
            progressBar.visibility = View.GONE     // To Hide ProgressBar
            progressLayout.visibility = View.GONE     // To Hide ProgressBar
            progressText.visibility = View.GONE     // To Hide ProgressBar            wrongLoginAlert!!.show()
            loginAttempts -= 1
        }
    }

    companion object {

        private val TAG = "LoginActivity"
    }
}
