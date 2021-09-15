package com.cliffordlab.amoss.gui.environment

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import com.cliffordlab.amoss.R.*
import com.cliffordlab.amoss.app.AmossApplication.Companion.context
import com.cliffordlab.amoss.models.EnvironmentModel
import java.util.*


class EnvironmentContentHelper(realmResult: EnvironmentModel?) {
    val mRealmResults = realmResult
    companion object {
        private const val TAG = "EnvironmentContentHelpe"
    }
    var pollutionIndex: Double = 0.0

    fun getFoodIcon(): Drawable? {
        if (mRealmResults != null) {
            val indexRounded = Math.round(mRealmResults.foodDesertinessIndex * 100.0) / 100.0
            pollutionIndex = indexRounded*100
            Log.i(TAG, "getFoodIcon: this is the index: " + pollutionIndex)
            when (pollutionIndex) {
                in 0.0 .. 19.99 -> {
                    return ContextCompat.getDrawable(context, drawable.broccoli)
                }
                in 20.0 .. 39.99 -> {
                    return ContextCompat.getDrawable(context, drawable.rice)
                }
                in 40.0 .. 59.99 -> {
                    return ContextCompat.getDrawable(context, drawable.chicken)
                }
                in 60.0 .. 79.99 -> {
                    return ContextCompat.getDrawable(context, drawable.pizza)
                }
                in 80.0 .. 100.0 -> {
                    return ContextCompat.getDrawable(context, drawable.doughnut)
                }
            }
        }
        return ContextCompat.getDrawable(context, drawable.chicken)
    }


    fun getPollutionIcon(): Drawable? {
        if (mRealmResults != null) {
            val pollutionCondition = mRealmResults.pollutionStatus
            val pollutionIcon = ContextCompat.getDrawable(context, drawable.goodair)
            when (pollutionCondition) {
                "Good" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pollutionIcon!!.setTint(Color.parseColor("#00CB33"))
                    } else {
                        val color = getColor(context, color.pollutionGood)
                        pollutionIcon!!.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                    return pollutionIcon
                }
                "Moderate" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pollutionIcon!!.setTint(Color.YELLOW)
                    } else {
                        pollutionIcon!!.mutate().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN)
                    }
                    return pollutionIcon
                }
                "Unhealthy for Sensitive Groups" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pollutionIcon!!.setTint(Color.parseColor("#FE621B"))
                    } else {
                        val color = getColor(context, color.pollutionUSG)
                        pollutionIcon!!.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                    return pollutionIcon
                }
                "Unhealthy" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pollutionIcon!!.setTint(Color.RED)
                    } else {
                        pollutionIcon!!.mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                    }
                    return pollutionIcon
                }
                "Very Unhealthy" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pollutionIcon!!.setTint(Color.parseColor("#330612"))
                    } else {
                        val color = getColor(context, color.pollutionVeryUnhealthy)
                        pollutionIcon!!.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                    return pollutionIcon
                }
                "Hazardous" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pollutionIcon!!.setTint(Color.parseColor("#3E3E3E"))
                    } else {
                        val color = getColor(context, color.pollutionHazardous)
                        pollutionIcon!!.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
                    }
                    return pollutionIcon
                }
            }
        }
//        val pollutionStatus = moyoEnvironmentData.pollutionStatus
//        TODO("Figure out why AQI is coming back -1")
        return ContextCompat.getDrawable(context, drawable.pollution_icon)
    }

    fun getWeatherIcon(): Drawable? {
        if (mRealmResults != null) {
            val weatherCondition = mRealmResults.weatherIcon
            Log.i(TAG, "getWeatherIcon: this is the weather condition: " + weatherCondition)
            when (weatherCondition) {
                "clear-day" -> return ContextCompat.getDrawable(context, drawable.clear_day)
                "clear-night" -> return ContextCompat.getDrawable(context, drawable.clear_night)
                "rain" -> return ContextCompat.getDrawable(context, drawable.rain)
                "snow" -> return ContextCompat.getDrawable(context, drawable.snow)
                "sleet" -> return ContextCompat.getDrawable(context, drawable.sleet)
                "wind" -> return ContextCompat.getDrawable(context, drawable.windy)
                "fog" -> return ContextCompat.getDrawable(context, drawable.fog)
                "cloudy" -> return ContextCompat.getDrawable(context, drawable.cloudy)
                "partly-cloudy-day" -> return ContextCompat.getDrawable(context, drawable.partly_cloudy_day)
                "partly-cloudy-night" -> return ContextCompat.getDrawable(context, drawable.partly_cloudy_night)
            }
        }
        return ContextCompat.getDrawable(context, drawable.clear_day)!!
    }

    fun getFoodDesertSummary(): String {
        if (mRealmResults != null) {
            when (pollutionIndex) {
                in 0.0..39.99 -> {
                    return "The food desert index of your location is: $pollutionIndex. You are currently in an area that is NOT considered a food desert. This area has plenty of access to healthful foods through grocery stores and various resources such as farmers’ markets."
                }
                in 40.0..59.99 -> {
                    return "The food desert index of your location is: $pollutionIndex. You are currently in an area that is considered a borderline food desert. This area has more access to healthful food choices than a food desert but establishments carrying processed foods still may outnumber healthy establishments."
                }
                in 60.0..100.0 -> {
                    return "The food desert index of your location is: $pollutionIndex. You are currently in an area that is considered a food desert, with very limited access to healthful foods. This may be due to a lack of grocery stores/farmers’ markers and an abundance of processed foods."
                }
            }
        }
        return "No food desert summary available."
    }

    fun getPollutionSummary(): String {
        if (mRealmResults != null) {
            val pollutionCondition = mRealmResults.pollutionStatus.toString()
                .lowercase(Locale.getDefault())
            return if (pollutionCondition == "null") {
                "No pollution data currently available."
            } else {
                "The current pollution conditions in the area are considered $pollutionCondition."
            }
        }
        return "No pollution data available."
    }

    fun getWeatherSummary(): String {
        if (mRealmResults != null) {
            val weatherTemp = mRealmResults.weatherTemp
            val weatherSummary = mRealmResults.weatherSummary
            return "Current Temperature: $weatherTemp.\nSummary: $weatherSummary."
        }
        return "No weather summary available."
    }
}
