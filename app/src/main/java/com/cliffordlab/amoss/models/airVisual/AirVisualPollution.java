package com.cliffordlab.amoss.models.airVisual;

import com.google.gson.annotations.SerializedName;

public class AirVisualPollution {
    @SerializedName("ts")
    public String timestamp;
    @SerializedName("mainus")
    public String mainUSPollutant;
    @SerializedName("o3")
    public AirVisualO3 mAirVisualO3;
}
