package com.cliffordlab.amoss.gui.charts.graphData

import io.realm.RealmObject

open class GraphDataModel(
        var moodZoomScore: Int? = null,
        var kCCQScore: Int? = null,
        var moodSwipeScore: Int? = null,
        var pHQ9Score: Double? = null

) : RealmObject()
