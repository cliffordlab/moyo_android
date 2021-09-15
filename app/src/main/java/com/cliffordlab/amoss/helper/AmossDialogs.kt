package com.cliffordlab.amoss.helper

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.cliffordlab.amoss.R

class AmossDialogs {
    fun showDialog(activity: String, context: Context) {
        val dialog = Dialog(context)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.disabled_dialog)
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_bg)
        val textView20 = dialog.findViewById<TextView>(R.id.textView20)

        when (activity) {
            "activity" ->
                textView20.text = "Activity is Disabled. Please re-enable activity data collection in settings to see step count graph."
            "environment" ->
                textView20.text = "Environment is Disabled. Please re-enable environment data collection in settings to see local environmental data."
            "social" ->
                textView20.text = "Social is Disabled. Please re-enable social data collection in settings to see social history graph."
            "google" ->
                textView20.text = "Signing into Google is necessary to view your step count history."
            "logout" ->
                textView20.text = "You are currently logged in from another device. This application is meant to be used with only a single device at a time per account. Please log back in if this is your primary device."
            "activityDescription" ->
                textView20.text = "TBD."
            "environmentDescription" ->
                textView20.text = "Why are we giving you this info? Your health is affected by your environment. Pollution, weather, and food options are big influences on your day-to-day behaviours and long term health. By looking at your mood and other things this app measures(like movement) you may see a relationship between your environment and your well being..."
            "foodDescription" ->
                textView20.text = "We are making an app to guess what you are eating. By taking a photo and writing down what you eat, it helps us build that app."
            "moodDescription" ->
                textView20.text = "TBD."
            "socialDescription" ->
                textView20.text = "Why are we collecting this info? The number of people you interact with and how often you do so reflects your state of mind. By looking at these changes over time you may notice when you are more sociable and perhaps even work out what makes you happier..."
            "vitalsDescription" ->
                textView20.text = "We are making an app to automatically read your blood pressure and heart rate from the photo. By taking a photo and selecting the right blood pressure and heart rate you will help us create this new app and save everyone time..."
            "momVitalsDescription" ->
                textView20.text = "Moyo Mom is a mobile app for recording and tracking daily blood pressure information.\n\n It can also be used to record symptoms that may be caused by high blood pressure."
            "epicDescription" ->
                textView20.text = "Sync EMR data via MyChart"
        }

        val cancelView = dialog.findViewById<Button>(R.id.cancelView)

        // Your android custom dialog ok action
        // Action for custom dialog ok button click
        cancelView.setOnClickListener { view -> dialog.dismiss() }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}