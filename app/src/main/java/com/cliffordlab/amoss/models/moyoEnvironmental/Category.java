package com.cliffordlab.amoss.models.moyoEnvironmental;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("Name")
    @Expose
    public String name;
    @SerializedName("Number")
    @Expose
    public long number;

}