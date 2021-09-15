package com.cliffordlab.amoss.gui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.ButterKnife
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.adapters.MainAdapter
import com.cliffordlab.amoss.app.AmossApplication
import com.cliffordlab.amoss.datacollector.location.GPSTracker
import com.cliffordlab.amoss.extensions.DividerItemDecoration
import com.cliffordlab.amoss.gui.activity.AccelGraphActivity
import com.cliffordlab.amoss.gui.environment.EnvironmentActivity
import com.cliffordlab.amoss.gui.epicfhir.MyChartActivity
import com.cliffordlab.amoss.gui.food.FoodDiaryActivity
import com.cliffordlab.amoss.gui.mom.MoyoMomActivity
import com.cliffordlab.amoss.gui.social.SocialGraphActivity
import com.cliffordlab.amoss.gui.surveys.SurveyListActivity
import com.cliffordlab.amoss.gui.surveys.WeightActivity
import com.cliffordlab.amoss.gui.vitals.VitalsActivity
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.helper.MenuOptions
import com.cliffordlab.amoss.models.EnvironmentModel
import com.cliffordlab.amoss.models.airVisual.AirVisualGPSResponse
import com.cliffordlab.amoss.models.moyoEnvironmental.EnvironmentalResponse
import com.cliffordlab.amoss.network.AmossNetwork
import com.cliffordlab.amoss.network.DataUploader
import com.cliffordlab.amoss.settings.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnDataPointListener {
    private lateinit var toolbar: Toolbar
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var adapter: MainAdapter? = null
    lateinit var gps: GPSTracker
    lateinit var mDisposable: Disposable
    private val REQUEST_OAUTH_REQUEST_CODE = 0x1001
    private lateinit var mSettingsUtil: SettingsUtil

    //permission is automatically granted on sdk<23 upon installation
    val isStoragePermissionGranted: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission is granted")
                    return true
                } else {
                    Log.v(TAG, "Permission is revoked")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    return false
                }
            } else {
                Log.v(TAG, "Permission is granted")
                return true
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar

        if (intent.extras != null && intent.extras!!.getBoolean("EXIT", false)) {
            val intent = Intent(AmossApplication.context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("EXIT", true)
            startActivity(intent)
        }

        mSettingsUtil = SettingsUtil(this)
        setUpNavigation()

        setSupportActionBar(toolbar)

        mainRecyclerView.setHasFixedSize(true)
        adapter = MainAdapter(this@MainActivity, getData(createRowNames()))

        mainRecyclerView.adapter = adapter
        mainRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        mainRecyclerView.addItemDecoration(
            DividerItemDecoration(
                applicationContext,
                DividerItemDecoration.VERTICAL_LIST
            )
        )

        when (SettingsUtil(this).studyId) {
            "MME" -> {
                // no permissions needed
            }
            else -> {
                if (!isPermissionsGranted()) {
                    requestPermissions()
                }
            }
        }
    }

    private fun isPermissionsGranted(): Boolean {
        val locationPermission = ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
        val smsPermission = ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.READ_SMS)
        val callPermission = ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.READ_CALL_LOG)
        val writePermission = ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val activityPermission = ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission_group.SENSORS)
        return (locationPermission == PackageManager.PERMISSION_GRANTED ||
                smsPermission == PackageManager.PERMISSION_GRANTED ||
                callPermission == PackageManager.PERMISSION_GRANTED ||
                writePermission == PackageManager.PERMISSION_GRANTED ||
                activityPermission == PackageManager.PERMISSION_GRANTED )
    }


    @SuppressLint("WrongViewCast")
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                    findViewById(R.id.main_activity_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    }
                    .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS, Manifest.permission.BODY_SENSORS, Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
            signIntoGoogleFitnessAPI()
        }
    }

    fun signIntoGoogleFitnessAPI() {
        val fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build()
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions)
        } else {
            subscribe()
        }
    }

    /** Records step data by requesting a subscription to background step data.  */
    fun subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mSettingsUtil.setGoogleDataCollection(true)
                        Log.i(TAG, "Successfully subscribed!")
                    } else {
                        Log.w(TAG, "There was a problem subscribing.", task.exception)
                    }
                }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @SuppressLint("WrongViewCast")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        val serviceControl = ServiceControl(applicationContext, this)
        //for now service control class has request codes use that as reference sorry
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    serviceControl.startLocationServices()
                    mSettingsUtil.setLocCollection(true)

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    serviceControl.stopLocationServices()
                    mSettingsUtil.setLocCollection(false)
                }
                return
            }
            3 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    serviceControl.startCallActivityServices()
                    mSettingsUtil.setCallDataCollection(true)

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    serviceControl.stopCallActivityServices()
                    mSettingsUtil.setCallDataCollection(false)
                }
                return
            }
            4 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    serviceControl.startLIWCActivityServices()
                    mSettingsUtil.setLIWCDataCollection(true)

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    serviceControl.stopLIWCActivityServices()
                    mSettingsUtil.setLIWCDataCollection(false)
                }
                return
            }
            REQUEST_OAUTH_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mSettingsUtil.setGoogleDataCollection(true)

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    mSettingsUtil.setGoogleDataCollection(false)
                }
                return
            }

            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.size <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    //buildFitnessClient()
                    ServiceControl(this, this ).initServices()

                } else {
                    // Permission denied.

                    // In this Activity we've chosen to notify the user that they
                    // have rejected a core permission for the app since it makes the Activity useless.
                    // We're communicating this message in a Snackbar since this is a sample app, but
                    // core permissions would typically be best requested during a welcome-screen flow.

                    // Additionally, it is important to remember that a permission might have been
                    // rejected without asking the user for permission (device policy or "Never ask
                    // again" prompts). Therefore, a user interface affordance is typically implemented
                    // when permissions are denied. Otherwise, your app could appear unresponsive to
                    // touches or interactions which have required permissions.
                    Snackbar.make(
                            findViewById(R.id.main_activity_view),
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction("settings") {
                                // Build intent that displays the App settings screen.
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null)
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                            .show()
                }
            }
        }
    }

    override fun onBackPressed() {
        //disable back button
    }

    public override fun onResume() {
        super.onResume()
//        runServices()

        val myApp = this.application as AmossApplication
        myApp.stopActivityTransitionTimer()

        if (isPermissionsGranted() && mSettingsUtil.isLocCollectionEnabled) {
            gps = GPSTracker(this)
            getEnvironmentData()
//            getWeatherData()
        }
    }

    private fun getEnvironmentData() {
        Log.i("moyo api request", "attempting to get location")
        println("moyo api request attempt")
        println("moyo api request attempt lat" + gps.latitude)
        println("moyo api request attempt long" + gps.longitude)
        Log.i("moyo api request", "location is enabled")
        if (gps.canGetLocation()) {

            val lat = gps.latitude
            val lon = gps.longitude
            AmossNetwork.changeBaseURL("https://amoss.emory.edu/")
            val mapQuery = HashMap<String, String>()
            mapQuery["lat"] = lat.toString()
            mapQuery["long"] = lon.toString()
            mDisposable = AmossNetwork.client.getEnvironmentalData(mapQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ handleResponse(it) }, { handleError(it) })
        }
    }

    private fun getWeatherData() {
        // check if GPS enabled
        Log.i("air visual", "attempting to get location")
        println("air visual attempt")
        if (gps.canGetLocation()) {
            Log.i("air visual", "location is enabled")
            val lat = gps.latitude
            val lon = gps.longitude
            Log.i("air visual", "$lat")
            Log.i("air visual", "$lon")
            AmossNetwork.changeBaseURL(Constants.AIRVISUAL_BASE_URL)
            mDisposable = AmossNetwork.client.getWeather(lat, lon, BuildConfig.AIR_VISUAL_API_KEY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ handeWeatherResponse(it) }, { handleError(it) })
        }
    }

    fun handeWeatherResponse(weatherResponse: AirVisualGPSResponse) {
        Log.i("air visual", "Temp is ${weatherResponse.mAirVisualData?.currentWeather?.mAirVisualWeather?.tempCelsius}")
        val gson = Gson()
        Log.i("air visual", gson.toJson(weatherResponse))
    }

    fun handleError(e: Throwable) {
        e.printStackTrace()
    }

    fun handleResponse(response: EnvironmentalResponse) {
        Log.d(TAG, "handleResponse: " + response.toString())
        val realm = Realm.getDefaultInstance()
        val result = realm.where(EnvironmentModel::class.java).findAll()
        val realmQuery = result.where().findFirst()

        if (realmQuery == null) {
                realm.executeTransaction { realm1 ->
                    val moyoEnvironmentModel = realm1.createObject(EnvironmentModel::class.java)
                    moyoEnvironmentModel.foodDesertinessIndex = response.desertinessIndex
                    if (response.pollution.category != null) {
                        moyoEnvironmentModel.pollutionStatus = response.pollution.category.name
                    }
                    if (response.weather != null) {
                        moyoEnvironmentModel.weatherIcon = response.weather.icon
                        moyoEnvironmentModel.weatherSummary = response.weather.summary
                        moyoEnvironmentModel.weatherTemp = response.weather.temperature
                    }
                }
            } else {
                realm.executeTransaction {
                    realmQuery.foodDesertinessIndex = response.desertinessIndex
                    if (response.pollution.category != null) {
                        realmQuery.pollutionStatus = response.pollution.category.name
                    }
                    realmQuery.weatherIcon = response.weather.icon
                    realmQuery.weatherSummary = response.weather.summary
                    realmQuery.weatherTemp = response.weather.temperature
                }
        }
        realm.close()
    }

    public override fun onPause() {
        super.onPause()
        if (isPermissionsGranted()) {
            (this.application as AmossApplication).scheduleAllJobs()
            (this.application as AmossApplication).startActivityTransitionTimer()
        }
        if (mClient != null) {
            mClient!!.disconnect()
        }
    }

    private fun runServices() {
        //check app settings for permission to run a service

        //pair service with permission
        val permissions = HashMap<String, Boolean>()
        permissions.put(CollectionSettings.ACTIVITY.toString(), mSettingsUtil.isAccCollectedEnabled)
        permissions.put(CollectionSettings.LOCATION.toString(), mSettingsUtil.isLocCollectionEnabled)
        permissions.put(CollectionSettings.LIWC.toString(), mSettingsUtil.isLIWCDataCollectionEnabled)
        permissions.put(CollectionSettings.CALL.toString(), mSettingsUtil.isCallDataCollectionEnabled)

        //pair accel service with Callable
        val serviceControl = ServiceControl(applicationContext, this)
        val physAct: () -> Void? = serviceControl::startPhysicalActivityServices
        //pair location service with Callable
        val location: () -> Void? = serviceControl::startLocationServices

        val call: () -> Void? = serviceControl::startCallActivityServices
        val liwc: () -> Void? = serviceControl::startLIWCActivityServices


        val services = HashMap<String, () -> Void?>()
        services.put(CollectionSettings.ACTIVITY.toString(), physAct)
        services.put(CollectionSettings.LOCATION.toString(), location)
        services.put(CollectionSettings.LIWC.toString(), liwc)
        services.put(CollectionSettings.CALL.toString(), call)

        //if key which corresponds to data collection setting returns
        //true then call callable which is the service
        for ((key, value) in permissions) {
            if (value) {
                try {
                    services[key].run {  }

                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Log.e("runServices", e.message.toString())
                }
            }
        }
    }

    override fun onDataPoint(dataPoint: DataPoint) {}

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i("GoogleFit", "RESULT_OKAY")
                mSettingsUtil.setGoogleDataCollection(true)
