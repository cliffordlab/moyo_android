package com.cliffordlab.amoss.models;

import io.realm.RealmObject;

/**
 * Created by ChristopherWainwrightAaron on 7/24/17.
 */

public class PlacesModel extends RealmObject {
    //timestamp when snapshot recorded
    private long timestamp;
    private String name;
    private String placeType;
    private float placeLikelihood;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPlaceLikelihood() {
        return placeLikelihood;
    }

    public void setPlaceLikelihood(float placeLikelihood) {
        this.placeLikelihood = placeLikelihood;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }
}
