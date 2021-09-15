package com.cliffordlab.amoss.extensions.fileExtApi

/**
 * Created by ChristopherWainwrightAaron on 5/22/17.
 */
object FileHeaders {
    private const val locationHeader = "time,latitude,longitude"
    private const val weightHeader = "weight/lbs,date"
    private const val chargeHeader = "is_charging,time"
    private const val smsHeader = "category/meta=value"
    private const val callHeader = "hashed_ph_number,call_type,call_date,call_duration/sec"
    private const val accHeader = "time,x,y,z"
    private const val googlePlacesHeader = "Timestamp,Name,Address,Estab.#,Likelihood"
    private const val googleWeatherHeader = "Timestamp,Temperature,Feels Like,Dew,Humidity,Cond #"

    fun header(fileType: FileType): String {
        when (fileType) {
            FileType.CHARGE -> return chargeHeader
            FileType.LOC -> return locationHeader
            FileType.WEIGHT -> return weightHeader
            FileType.CALL -> return callHeader
            FileType.SMS -> return smsHeader
            FileType.ACC -> return accHeader
            FileType.WEATHER -> return googleWeatherHeader
            FileType.PLACES -> return googlePlacesHeader
        }
    }
}