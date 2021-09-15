package com.cliffordlab.amoss.datacollector.accel;

/**
 * Created by ChristopherWainwrightAaron on 7/19/17.
 */

//class for storing accelerometer data
public class Accelerometer {
    private final long timeValue;
    private final double xValue;
    private final double yValue;
    private final double zValue;

    public Accelerometer(long timeValue, double xValue, double yValue, double zValue) {
        this.timeValue = timeValue;
        this.xValue = xValue;
        this.yValue = yValue;
        this.zValue = zValue;
    }

    public long getTimeValue() {
        return timeValue;
    }

    public double getxValue() {
        return xValue;
    }

    public double getyValue() {
        return yValue;
    }

    public double getzValue() {
        return zValue;
    }

    protected float transformRawData() {
        double sumOfSquares = (Math.pow(xValue, 2.0) + Math.pow(yValue, 2.0) + Math.pow((zValue - 9.8), 2.0));
        double rootSOS = Math.sqrt(sumOfSquares);
        double activityVal = rootSOS + 1;
        return (float) Math.floor(activityVal * 1000) / 1000;
    }


} // end of accelerometer class
