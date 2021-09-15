package com.cliffordlab.amoss.models.airVisual;

import com.google.gson.annotations.SerializedName;

public class AirVisualWeather {
    @SerializedName("ts")
    public String timestamp;
    @SerializedName("tp")
    public int tempCelsius;
    @SerializedName("pr")
    public int pressure;
    @SerializedName("hu")
    public int humidity;
    @SerializedName("ws")
    public float windSpeed;
    @SerializedName("wd")
    public int windDirection;
}
