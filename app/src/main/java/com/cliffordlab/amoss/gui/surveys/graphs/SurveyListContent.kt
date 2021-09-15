package com.cliffordlab.amoss.gui.surveys.graphs

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.app.AmossApplication.Companion.context
import com.cliffordlab.amoss.gui.MainActivity
import com.cliffordlab.amoss.gui.surveys.*
import com.cliffordlab.amoss.helper.ListItem
import com.cliffordlab.amoss.helper.logger.Log
import com.cliffordlab.amoss.settings.SettingsUtil
import java.util.*

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object SurveyListContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<ListItem> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    private var studyID = ""
    private lateinit var intent: Intent

    val surveyMap = mutableMapOf(
            "MUQ SURVEY" to Intent(context, MUQActivity::class.java),
            "GSQ SURVEY" to Intent(context, GSQActivity::class.java),
            "GAD7 SURVEY" to Intent(context, GAD7Activity::class.java),
            "MOOD SWIPE" to Intent(context, MoodSwipeActivity::class.java),
            "MOOD ZOOM" to Intent(context, MoodZoomActivity::class.java),
            "PHQ9 SURVEY" to Intent(context, PHQNineActivity::class.java),
            "KCCQ SURVEY" to Intent(context, KCCQActivity::class.java),
            "PROMIS SURVEY" to Intent(context, PROMISActivity::class.java),
            "PCL5 SURVEY" to Intent(context, PCL5Activity::class.java),
            "QLESQ SURVEY" to Intent(context, QLESQActivity::class.java),
            "SUDS SURVEY" to Intent(context, SUDSActivity::class.java),
            "WEIGHT SURVEY" to Intent(context, WeightActivity::class.java),
            "PAIN SURVEY" to Intent(context, PainActivity::class.java)
    )

    init {
        // Add some sample items.
        studyID = SettingsUtil(context).studyId
        when (studyID) {
            "MME" -> {
                surveyMap.remove("PAIN SURVEY")
                surveyMap.remove("MOOD SWIPE")
            }
            "hf" -> {
                surveyMap.remove("KCCQ SURVEY")
                surveyMap.remove("PROMIS SURVEY")
                surveyMap.remove("PCL5 SURVEY")
                surveyMap.remove("QLESQ SURVEY")
                surveyMap.remove("SUDS SURVEY")
                surveyMap.remove("WEIGHT SURVEY")
                surveyMap.remove("PAIN SURVEY")
            }
            "schizophrenia" -> {
                surveyMap.remove("PAIN SURVEY")
                surveyMap.remove("MOOD SWIPE")
            }
            "MOYO MOM MOREHOUSE?????" -> {
                surveyMap.remove("PAIN SURVEY")
                surveyMap.remove("MOOD SWIPE")
            }
            "Any other studies?????" -> {
                surveyMap.remove("PAIN SURVEY")
                surveyMap.remove("MOOD SWIPE")
            }
        }

        Log.d("MoodListContent", surveyMap.toString())

        surveyMap.forEach { (surveyName, activityIntent) ->
            addItem(createSurveyItem(surveyName, activityIntent))
        }
    }

    private fun addItem(item: ListItem) {
        ITEMS.add(item)
    }

    private fun createSurveyItem(survey: String, intent: Intent): ListItem {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return ListItem(getTaskIcon(survey), survey, intent, getButtonViewChart(survey))
    }

    private fun getTaskIcon(survey: String): Drawable? {
        // find drawable
        return if (survey == "MOOD ZOOM" || survey == "MOOD SWIPE") {
            ContextCompat.getDrawable(context, R.drawable.happy)
        } else {
            ContextCompat.getDrawable(context, R.drawable.checklist)
        }
    }

    private fun getButtonViewChart(survey: String): Intent {

        return when (survey) {
            "MOOD SWIPE" -> {
                intent = Intent(context, SurveyGraph::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("survey", "MOOD SWIPE")
            }
            "MOOD ZOOM" -> {
                intent = Intent(context, SurveyGraph::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("survey", "MOOD ZOOM")
            }
            "PHQ9" -> {
                intent = Intent(context, SurveyGraph::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("survey", "PHQ9")
            }
            else -> {
                intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }


}
