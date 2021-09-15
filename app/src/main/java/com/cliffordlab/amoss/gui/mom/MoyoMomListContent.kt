package com.cliffordlab.amoss.gui.mom

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.app.AmossApplication
import com.cliffordlab.amoss.gui.MainActivity
import com.cliffordlab.amoss.gui.vitals.VitalsHistoryActivity
import com.cliffordlab.amoss.helper.ListItem
import com.cliffordlab.amoss.models.BPModel
import com.cliffordlab.amoss.models.SymptomsModel
import io.realm.Realm
import java.util.*

object MoyoMomListContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<ListItem> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    private lateinit var intent: Intent

    val map = mutableMapOf(
            "RECORD VITALS" to Intent(AmossApplication.context, MomVitalsActivity::class.java),
            "RECORD SYMPTOMS" to Intent(AmossApplication.context, MomSymptomsActivity::class.java)
    )

    init {

        map.forEach { (activity, activityIntent) ->
            addItem(createItem(activity, activityIntent))
        }
    }

    private fun addItem(item: ListItem) {
        ITEMS.add(item)
    }

    private fun createItem(activityName: String, intent: Intent): ListItem {
        var activityNameTitle = activityName
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val realm = Realm.getDefaultInstance();

        if (activityName == "RECORD VITALS") {
            val results = realm.where(BPModel::class.java).findAll()
            if (results != null && results.size > 0) {
                activityNameTitle = activityName + "\nLast recorded vitals: " + "\n" + results.last()!!.createdAt
            }
        } else {
            val results = realm.where(SymptomsModel::class.java).findAll()
            if (results != null && results.size > 0) {
                activityNameTitle = activityName + "\nLast reported symptoms: " + "\n" + results.last()!!.createdAt
            }
        }
        realm.close()
        return ListItem(getTaskIcon(activityName), activityNameTitle, intent, getButtonViewChart(activityName))
    }

    private fun getTaskIcon(activityName: String): Drawable? {
        // find drawable

        return if (activityName == "RECORD VITALS") {
            ContextCompat.getDrawable(AmossApplication.context, R.drawable.heart_beat)
        } else {
            ContextCompat.getDrawable(AmossApplication.context, R.drawable.checklist)
        }
    }

    private fun getButtonViewChart(activityName: String): Intent {

        return when (activityName) {
            "RECORD VITALS" -> {
                intent = Intent(AmossApplication.context, VitalsHistoryActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("activityType", "RECORD VITALS")
            }
            "RECORD SYMPTOMS" -> {
                intent = Intent(AmossApplication.context, VitalsHistoryActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("activityType", "RECORD SYMPTOMS")
            }
            else -> {
                intent = Intent(AmossApplication.context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
