package com.cliffordlab.amoss.network.json;

/**
 * Created by ChristopherWainwrightAaron on 9/25/17.
 */

public class EmailResponse {
    private String success;
    private String failure;

    public EmailResponse(String success, String failure) {
        this.success = success;
        this.failure = failure;
    }


    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }
}
