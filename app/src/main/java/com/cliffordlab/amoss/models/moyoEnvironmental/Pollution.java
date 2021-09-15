package com.cliffordlab.amoss.models.moyoEnvironmental;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pollution {

    @SerializedName("AQI")
    @Expose
    public long aQI;
    @SerializedName("ActionDay")
    @Expose
    public boolean actionDay;
    @SerializedName("Category")
    @Expose
    public Category category;
    @SerializedName("DateForecast")
    @Expose
    public String dateForecast;
    @SerializedName("DateIssue")
    @Expose
    public String dateIssue;
    @SerializedName("Discussion")
    @Expose
    public String discussion;
    @SerializedName("Latitude")
    @Expose
    public double latitude;
    @SerializedName("Longitude")
    @Expose
    public double longitude;
    @SerializedName("ParameterName")
    @Expose
    public String parameterName;
    @SerializedName("ReportingArea")
    @Expose
    public String reportingArea;
    @SerializedName("StateCode")
    @Expose
    public String stateCode;

}