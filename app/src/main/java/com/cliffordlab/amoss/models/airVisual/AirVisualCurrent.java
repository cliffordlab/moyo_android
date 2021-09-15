package com.cliffordlab.amoss.models.airVisual;

import com.google.gson.annotations.SerializedName;

public class AirVisualCurrent {
    @SerializedName("weather")
    public AirVisualWeather mAirVisualWeather;
    @SerializedName("pollution")
    public AirVisualPollution mAirVisualPollution;
}
