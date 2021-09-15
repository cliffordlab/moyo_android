package com.cliffordlab.amoss.datacollector.accel;

/**
 * Created by ChristopherWainwrightAaron on 9/26/17.
 */

public class ActivityGraphPoints {
    private final long timeVal;
    private final float activityVal;

    public ActivityGraphPoints(long timeVal, float activityVal) {
        this.timeVal = timeVal;
        this.activityVal = activityVal;
    }

    public long getTimeVal() {
        return timeVal;
    }

    public float getActivityVal() {
        return activityVal;
    }
}
