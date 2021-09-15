package com.cliffordlab.amoss.models.airVisual

import com.google.gson.annotations.SerializedName

class AirVisualGPSResponse {
    @SerializedName("data")
    var mAirVisualData: AirVisualData? = null
}