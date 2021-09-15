package com.cliffordlab.amoss.models

import io.realm.RealmObject

open class EnvironmentModel : RealmObject() {
    var pollutionStatus: String? = null
    var foodDesertinessIndex: Double = 0.toDouble()
    var weatherIcon: String? = null
    var weatherTemp: Double = 0.toDouble()
    var weatherSummary: String? = null
}
