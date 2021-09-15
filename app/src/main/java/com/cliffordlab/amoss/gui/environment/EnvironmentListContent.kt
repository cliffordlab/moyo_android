package com.cliffordlab.amoss.gui.environment

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.app.AmossApplication.Companion.context
import com.cliffordlab.amoss.models.EnvironmentModel
import java.util.*

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
class EnvironmentListContent(realmResult: EnvironmentModel?) {

    /**
     * An array of sample (dummy) items.`
     */
    val ITEMS: MutableList<EnvironmentItem> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<Drawable, EnvironmentItem> = HashMap()
    var mRealmResults: EnvironmentModel? = realmResult
    var environmentContentHelper: EnvironmentContentHelper = EnvironmentContentHelper(mRealmResults)
    private val COUNT = 3

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createItem(i))
        }
    }

    private fun createItem(position: Int): EnvironmentItem {

        return EnvironmentItem(selectIcon(position), selectTitle(position), selectSummary(position))
    }


    private fun addItem(item: EnvironmentItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.icon!!, item)
    }

    private fun selectIcon(position: Int): Drawable? {
        return when (position) {
            1 -> {
                environmentContentHelper.getFoodIcon()
            }
            2 -> {
                environmentContentHelper.getPollutionIcon()
            }
            3 -> {
                environmentContentHelper.getWeatherIcon()
            }
            else -> ContextCompat.getDrawable(context, R.drawable.happy)
        }
    }

    private fun selectTitle(position: Int): String {
        return when (position) {
            1 -> {
                "Food Desertiness: "
            }
            2 -> {
                "Pollution: "
            }
            3 -> {
                "Weather: "
            }
            else -> {
                "something went wrong!"
            }
        }
    }

    private fun selectSummary(position: Int): String {
        return when (position) {
            1 -> {
                environmentContentHelper.getFoodDesertSummary()
            }
            2 -> {
                environmentContentHelper.getPollutionSummary()
            }
            3 -> {
                environmentContentHelper.getWeatherSummary()
            }
            else -> {
                "something went wrong!"
            }
        }
    }

    fun getItems(): List<EnvironmentItem> {
        return ITEMS
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class EnvironmentItem(val icon: Drawable?, val title: String, val summary: String)
}
