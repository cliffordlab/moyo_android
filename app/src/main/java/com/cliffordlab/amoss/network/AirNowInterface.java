package com.cliffordlab.amoss.network;

import com.cliffordlab.amoss.models.PollutionModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by user on 7/26/17.
 */

public interface AirNowInterface {
    @GET("?")
    Call<List<PollutionModel>> doGetAQI(@Query("latitude") Integer latitude,
                                        @Query("longitude") Integer longitude,
                                        @Query("date") String date,
                                        @Query("distance") Integer distance,
                                        @Query("format") String format,
                                        @Query("api_key") String api_key);
}