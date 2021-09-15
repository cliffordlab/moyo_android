package com.cliffordlab.amoss.gui.charts.graphData

import android.util.Log
import com.cliffordlab.amoss.app.AmossApplication
import io.realm.Realm

class GraphRealmRepo {
    companion object {
        private const val TAG = "GraphRealmRepo"
    }

    fun saveScoreToRealm(survey: String, totalScore: Int) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm1 ->
            val graphModel = realm1.createObject(GraphDataModel::class.java)
            Log.i(TAG, "saveScoreToRealm: total score $totalScore")
            when (survey) {
                "moodZoom" -> { graphModel.moodZoomScore = totalScore }
                "moodSwipe" -> { graphModel.moodSwipeScore = totalScore }
//                "kccq" -> { graphModel.kCCQScore = totalScore }
            }
        }
        realm.close()
    }

    fun savePHQ9ScoreToRealm(totalScore: Double) {
        Realm.init(AmossApplication.context)
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm1 ->
            realm1.where(GraphDataModel::class.java).findAll()
            val graphModel = realm1.createObject(GraphDataModel::class.java)
            graphModel.pHQ9Score = totalScore
        }
        realm.close()
    }
}
