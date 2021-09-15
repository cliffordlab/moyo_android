package com.cliffordlab.amoss.models.airVisual;

import com.google.gson.annotations.SerializedName;

public class AirVisualData {
    @SerializedName("city")
    public String city;
    @SerializedName("state")
    public String state;
    @SerializedName("country")
    public String country;
    @SerializedName("current")
    public AirVisualCurrent currentWeather;
}
