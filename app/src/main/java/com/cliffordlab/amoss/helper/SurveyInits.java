package com.cliffordlab.amoss.helper;

import io.realm.RealmObject;

/**
 * Created by ChristopherWainwrightAaron on 8/5/17.
 */

public class SurveyInits extends RealmObject {
    private boolean hasCompletedMoodSwipe = false;
    private boolean hasCompletedPromis = false;
    private boolean hasCompletedQLesQ = false;
    private boolean hasCompletedPainMeasurement = false;
    private boolean hasCompletedMoodZoom = false;

    public boolean hasCompletedAllpCRFSurveys() {
        boolean[] surveyCompleteArr = new boolean[] {
                hasCompletedMoodSwipe, hasCompletedPromis, hasCompletedQLesQ, hasCompletedPainMeasurement, hasCompletedMoodSwipe};

        for (Boolean complete: surveyCompleteArr) {
            if (!complete) {
                return false;
            }
        }
        return true;
    }

    public boolean hasCompletedMoodSwipe() {
        return hasCompletedMoodSwipe;
    }

    public void setHasCompletedMoodSwipe(boolean hasCompletedMoodSwipe) {
        this.hasCompletedMoodSwipe = hasCompletedMoodSwipe;
    }

    public boolean hasCompletedPromis() {
        return hasCompletedPromis;
    }

    public void setHasCompletedPromis(boolean hasCompletedPromis) {
        this.hasCompletedPromis = hasCompletedPromis;
    }

    public boolean hasCompletedQLesQ() {
        return hasCompletedQLesQ;
    }

    public void setHasCompletedQLesQ(boolean hasCompletedQLesQ) {
        this.hasCompletedQLesQ = hasCompletedQLesQ;
    }

    public boolean hasCompletedPainMeasurement() {
        return hasCompletedPainMeasurement;
    }

    public void setHasCompletedPainMeasurement(boolean hasCompletedPainMeasurement) {
        this.hasCompletedPainMeasurement = hasCompletedPainMeasurement;
    }

    public boolean hasCompletedMoodZoom() {
        return hasCompletedMoodZoom;
    }

    public void setHasCompletedMoodZoom(boolean hasCompletedMoodZoom) {
        this.hasCompletedMoodZoom = hasCompletedMoodZoom;
    }
}
