package com.cliffordlab.amoss.adapters

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.app.AmossApplication
import com.cliffordlab.amoss.gui.SettingsActivity
import com.cliffordlab.amoss.gui.activity.AccelGraphActivity
import com.cliffordlab.amoss.gui.environment.EnvironmentActivity
import com.cliffordlab.amoss.gui.epicfhir.MyChartActivity
import com.cliffordlab.amoss.gui.food.FoodDiaryActivity
import com.cliffordlab.amoss.gui.mom.MoyoMomActivity
import com.cliffordlab.amoss.gui.social.SocialGraphActivity
import com.cliffordlab.amoss.gui.surveys.SurveyListActivity
import com.cliffordlab.amoss.gui.vitals.VitalsActivity
import com.cliffordlab.amoss.helper.AmossDialogs
import com.cliffordlab.amoss.helper.MenuOptions
import com.cliffordlab.amoss.settings.SettingsUtil
import kotlinx.android.synthetic.main.main_viewholder.view.*

/**
 * Created by ChristopherWainwrightAaron on 1/25/16.
 */
class MainAdapter(private val mContext: FragmentActivity, private val mData: List<MenuOptions>) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {
    private var mFragmentManager: FragmentManager? = null
    private var settingsUtil: SettingsUtil? = null
    private var lastPosition = -1
    private var selectedPosition: Int = 0
    private var progressBar: ProgressBar? = null
    internal object Position {
        var ACTIVITY_GRAPH = 0
        var ENVIRONMENT = 1
        var FOOD_DIARY = 2
        var MOOD_LIST = 3
        var SOCIAL_GRAPH = 4
        var VITALS = 5
        var MOM_VITALS = 6
        var SEND_EPIC = 7
        var SETTINGS = 8
        var SIZE = SETTINGS + 1
    }

    companion object {
        private const val TAG = "MainAdapter"
    }

    init {
        when (SettingsUtil(mContext).studyId) {
            "MME" -> {
                Position.MOM_VITALS = 0
                Position.SETTINGS = 1
                Position.SIZE = 2

                Position.ACTIVITY_GRAPH = 3
                Position.ENVIRONMENT = 4
                Position.FOOD_DIARY = 5
                Position.MOOD_LIST = 6
                Position.SOCIAL_GRAPH = 7
                Position.VITALS = 8
                Position.SEND_EPIC = 9
                Position.SOCIAL_GRAPH = 10
            }
            else -> {

            }
        }
    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.isClickable = true
            itemView.setOnClickListener { v -> handleTouch() }
            itemView.descriptionIcon.setOnClickListener { displayDescription() }
        }

        private fun displayDescription() {
            val intent: Intent
            val util = SettingsUtil(mContext)
            val position = layoutPosition
            val dialog = AmossDialogs()
            toggleSelected(position)
            when (position) {

                Position.ACTIVITY_GRAPH -> {
                    dialog.showDialog("activityDescription", mContext)
                }

                Position.ENVIRONMENT -> {
                    dialog.showDialog("environmentDescription", mContext)
                }

                Position.FOOD_DIARY -> {
                    dialog.showDialog("foodDescription", mContext)
                }

                Position.MOOD_LIST -> {
                    dialog.showDialog("moodDescription", mContext)
                }

                Position.SOCIAL_GRAPH -> {
                    dialog.showDialog("socialDescription", mContext)
                }

                Position.VITALS -> {
                    dialog.showDialog("vitalsDescription", mContext)
                }

                Position.MOM_VITALS -> {
                    dialog.showDialog("momVitalsDescription", mContext)
                }

                Position.SEND_EPIC -> {
                    dialog.showDialog("epicDescription", mContext)
                }

            }
        }

