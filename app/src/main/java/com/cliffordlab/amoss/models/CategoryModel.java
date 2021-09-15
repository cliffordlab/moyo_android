package com.cliffordlab.amoss.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by user on 7/27/17.
 */

    public class CategoryModel extends RealmObject {
        @SerializedName("Number")
        public int number;

        @SerializedName("Name")
        public String name;
    }

