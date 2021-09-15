
package com.cliffordlab.amoss.models.moyoEnvironmental;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Weather {

    @SerializedName("apparentTemperature")
    @Expose
    public double apparentTemperature;
    @SerializedName("cloudCover")
    @Expose
    public float cloudCover;
    @SerializedName("dewPoint")
    @Expose
    public double dewPoint;
    @SerializedName("humidity")
    @Expose
    public double humidity;
    @SerializedName("icon")
    @Expose
    public String icon;
    @SerializedName("nearestStormDistance")
    @Expose
    public long nearestStormDistance;
    @SerializedName("ozone")
    @Expose
    public double ozone;
    @SerializedName("precipIntensity")
    @Expose
    public double precipIntensity;
    @SerializedName("precipIntensityError")
    @Expose
    public double precipIntensityError;
    @SerializedName("precipProbability")
    @Expose
    public double precipProbability;
    @SerializedName("precipType")
    @Expose
    public String precipType;
    @SerializedName("pressure")
    @Expose
    public double pressure;
    @SerializedName("summary")
    @Expose
    public String summary;
    @SerializedName("temperature")
    @Expose
    public double temperature;
    @SerializedName("time")
    @Expose
    public long time;
    @SerializedName("uvIndex")
    @Expose
    public long uvIndex;
    @SerializedName("visibility")
    @Expose
    public double visibility;
    @SerializedName("windBearing")
    @Expose
    public long windBearing;
    @SerializedName("windGust")
    @Expose
    public double windGust;
    @SerializedName("windSpeed")
    @Expose
    public double windSpeed;

}