package com.cliffordlab.amoss.network.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ChristopherWainwrightAaron on 3/21/17.
 */

public class UploadResponse {
    @SerializedName("success")
    public String success;

    @SerializedName("partial success")
    public String partialSuccess;

    @SerializedName("token error")
    public String tokenError;

    @SerializedName("new token")
    public String newToken;

    @SerializedName("alt ID")
    public long altID;

    @SerializedName("logout user")
    public String logoutUser;
}
