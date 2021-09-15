package com.cliffordlab.amoss.network.json;

/**
 * Created by tonynguyen on 2/27/18.
 */

public class FhirDataResponse {
    private String error;
    private String success;


    FhirDataResponse() {

    }
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }



}
