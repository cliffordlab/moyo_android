package com.cliffordlab.amoss.network.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ChristopherWainwrightAaron on 2/25/18.
 */

public class EpicTokenResponse {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("patient")
    private String patient;


    @SerializedName("expires_in")
    private Long timeToLive;

    public EpicTokenResponse(String accessToken, String patient, Long timeToLive) {
        this.accessToken = accessToken;
        this.patient = patient;
        this.timeToLive = timeToLive;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public Long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Long timeToLive) {
        this.timeToLive = timeToLive;
    }
}
