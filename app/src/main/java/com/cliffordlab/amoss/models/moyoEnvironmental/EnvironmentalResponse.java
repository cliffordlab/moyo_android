package com.cliffordlab.amoss.models.moyoEnvironmental;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EnvironmentalResponse {

    @SerializedName("desertiness_index")
    @Expose
    public double desertinessIndex;
    @SerializedName("pollution")
    @Expose
    public Pollution pollution;
    @SerializedName("server_version")
    @Expose
    public String serverVersion;
    @SerializedName("weather")
    @Expose
    public Weather weather;

}