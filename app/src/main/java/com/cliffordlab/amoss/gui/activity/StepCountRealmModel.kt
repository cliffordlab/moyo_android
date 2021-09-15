package com.cliffordlab.amoss.gui.activity

import io.realm.RealmObject

open class StepCountRealmModel(
        var stepCount: Long? = null
): RealmObject()