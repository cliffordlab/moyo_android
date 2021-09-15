package com.cliffordlab.amoss.network

import com.cliffordlab.amoss.BuildConfig
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by ChristopherWainwrightAaron on 5/25/17.
 */
object AmossNetwork {
    lateinit var client: AmossClient
    private var httpClient: OkHttpClient.Builder

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        httpClient = OkHttpClient.Builder()
                .connectTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .addInterceptor(logging)

        changeBaseURL(BuildConfig.apiBase)
    }

    fun changeBaseURL(url: String) {
        val builder = Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        val retrofit = builder.client(httpClient.build()).build()
        this.client = retrofit.create(AmossClient::class.java)
    }
}