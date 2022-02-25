package com.geckour.homeapi.api

import com.geckour.homeapi.api.model.Data
import com.geckour.homeapi.api.model.EnvironmentalData
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

    @FormUrlEncoded
    @POST("air-cond")
    suspend fun airCond(
        @Field("command") command: String
    )

    @FormUrlEncoded
    @POST("amp")
    suspend fun amp(
        @Field("command") command: String
    )
}

enum class CeilingLightCommand(val rawValue: String) {
    HIGH("ceiling-light_high"),
    ALL_ON("ceiling-light_all-on"),
    ON("ceiling-light_on"),
    NIGHT_ON("ceiling-light_night-on"),
    OFF("ceiling-light_off"),
    WARMER("ceiling-light_warmer"),
    COOLER("ceiling-light_cooler"),
    BRIGHTER("ceiling-light_brighter"),
    DARKER("ceiling-light_darker"),
}

enum class AirCondCommand(val rawValue: String) {
    HEATER("air-cond_heater"),
    STOP("air-cond_stop"),
}

enum class AmpCommand(val rawValue: String) {
    TOGGLE_POWER("amp_toggle_power"),
    VOL_UP("amp_vol_up"),
    VOL_DOWN("amp_vol_down"),
    VOL_TOGGLE_MUTE("amp_vol_toggle_mute"),
    SELECT_OPTICAL("amp_select_optical"),
    SELECT_PHONO("amp_select_phono"),
    SELECT_CD("amp_select_cd"),
    SELECT_NETWORK("amp_select_network"),
    SELECT_TUNER("amp_select_tuner"),
    SELECT_RECORDER("amp_select_recorder"),
    SELECT_COAXIAL("amp_select_coaxial"),
    MODE_TOGGLE_SOURCE_DIRECT("amp_mode_toggle_source-direct"),
    SELECT_SPDIF_1("spdif_select_1"),
    SELECT_SPDIF_2("spdif_select_2"),
    SELECT_SPDIF_3("spdif_select_3"),
    SELECT_SPDIF_4("spdif_select_4"),
}