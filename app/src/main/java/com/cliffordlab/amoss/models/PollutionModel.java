package com.cliffordlab.amoss.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by CoreyShaw on 7/23/17.
 */

public class PollutionModel extends RealmObject {
    public String getDataIssue() {return dataIssue;}

    public void setDataIssue(String dataIssue) {
        this.dataIssue = dataIssue;
    }

    public String getDataForecast() {
        return dataForecast;
    }

    public void setDataForecast(String dataForecast) {
        this.dataForecast = dataForecast;}

    public String getReportingArea() {
        return reportingArea;
    }

    public void setReportingArea(String reportingArea) {
        this.reportingArea = reportingArea;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(Integer latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(Integer longitude) {
        this.longitude = longitude;
    }

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public Boolean getActionDay() {
        return actionDay;
    }

    public void setActionDay(Boolean actionDay) {
        this.actionDay = actionDay;
    }

    public String getDiscussion() {
        return discussion;
    }

    public void setDiscussion(String discussion) {
        this.discussion = discussion;
    }

    @SerializedName("DataIssue")
    public String dataIssue;
    @SerializedName("DataForecast")
    public String dataForecast;
    @SerializedName("ReportingArea")
    public String reportingArea;
    @SerializedName("StateCode")
    public String stateCode;
    @SerializedName("Latitude")
    public double latitude;
    @SerializedName("Longitude")
    public double longitude;
    @SerializedName("AQI")
    public Integer aqi;
    @SerializedName("ActionDay")
    public Boolean actionDay;
    @SerializedName("Discussion")
    public String discussion;
    @SerializedName("Category")
    @Expose
    public CategoryModel category;
}


