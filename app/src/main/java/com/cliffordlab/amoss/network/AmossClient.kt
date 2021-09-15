package com.cliffordlab.amoss.network

import com.cliffordlab.amoss.models.RxNormResponse
import com.cliffordlab.amoss.models.airVisual.AirVisualGPSResponse
import com.cliffordlab.amoss.models.moyoEnvironmental.EnvironmentalResponse
import com.cliffordlab.amoss.network.json.*
import com.google.gson.JsonObject
import io.reactivex.Flowable
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by ChristopherWainwrightAaron on 5/25/17.
 */

interface AmossClient {
    @POST("loginParticipant")
    fun loginParticipant(@Body request: LoginRequest): Flowable<TokenResponse>

    @POST("api/emails")
    fun loginParticipant(@Body request: EmailRequest): Flowable<EmailResponse>

    @Multipart
    @POST("api/moyo/upload_s3")
    fun upload(
            @HeaderMap headers: Map<String, String>,
            @Part file: List<MultipartBody.Part>
    ): Observable<UploadResponse>

    @Multipart
    @POST("api/moyo/mom/emory/vitals/upload")
    fun uploadVitals(
            @HeaderMap headers: Map<String, String>,
            @Part data: List<MultipartBody.Part>
    ): Observable<UploadResponse>

    @Multipart
    @POST("api/moyo/mom/emory/symptoms/upload")
    fun uploadSymptoms(
            @HeaderMap headers: Map<String, String>,
            @Part data: List<MultipartBody.Part>
    ): Observable<UploadResponse>

    @Multipart
    @POST("upload")
    fun uploadFiles(
            @HeaderMap headers: Map<String, String>,
            @Part file: List<MultipartBody.Part>
    ): Call<UploadResponse>

    @Multipart
    @POST("api/utsw/upload_s3")
    fun uploadFilesUTSW(
            @HeaderMap headers: Map<String, String>,
            @Part file: List<MultipartBody.Part>
    ): Observable<UploadResponse>

    @Multipart
    @POST("token")
    fun getEpicAccessToken(
            @Part("grant_type") grantType: RequestBody,
            @Part("code") code: RequestBody,
            @Part("redirect_uri") redirectURI: RequestBody,
            @Part("client_id") clientID: RequestBody): Flowable<EpicTokenResponse>

    @POST("default/fhirFilter")
    fun postCategoryProd(
            @HeaderMap headers: Map<String?, String?>?,
            @Body request: FhirDataRequest?): Flowable<FhirDataResponse?>?

    @POST("/prod/fhirData")
    fun postCategory(@Body request: FhirDataRequest): Flowable<FhirDataResponse>

    @GET("?")
    fun getWeather(@Query("lat") latitude: Double,
                   @Query("lon") longitude: Double,
                   @Query("key") apiKey: String): Flowable<AirVisualGPSResponse>

    @GET("query")
    fun getEnvironmentalData(@QueryMap coordinates: Map<String, String>): Flowable<EnvironmentalResponse>

    @GET("query")
    fun getEnvironmentalJson(@QueryMap coordinates: Map<String, String>): Flowable<JsonObject>



    @GET("spellingsuggestions.json")
    fun getRXSpellingSuggestions(@Query ("name") medication: String): Flowable<RxNormResponse>

}