//                signIntoGoogleFitnessAPI()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                mSettingsUtil.setGoogleDataCollection(false)
                Log.e("GoogleFit", "RESULT_CANCELED")
            }
        } else {
//            mSettingsUtil.setAccCollection(false)
            Log.e("GoogleFit", "requestCode NOT request_oauth")
        }
    }

    //todo add new list
    private fun createRowNames(): Array<String> {
        val temp: Array<String>

        when (SettingsUtil(this).studyId) {
            "MME" -> {
                temp = arrayOf(
                        MoyoMomActivity.name,
                        SettingsActivity.TITLE
                )
            }
            else -> {
                temp = arrayOf(
                        AccelGraphActivity.name,
                        EnvironmentActivity.name,
                        FoodDiaryActivity.name,
                        SurveyListActivity.name,
                        SocialGraphActivity.name,
                        VitalsActivity.name,
                        MoyoMomActivity.name,
                        MyChartActivity.name,
                        SettingsActivity.TITLE
                )
            }
        }
        return temp
    }

    private fun getData(rows: Array<String>): List<MenuOptions> {
        val studyID = SettingsUtil(this).studyId
        val data = ArrayList<MenuOptions>()
        for (i in rows.indices) {
            val current = MenuOptions()
            current.optionName = rows[i]
            current.setResId(i, studyID)
            data.add(current)
        }
        return data
    }

    public override fun onStop() {
        super.onStop()
        if (mClient != null) {
            mClient!!.disconnect()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mClient != null) {
            mClient!!.disconnect()
        }
        if (::mDisposable.isInitialized) {
            if (!mDisposable.isDisposed) {
                mDisposable.dispose()
            }
        }
    }

    private fun setUpNavigation() {
        mDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.open, R.string.close)
        drawer_layout.addDrawerListener(mDrawerToggle as ActionBarDrawerToggle)

        if (SettingsUtil.isAuthenticated(this)) {
            unlockDrawer()
            if (supportActionBar != null) {
                supportActionBar!!.setHomeButtonEnabled(true)
            }
            mDrawerToggle!!.syncState()
        } else {
            lockDrawer()
        }

        if (BuildConfig.DEBUG) {
            nvView.setNavigationItemSelectedListener { item ->
                selectDrawerItem(item)
                true
            }
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_weight -> {
                val intentWeight = Intent(this, WeightActivity::class.java)
                startActivity(intentWeight)
            }
            R.id.nav_sync -> {
                Log.i("sync", "preparing to sync data")
                val uploader = DataUploader(context = applicationContext)
                uploader.uploadData()
            }
            R.id.nav_settings -> {
                val intentSettings = Intent(this, SettingsActivity::class.java)
                startActivity(intentSettings)
            }
        }
        drawer_layout.closeDrawers()
    }

    fun lockDrawer() {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun unlockDrawer() {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    companion object {

        val TITLE = "Home"
        private var hasFitnessClient = false
        private val INIT_REQUEST = 0
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        val FIT_TAG = "BasicHistoryApi"
        val TAG = MainActivity::class.java.simpleName
        private val REQUEST_OAUTH = 4097
        private val DATE_FORMAT = "yyyy.MM.dd HH:mm:ss"

        /**
         * Track whether an authorization activity is stacking over the current activity, i.e. when
         * a known auth error is being resolved, such as showing the account chooser or presenting a
         * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
         */
        private val AUTH_PENDING = "auth_state_pending"


        var mClient: GoogleApiClient? = null
    }
}

