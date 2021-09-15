package com.cliffordlab.amoss.models

import io.realm.RealmObject

open class BPModel ( var arm: String? = "", var vitals: String? = "", var createdAt: String? = ""): RealmObject()
open class SymptomsModel (var symptoms: String? = "", var createdAt: String? = "") : RealmObject()

data class VitalItems (var title: String? = "", var vitals: String? = "", var createdAt: String? = "")
