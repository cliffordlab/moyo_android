package com.cliffordlab.amoss.extensions.fileExtApi

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ChristopherWainwrightAaron on 5/22/17.
 */
object Filename {
    private const val weightExt = ".weight"
    private const val chargingExt = ".charge"
    private const val locationExt = ".loc"
    private const val smsExt = ".sms"
    private const val callExt = ".call"

    fun create(fileType: FileType): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("EST")
        val defaultDate = Date()
        val formattedDate = dateFormat.format(defaultDate)
        when (fileType) {
            FileType.CHARGE -> return "$formattedDate$chargingExt"
            FileType.LOC -> return "$formattedDate$locationExt"
            FileType.WEIGHT -> return "$formattedDate$weightExt"
            FileType.SMS -> return ""
            FileType.ACC -> return ""
            FileType.CALL -> return ""
            FileType.PLACES -> return ""
            FileType.WEATHER -> return ""
        }
    }
}