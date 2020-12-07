package com.geckour.homeapi.api

import com.geckour.homeapi.api.model.Data
import com.geckour.homeapi.api.model.EnvironmentalData
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface APIService {

    @FormUrlEncoded
    @POST("light/ceiling")
    suspend fun ceilingLight(
        @Field("command") command: String
    )

    @GET("environmental")
    suspend fun getEnvironmentalData(): Data<EnvironmentalData>
}

enum class CeilingLightCommand(val rawValue: String) {
    ALL_ON("all-on"),
    ON("on"),
    NIGHT_ON("night-on"),
    OFF("off"),
}