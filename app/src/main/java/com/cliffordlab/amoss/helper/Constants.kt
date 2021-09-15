package com.cliffordlab.amoss.helper

/**
 * Created by michael on 2/2/16.
 */
object Constants {
    class ACTION {
        companion object {
            const val STOP_ACCEL_ACTION = "STOP_ACCEL_ACTION" // stops accel data collection
            const val STOP_CALL_ACTION = "STOP_CALL_ACTION" // stops call data collection
            const val STOP_SMS_ACTION = "STOP_SMS_ACTION" // stops LIWC data collection
            const val STOP_LOCATION_ACTION = "STOP_LOCATION_ACTION" //stops environment data collection
            const val STOPFOREGROUND_ACTION = "STOPFOREGROUND_ACTION" //stops all data collection
        }
    }


    /**
     * Replace Constants URL fields with your own
     */
    const val AMOSS_API_BASE_URL = "https://localhost:8080/"                                            // Base URL for backend API
    const val EPIC_FHIR_PROD_BASE_URL = "https://fhir.epic.com/interconnect-fhir-oauth/api/FHIR/DSTU2"  // Base URL for Epic Fhir OAuth
    const val AIRVISUAL_BASE_URL = "https://api.airvisual.com/v2/nearest_city/"                         // Base URL for AirVisual API

    /**
     * Intent Flags and notification ID's used to trigger heads up notification reminder to take surveys
     */
    const val NOTIFICATION_FLAG = "notification"
    const val MOOD_ZOOM_NOTIFICATION_ID = 0
    const val PHQ9_NOTIFICATION_ID = 1
    const val PHQ9_DAILY_NOTIFICATION_ID = 2
    const val ALL_SURVEYS_NOTIFICATION_ID = 3
    const val KCCQ_NOTIFICATION_ID = 4
    const val PTSD_DAILY_NOTIFICATION_ID = 5
    const val MOOD_SWIPE_NOTIFICATION_ID = 6
    const val DAILY_SURVEY_NOTIFICATION_ID = 7
    const val WEEKLY_SURVEY_NOTIFICATION_ID = 8

    /**
     * Filename used to store LIWC logs
     */
    const val LIWC_LOG_FILENAME = "liwcLogTime.txt"

    /**
     * Intent flag used for determining if there are more surveys for the participant to take
     */
    const val MULTISURVEYS = "multipleSurveys"
    const val MULTISURVEYSHF = "multipleSurveysHF"

}// prevents instantiation
