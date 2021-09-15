package com.cliffordlab.amoss.helper

import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.adapters.MainAdapter

/**
 * Created by ChristopherWainwrightAaron on 1/25/16.
 */
class MenuOptions {
    var optionName: String? = null
    var image: Int = 0
        private set

    fun setResId(position: Int, studyId: String) {
        when (studyId) {
            "MME" -> {
                MainAdapter.Position.MOM_VITALS = 0
                MainAdapter.Position.SETTINGS = 1
                MainAdapter.Position.SIZE = 2

                MainAdapter.Position.ACTIVITY_GRAPH = 3
                MainAdapter.Position.ENVIRONMENT = 4
                MainAdapter.Position.FOOD_DIARY = 5
                MainAdapter.Position.MOOD_LIST = 6
                MainAdapter.Position.SOCIAL_GRAPH = 7
                MainAdapter.Position.VITALS = 8
                MainAdapter.Position.SEND_EPIC = 9
                MainAdapter.Position.SOCIAL_GRAPH = 10
            }
            else -> {
            }
        }
        image = when (position) {
            MainAdapter.Position.ACTIVITY_GRAPH //0
            -> R.drawable.activity
            MainAdapter.Position.ENVIRONMENT //0
            -> R.drawable.environment
            MainAdapter.Position.FOOD_DIARY //0
            -> R.drawable.food
            MainAdapter.Position.MOOD_LIST //0
            -> R.drawable.mood
            MainAdapter.Position.SOCIAL_GRAPH //0
            -> R.drawable.social
            MainAdapter.Position.VITALS //0
            -> R.drawable.vitals
            MainAdapter.Position.MOM_VITALS //0
            -> R.drawable.heart_beat
            MainAdapter.Position.SEND_EPIC
            -> R.drawable.graph
//            MainAdapter.Position.MOOD_SWIPE //0
//            -> R.drawable.happy
//            MainAdapter.Position.MOOD_ZOOM  //1
//            -> R.drawable.checklist
//            MainAdapter.Position.MOOD_PHQ9 //2
//            -> R.drawable.checklist
////            MainAdapter.Position.ACTIVITY_GRAPH
////            -> R.drawable.graph
//            MainAdapter.Position.BALL_GAME //4
//            -> R.drawable.ball
            MainAdapter.Position.SETTINGS //6
            -> R.drawable.settings
            else -> 0
        }

    }


}