        private fun handleTouch() {
            val intent: Intent
            val util = SettingsUtil(mContext)
            val position = layoutPosition
            val dialog = AmossDialogs()
            toggleSelected(position)

            when (position) {

                Position.ACTIVITY_GRAPH -> {
                    if (util.isGoogleDataCollectionEnabled) {
                    val intentGraph = Intent(mContext, AccelGraphActivity::class.java)
//                        val intentGraph = Intent(mContext, StepCountGraphActivity::class.java)
                        mContext.startActivity(intentGraph)
                    } else {
                        dialog.showDialog("activity", mContext)
                    }
                }

                Position.ENVIRONMENT -> {

                    val locationPermissionState = ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    val locationCoarsePermissionSTate = ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION)

                    if (util.isLocCollectionEnabled && (locationPermissionState == PackageManager.PERMISSION_GRANTED || locationCoarsePermissionSTate == PackageManager.PERMISSION_GRANTED)) {
                        val intentGraph = Intent(mContext, EnvironmentActivity::class.java)
                        mContext.startActivity(intentGraph)
                    } else {
                        dialog.showDialog("environment", mContext)
                    }
                }

                Position.FOOD_DIARY -> {
                    val intentGraph = Intent(mContext, FoodDiaryActivity::class.java)
                    mContext.startActivity(intentGraph)
                }

                Position.MOOD_LIST -> {
                    val intentGraph = Intent(mContext, SurveyListActivity::class.java)
                    mContext.startActivity(intentGraph)
                }

                Position.SOCIAL_GRAPH -> {
                    val callPermissionState = ActivityCompat.checkSelfPermission(
                        AmossApplication.context,
                            Manifest.permission.READ_CALL_LOG)
                    val textPermissionState = ActivityCompat.checkSelfPermission(AmossApplication.context,
                            Manifest.permission.READ_SMS)

                    if (callPermissionState == PackageManager.PERMISSION_GRANTED && textPermissionState == PackageManager.PERMISSION_GRANTED) {
                        val intentGraph = Intent(mContext, SocialGraphActivity::class.java)
                        mContext.startActivity(intentGraph)
                    } else {
                        dialog.showDialog("social", mContext)
                        Toast.makeText(mContext, "Please turn on Social Networking Data Collection in Settings.", Toast.LENGTH_LONG).show()
                    }
                }

                Position.VITALS -> {
                    val intentGraph = Intent(mContext, VitalsActivity::class.java)
                    mContext.startActivity(intentGraph)
                }

                Position.MOM_VITALS -> {
                    val intentGraph = Intent(mContext, MoyoMomActivity::class.java)
                    mContext.startActivity(intentGraph)
                }
                Position.SEND_EPIC -> {
                    if (SettingsUtil(mContext).epicTokenCreationTime == 0L) {
                        SettingsUtil(mContext).fhirCode = "no fhir code"
                        intent = Intent(mContext, MyChartActivity::class.java)
                        mContext.startActivity(intent)
                    } else {
                        val tokenStartTime = SettingsUtil(mContext).epicTokenCreationTime
                        val currentTime = System.currentTimeMillis()
                        val diff = currentTime - tokenStartTime
                        Log.i(TAG, "handleTouch: diff: $currentTime - $tokenStartTime")
                        Log.i(TAG, "handleTouch: $diff")
                        //58 minutes aka diff < 3480000
                        Toast.makeText(mContext, "Logged into MyChart!", Toast.LENGTH_LONG).show()

                    }
                }

                Position.SETTINGS -> {
                    intent = Intent(mContext, SettingsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mContext.startActivity(intent)
                }
//                Position.BALL_GAME -> {
//                    val confirm = BaseDialogConfirm()
//                    confirm.show(mFragmentManager!!, "string")
//                }
                else -> {
                }
            }
        }

        private fun toggleSelected(position: Int) {
            notifyItemChanged(selectedPosition)
            selectedPosition = position
            notifyItemChanged(selectedPosition)
        }

        fun bindOption(current: MenuOptions, rowImageView: ImageView, rowTextView: TextView) {
            if (current.image != 0) rowImageView.setImageResource(current.image)
            rowTextView.text = current.optionName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val optionRow = LayoutInflater.from(parent.context).inflate(R.layout.main_viewholder, parent, false)
        mFragmentManager = mContext.supportFragmentManager
        return MainViewHolder(optionRow)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bindOption(mData[position], holder.itemView.reImageView, holder.itemView.user_option)
        if (holder.itemView.user_option.text == "SETTINGS") {
            holder.itemView.descriptionIcon.visibility = View.INVISIBLE
        }
        holder.itemView.isSelected = selectedPosition == position
        setAnimation(holder.itemView.container, position)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    private fun setAnimation(viewToAnimate: View?, position: Int) {
        if (position > lastPosition) {
            val anim = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left)
            viewToAnimate!!.startAnimation(anim)
            lastPosition = position
        }
    }
}
