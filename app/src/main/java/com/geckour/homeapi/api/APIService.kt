package com.geckour.homeapi.api

import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface APIService {

    @FormUrlEncoded
    @POST("light/ceiling")
    suspend fun ceilingLight(
        @Field("command") command: String
    )
}

enum class CeilingLightCommand(val rawValue: String) {
    ALL_ON("all-on"),
    ON("on"),
    NIGHT_ON("night-on"),
    OFF("off"),
}