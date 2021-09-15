package com.cliffordlab.amoss.gui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.preference.*
import com.cliffordlab.amoss.BuildConfig
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.datacollector.AmossForegroundService
import com.cliffordlab.amoss.datacollector.accel.AccelService
import com.cliffordlab.amoss.datacollector.location.LocationService
import com.cliffordlab.amoss.gui.LoginActivity
import com.cliffordlab.amoss.helper.Constants
import com.cliffordlab.amoss.settings.ServiceControl
import com.cliffordlab.amoss.settings.SettingsUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    companion object {

        val TITLE = "SETTINGS"

        private const val TAG = "SettingsActivity"

    }
    private var mFragmentManager: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar!!.title = TITLE
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        mFragmentManager = supportFragmentManager

        overridePendingTransition(0, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
        private var mServiceControl: ServiceControl? = null
        private var mSettingsUtil: SettingsUtil? = null

        fun isAccelCollectionEnabled(): Boolean {
            val sensorPermission = findPreference("accel_data") as SwitchPreference?
            return sensorPermission!!.isChecked

        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
            SettingsUtil.register(activity, this)
            mServiceControl = ServiceControl(requireContext(), requireActivity())
            mSettingsUtil = SettingsUtil(activity)
            setSwitchValues()
            showSnackBar()

            val googlePermission = findPreference("step_data") as SwitchPreference?
            googlePermission?.setOnPreferenceClickListener { preference ->
                val preferenceManager = preferenceManager
                if (preferenceManager.sharedPreferences.getBoolean("step_data", true)) {
                    signIntoGoogleFitnessAPI()
                    mSettingsUtil!!.setGoogleDataCollection(true)
                    googlePermission.isChecked = true
                } else {
                    signOut()
                    googlePermission.isChecked = false
                    mSettingsUtil!!.setGoogleDataCollection(false)
                }
                true
            }

            val sensorPermission = findPreference("accel_data") as SwitchPreference?
            sensorPermission?.setOnPreferenceClickListener { preference ->
                val preferenceManager = preferenceManager
                if (preferenceManager.sharedPreferences.getBoolean("accel_data", true)) {
                    mServiceControl!!.startPhysicalActivityServices()
                    mSettingsUtil!!.setAccCollection(true)
                    sensorPermission.isChecked = true
                } else {
                    mServiceControl!!.stopPhysicalActivityServices()
                    val stopIntent = Intent(requireActivity(), AmossForegroundService::class.java)
                    stopIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
                    requireContext().stopService(stopIntent)
                    requireContext().stopService(Intent(context, AccelService::class.java))
                    mSettingsUtil!!.setAccCollection(false)
                    sensorPermission.isChecked = false
                }
                true
            }


            val logOutPref = findPreference("logOut") as Preference?
            logOutPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { // open browser or intent here
                logout()
                true
            }
            if (SettingsUtil(requireContext()).studyId == "MME") {
                if (!BuildConfig.DEBUG) {
                    preferenceScreen = findPreference("PreferenceScreen")
                    val myCategory = findPreference<PreferenceCategory>("accountKey")
                    preferenceScreen.removePreference(myCategory)

                    googlePermission?.isEnabled = false
                    sensorPermission?.isEnabled = false
                }
            }
        }

        private fun setSwitchValues() {
            val stepPref: SwitchPreference? = findPreference("step_data")
            val contactPref: CheckBoxPreference? = findPreference("contact_data")
            val locationPref: CheckBoxPreference? = findPreference("location_data")
            val callPref: CheckBoxPreference? = findPreference("call_data")
            val textPref: CheckBoxPreference? = findPreference("text_data")
            val storagePref: CheckBoxPreference? = findPreference("storage_data")

            val fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .build()
            if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(requireContext()), fitnessOptions)) {
                mSettingsUtil!!.setGoogleDataCollection(true)
                stepPref?.isChecked = true
            } else {
                mSettingsUtil!!.setGoogleDataCollection(false)
                stepPref?.isChecked = false
            }

            val contactPermission = ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission_group.CONTACTS)
            contactPref?.isChecked = contactPermission == PackageManager.PERMISSION_GRANTED
            val locationPermission = ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
            locationPref?.isChecked = locationPermission == PackageManager.PERMISSION_GRANTED
            val callPermission = ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_CALL_LOG)
            callPref?.isChecked = callPermission == PackageManager.PERMISSION_GRANTED
            val smsPermission = ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_SMS)
            textPref?.isChecked = smsPermission == PackageManager.PERMISSION_GRANTED
            val writePermission = ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            storagePref?.isChecked = writePermission == PackageManager.PERMISSION_GRANTED
        }

        private fun toggleCheckBox(preference: TwoStatePreference) {
            when (preference.key) {
                "accel_data" -> {
                    Log.d(TAG, "toggleCheckBox: accel")
                    if (preference.isChecked) {
                        Log.d(TAG, "toggleCheckBox: is checked ")
                        //turn on location services from phone not google api
                        mServiceControl!!.startPhysicalActivityServices()
                        mSettingsUtil!!.setAccCollection(true)
                        preference.isChecked = true
                    } else {
                        Log.d(TAG, "toggleCheckBox: is not checked ")
                        //turn off location services from phone not google api
                        mServiceControl!!.stopPhysicalActivityServices()
                        val stopIntent = Intent(requireActivity(), AmossForegroundService::class.java)
                        stopIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
                        requireContext().stopService(stopIntent)
                        requireContext().stopService(Intent(context, AccelService::class.java))
                        mSettingsUtil!!.setAccCollection(false)
                        preference.isChecked = false
                    }
                }
                "step_data" -> {
                    val fitnessOptions = FitnessOptions.builder()
                            .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                            .build()
                    if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(requireContext()), fitnessOptions)) {
                        mSettingsUtil!!.setGoogleDataCollection(true)
                        preference.isChecked = true
                    } else {
                        mSettingsUtil!!.setGoogleDataCollection(false)
                        preference.isChecked = false
                    }
                }
                "contact_data" -> {
                    val contactPermission = ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission_group.CONTACTS)
                    preference.isChecked = contactPermission == PackageManager.PERMISSION_GRANTED
                }
                "location_data" -> {
                    val locationPermission = ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    preference.isChecked = locationPermission == PackageManager.PERMISSION_GRANTED
                }
                "call_data" -> {
                    val callPermission = ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.READ_CALL_LOG)
                    preference.isChecked = callPermission == PackageManager.PERMISSION_GRANTED
                }
                "text_data" -> {
                    val smsPermission = ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.READ_SMS)
                    preference.isChecked = smsPermission == PackageManager.PERMISSION_GRANTED
                }
                "storage_data" -> {
                    val writePermission = ActivityCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    preference.isChecked = writePermission == PackageManager.PERMISSION_GRANTED
                }
            }
        }

        private fun toggleSetting(preference: TwoStatePreference) {
            when (preference.key) {
                "step_data" -> {
                    if (preference.isChecked) {
                        signIntoGoogleFitnessAPI()
                        mSettingsUtil!!.setGoogleDataCollection(true)
                        preference.isChecked = true
                    } else {
                        signOut()
                        preference.isChecked = false
                        mSettingsUtil!!.setGoogleDataCollection(false)
                    }
                }
                "accel_data" -> {
                    Log.d(TAG, "toggleSetting: accel")
                    if (preference.isChecked) {
                        Log.d(TAG, "toggleSetting: ischecked")
                        //turn on location services from phone not google api
                        mServiceControl!!.startPhysicalActivityServices()
                        mSettingsUtil!!.setAccCollection(true)
                    } else {
                        Log.d(TAG, "toggleSetting: is not checked")
                        //turn off location services from phone not google api
                        mServiceControl!!.stopPhysicalActivityServices()
                        val stopIntent = Intent(requireActivity(), AmossForegroundService::class.java)
                        stopIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
                        requireContext().stopService(stopIntent)
                        requireContext().stopService(Intent(context, AccelService::class.java))
                        mSettingsUtil!!.setAccCollection(false)
                    }
                }
                "location_data" -> {
                    if (preference.isChecked) {
                        //turn on location services from phone not google api
                        mServiceControl!!.startLocationServices()
                        mSettingsUtil!!.setLocCollection(true)
                    } else {
                        //turn off location services from phone not google api
                        mServiceControl!!.stopLocationServices()
                        mSettingsUtil!!.setLocCollection(false)
                        showSnackBar()
                    }
                }
                "call_data" -> {
                    if (preference.isChecked) {
                        //                    Toast.makeText(getActivity(), "Social is on", Toast.LENGTH_SHORT).show();
                        mServiceControl!!.startCallActivityServices()
                        mSettingsUtil!!.setCallDataCollection(true)
                    } else {
                        mServiceControl!!.stopCallActivityServices()
                        mSettingsUtil!!.setCallDataCollection(false)
                        showSnackBar()
                        //                    Toast.makeText(getActivity(), "Social is off", Toast.LENGTH_SHORT).show();
                    }
                }
                "text_data" -> {
                    if (preference.isChecked) {
                        //                    Toast.makeText(getActivity(), "Social is on", Toast.LENGTH_SHORT).show();
                        mServiceControl!!.startLIWCActivityServices()
                        mSettingsUtil!!.setLIWCDataCollection(true)
                    } else {
                        mServiceControl!!.stopLIWCActivityServices()
                        mSettingsUtil!!.setLIWCDataCollection(false)
                        showSnackBar()
                        //                    Toast.makeText(getActivity(), "Social is off", Toast.LENGTH_SHORT).show();
                    }
                }
                "restaurant_data" -> {
                    if (preference.isChecked) {
                        Toast.makeText(activity, "Restaurant is on", Toast.LENGTH_SHORT).show()
                        mSettingsUtil!!.setGoogleDataCollection(true)
                    } else {
                        Toast.makeText(activity, "Restaurant is off", Toast.LENGTH_SHORT).show()
                        mSettingsUtil!!.setGoogleDataCollection(false)
                    }
                }
            }

        }

        private fun logout() {
            AlertDialog.Builder(requireActivity())
                    .setTitle("Logout")
                    .setMessage("Are you sure want to logout?")
                    .setPositiveButton("Yes") { dialog, which ->
                        clearAllServices()
                        SettingsUtil.clear(activity)
                        SettingsUtil.addToken(
                            requireActivity().applicationContext,
                            SettingsUtil.NO_TOKEN
                        )
                        val intent1 = Intent(requireActivity().applicationContext, LoginActivity::class.java)
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent1)

                    }
                    .setNegativeButton("No") { dialog, which ->

                    }.show()

        }

        private fun clearAllServices() {
            val stopIntent = Intent(requireActivity(), AmossForegroundService::class.java)
            stopIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
            requireContext().stopService(stopIntent)
            requireContext().stopService(Intent(context, AccelService::class.java))
            requireContext().stopService(Intent(context, LocationService::class.java))
            signOut()
        }

        private fun signIntoGoogleFitnessAPI() {
            val REQUEST_OAUTH_REQUEST_CODE = 0x1001
            val fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .build()
            if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
                GoogleSignIn.requestPermissions(
                        requireActivity(),
                        REQUEST_OAUTH_REQUEST_CODE,
                        GoogleSignIn.getLastSignedInAccount(activity),
                        fitnessOptions)
            }
        }

        private fun signOut() {
            val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN)

            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(requireActivity()) { task ->
                        googleSignOutSuccessful()
                    }

        }



        private fun googleSignOutSuccessful() {
            val preference: SwitchPreference? = findPreference("step_data")
            preference!!.isChecked = false
            Toast.makeText(activity, "Successfully signed out of Google Fit API.", Toast.LENGTH_LONG).show()
            mSettingsUtil!!.setGoogleDataCollection(false)
        }

        private fun showSnackBar() {
            if (SettingsUtil(requireContext()).studyId == "MME") {
                Snackbar.make(
                    requireActivity().window.decorView.rootView,
                    R.string.permissions_disabled_mme,
                Snackbar.LENGTH_INDEFINITE).show()
            } else {
                Snackbar.make(
                    requireActivity().window.decorView.rootView,
                    R.string.permission_turned_off,
                    Snackbar.LENGTH_INDEFINITE).setAction("settings") {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }.show()
            }
        }


        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        }

        override fun onDestroy() {
            super.onDestroy()
            SettingsUtil.unregister(activity, this)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            Log.i(MainActivity.TAG, "onRequestPermissionResult")
            val serviceControl = ServiceControl(requireContext(), requireActivity())
            //for now service control class has request codes use that as reference sorry
            mSettingsUtil = SettingsUtil(activity)
            val REQUEST_OAUTH_REQUEST_CODE = 0x1001
            when (requestCode) {
                1 -> {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        mServiceControl!!.startLocationServices()
                        mSettingsUtil!!.setLocCollection(true)

                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        serviceControl.stopLocationServices()
                        mSettingsUtil!!.setLocCollection(false)
                    }
                    return
                }
                3 -> {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        mServiceControl!!.startCallActivityServices()
                        mSettingsUtil!!.setCallDataCollection(true)

                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        serviceControl.stopCallActivityServices()
                        mSettingsUtil!!.setCallDataCollection(false)
                    }
                    return
                }
                4 -> {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        mServiceControl!!.startLIWCActivityServices()
                        mSettingsUtil!!.setLIWCDataCollection(true)

                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        serviceControl.stopLIWCActivityServices()
                        mSettingsUtil!!.setLIWCDataCollection(false)
                    }
                    return
                }
                REQUEST_OAUTH_REQUEST_CODE -> {

                    OnSharedPreferenceChangeListener { _, key ->
                        when (val preference = findPreference<Preference>(key)) {
                            is CheckBoxPreference -> {
                                toggleSetting(preference)
                                if (key == "step_data") {
                                    Log.d("Test", "${preference.key} : ${preference.isChecked}")
                                }
                            }
                            is CheckBoxPreference -> {
                                toggleCheckBox(preference)
                            }
                        }
                    }

                    val stepPref = findPreference("step_data") as SwitchPreference?
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        mSettingsUtil!!.setGoogleDataCollection(true)
                        stepPref!!.isChecked = true

                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        mSettingsUtil!!.setGoogleDataCollection(false)
                        stepPref!!.isChecked = false

                    }
                    return
                }
        }
    }
}}