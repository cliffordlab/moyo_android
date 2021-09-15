package com.cliffordlab.amoss.network.json;

/**
 * Created by ChristopherWainwrightAaron on 8/3/16.
 */
public class CallLog {
    public String callLog;

    public CallLog(String log) {
        this.callLog = log;
    }

    public String getCallLog() {
        return callLog;
    }

    public void setCallLog(String callLog) {
        this.callLog = callLog;
    }
}
