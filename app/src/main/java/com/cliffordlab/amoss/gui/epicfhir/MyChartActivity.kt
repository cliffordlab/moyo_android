package com.cliffordlab.amoss.gui.epicfhir

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.gui.MainActivity
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.network.json.EpicTokenResponse
import com.cliffordlab.amoss.network.json.FhirDataRequest
import com.cliffordlab.amoss.network.json.FhirDataResponse
import com.cliffordlab.amoss.settings.SettingsUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_login.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.net.URLDecoder
import java.util.*

/**
 * Created by ChristopherWainwrightAaron on 2/5/18.
 */

class MyChartActivity : Activity() {
    private lateinit var util: SettingsUtil
    private lateinit var fhirCode: String
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cliffordlab.amoss.R.layout.web_browser)

        util = SettingsUtil(applicationContext)
        Log.i(TAG, "onCreate: fhircode: " + util.fhirCode)
        if (util.fhirCode != "no fhir code") {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            return
        }

        val (currentUrl, web) = buildWebViewClient()
        web.loadUrl(currentUrl.toString())
        getFhirCodeFromURL()
    }

    private fun getFhirCodeFromURL() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.i(TAG, "shouldOverrideUrlLoading: this is the URL: " + url)
                return if (url!!.take(28) == "https://amoss.emory.edu/utsw") {
                    val urlParsed = Uri.parse(url)
                    Log.i(TAG, "getFhirCodeFromURL: $urlParsed")

                    val paramNames = urlParsed.queryParameterNames
                    for (key in paramNames) {
                        val value = urlParsed.getQueryParameter("code")
                        fhirCode = value?.toString().toString()
                        if (fhirCode != "" && util.fhirCode == "no fhir code" && fhirCode.isNotEmpty()) {
                            getEpicAccessToken(fhirCode)
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.i(TAG, "shouldOverrideUrlLoading: no fhir code")
                        }
                        Log.i(TAG, "getFhirCodeFromURL: fhirCode: $fhirCode")
                        Log.i(TAG, "buildWebViewClient: paramnames: $value")
                    }
                    return true
                } else {
                    false
                }
            }
        }

    }

    private fun buildWebViewClient(): Pair<URL, WebView> {

        val builder = Uri.Builder()
        builder.scheme("https")
                .authority("EpicIntprxyPRD.swmed.edu")
                .appendPath("FHIR")
                .appendPath("oauth2")
                .appendPath("authorize")
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", BuildConfig.PROD_FHIR_CLIENT_ID)
                .appendQueryParameter("redirect_uri", "https://amoss.emory.edu/utsw")
                .appendQueryParameter("scope", "launch")
        val currentUrl = URL(URLDecoder.decode(builder.build().toString(), "UTF-8"))
        Log.i(TAG, "buildWebViewClient: $currentUrl")

        webView = findViewById(com.cliffordlab.amoss.R.id.webViewUTSW)
        webView.settings.javaScriptEnabled = true
        webView.settings.userAgentString = System.getProperty("http.agent")
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.domStorageEnabled = true


        Log.i(TAG, "buildWebViewClient: this is the user agent " + webView.settings.userAgentString)
        Log.i(TAG, "buildWebViewClient: this is the fhircode: " + util.fhirCode)
        return Pair(currentUrl, webView)
    }

    override fun onStart() {
        super.onStart()
        val settingsUtil = SettingsUtil(this)
        // if logged in than go to moodswipe else go to loginactivity
        Log.i(TAG, "onStart: fhircode: " + util.fhirCode)
        if (settingsUtil.fhirCode != "no fhir code") {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        val name: String = "MYCHART"
        private val TAG = "MyChartActivity"
    }

    private fun getEpicAccessToken(fhirCode: String) {
        util.fhirCode = fhirCode
        val grantType : RequestBody = "authorization_code"
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val code : RequestBody = fhirCode
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())
        Log.i(TAG, "getEpicAccessToken: fhircode: " + util.fhirCode)
        val redirectURI : RequestBody = "https://amoss.emory.edu/utsw"
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val clientID : RequestBody = BuildConfig.PROD_FHIR_CLIENT_ID
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        AmossNetwork.changeBaseURL(Constants.EPIC_FHIR_PROD_BASE_URL)
        AmossNetwork.client
                .getEpicAccessToken(grantType, code, redirectURI, clientID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.handleEpicResponse(it) }, { this.handleEpicError(it) })
    }

    private fun handleEpicError(e: Throwable) {
        Log.e(TAG, "handleEpicError: ", e)
        e.printStackTrace()
    }

    private fun handleEpicResponse(epicTokenResponse: EpicTokenResponse) {
        Log.i(TAG, "handleEpicResponse: epicTokenResponse = $epicTokenResponse")
        Log.i(TAG, "handleEpicResponse: this is the TTL: ${epicTokenResponse.timeToLive}")
        if (epicTokenResponse.accessToken != null) {

            //TODO token is saved. Sometime is being set back to 0 for some reason
            if (util.epicToken.isEmpty()) {
                util.epicToken = epicTokenResponse.accessToken
                util.epicTokenCreationTime = System.currentTimeMillis()
            }
            // Use creation time to compare with current time
            // If time is greater than an hour, refresh token.
            // Only allow one request per token duration.
            if (util.patientID.isEmpty()) {
                util.setPatientId(epicTokenResponse.patient)
            }
            if (util.epicToken.isNotEmpty()) {
                val tokenStartTime = util.epicTokenCreationTime
                val currentTime = System.currentTimeMillis()
                val diff = currentTime - tokenStartTime
                if (diff > 3480000) {
                    util.epicToken = epicTokenResponse.accessToken
                    util.epicTokenCreationTime = System.currentTimeMillis()
                }
                progressBar!!.visibility = View.VISIBLE
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                postCategory()
            }
        } else {
            Log.i(TAG, "handleEpicResponse: epic access token is null")
        }
    }

    private fun postCategory() {
        val request = FhirDataRequest( SettingsUtil.getParticipantId(this).toString(),SettingsUtil(this).epicToken, SettingsUtil(this).patientID, "allergy")
        AmossNetwork.changeBaseURL(Constants.AMOSS_API_BASE_URL)
        val headerMap = HashMap<String?, String?>()
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.firstDayOfWeek = Calendar.MONDAY
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.timeZone = TimeZone.getTimeZone("UTC")

        val monday = c.timeInMillis
        val mondayString = monday.toString()
        val partialMonTimestamp = mondayString.substring(1)
        headerMap["weekMillis"] = partialMonTimestamp
        AmossNetwork.client.postCategoryProd(headerMap, request)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ this.handleFhirResponse(it) }, { this.handleFhirError(it) })
    }

    private fun handleFhirError(e: Throwable) {
        e.printStackTrace()
        progressBar!!.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        Toast.makeText(this, "EMR data successfully requested", Toast.LENGTH_LONG).show()
    }

    private fun handleFhirResponse(response: FhirDataResponse?) {
        progressBar!!.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        Log.i("PostToFhir:", response.toString())
        if (response?.error == "missing patient credentials") {
            Toast.makeText(this, "Credentials for Epic data expired please re login to get new credentials", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "EMR data successfully requested", Toast.LENGTH_LONG).show()
        }
    }
}
