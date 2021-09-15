package com.cliffordlab.amoss.helper

import android.content.Intent
import android.graphics.drawable.Drawable

data class ListItem(val drawable: Drawable?, val details: String, val takeSurveyIntent: Intent, val surveyGraphIntent: Intent)