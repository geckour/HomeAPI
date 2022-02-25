package com.geckour.homeapi.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.geckour.homeapi.api.APIService
import com.geckour.homeapi.api.AirCondCommand
import com.geckour.homeapi.api.AmpCommand
import com.geckour.homeapi.api.CeilingLightCommand
import com.geckour.homeapi.api.model.EnvironmentalData
import com.geckour.homeapi.model.RequestData
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import timber.log.Timber

class MainViewModel : ViewModel() {

    internal var data: MainData by mutableStateOf(MainData())
        private set

    private val ceilingLightItems = listOf(
        RequestData("🌑", "消灯") { sendCeilingLight(CeilingLightCommand.OFF) },
        RequestData("🌚", "常夜灯") { sendCeilingLight(CeilingLightCommand.NIGHT_ON) },
        RequestData("💡", "点灯") { sendCeilingLight(CeilingLightCommand.ON) },
        RequestData("🌟", "全灯") { sendCeilingLight(CeilingLightCommand.ALL_ON) },
        RequestData("💥", "全灯 (強)") { sendCeilingLight(CeilingLightCommand.HIGH) },
        RequestData("", "", null),
        RequestData("🏙", "寒色") { sendCeilingLight(CeilingLightCommand.COOLER) },
        RequestData("🌇", "暖色") { sendCeilingLight(CeilingLightCommand.WARMER) },
        RequestData("🌥", "暗く") { sendCeilingLight(CeilingLightCommand.DARKER) },
        RequestData("☀️", "明るく") { sendCeilingLight(CeilingLightCommand.BRIGHTER) },
    )
    private val airCondItems = listOf(
        RequestData("🌚", "停止") { sendAirCond(AirCondCommand.STOP) },
        RequestData("🏮", "暖房") { sendAirCond(AirCondCommand.HEATER) },
    )
    private val ampItems = listOf(
        RequestData("🖖", "S/PDIF 4") { sendAmp(AmpCommand.SELECT_SPDIF_4) },
        RequestData("🤟", "S/PDIF 3") { sendAmp(AmpCommand.SELECT_SPDIF_3) },
        RequestData("✌️", "S/PDIF 2") { sendAmp(AmpCommand.SELECT_SPDIF_2) },
        RequestData("☝️", "S/PDIF 1") { sendAmp(AmpCommand.SELECT_SPDIF_1) },
        RequestData("🐘", "ボリューム増") { sendAmp(AmpCommand.VOL_UP) },
        RequestData("🐜", "ボリューム減") { sendAmp(AmpCommand.VOL_DOWN) },
        RequestData("🙉", "ミュート") { sendAmp(AmpCommand.VOL_TOGGLE_MUTE) },
        RequestData("🔌", "アンプ電源") { sendAmp(AmpCommand.TOGGLE_POWER) },
        RequestData("💡", "OPTICAL") { sendAmp(AmpCommand.SELECT_OPTICAL) },
        RequestData("⚡", "COAXIAL") { sendAmp(AmpCommand.SELECT_COAXIAL) },
        RequestData("📽", "RECORDER") { sendAmp(AmpCommand.SELECT_RECORDER) },
        RequestData("📻", "TUNER") { sendAmp(AmpCommand.SELECT_TUNER) },
        RequestData("🕸", "NETWORK") { sendAmp(AmpCommand.SELECT_NETWORK) },
        RequestData("💿", "CD") { sendAmp(AmpCommand.SELECT_CD) },
        RequestData("🍥", "PHONO") { sendAmp(AmpCommand.SELECT_PHONO) },
        RequestData("🍣", "SOURCE DIRECT") { sendAmp(AmpCommand.MODE_TOGGLE_SOURCE_DIRECT) },
    )
    internal val items = mapOf(
        Screen.CEILING_LIGHT to ceilingLightItems,
        Screen.AIR_COND to airCondItems,
        Screen.AMP to ampItems,
    )

    private val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()

    @OptIn(ExperimentalSerializationApi::class)
    private val apiService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("http://192.168.10.101:3000")
        .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
        .build()
        .create<APIService>()

    private var pendingRequest: Job? = null

    private fun cancelPendingRequest() {
        pendingRequest?.cancel()
    }

    private fun onFailure(throwable: Throwable) {
        if (throwable is CancellationException) return

        data = MainData(error = throwable)
        Timber.e(throwable)
    }

    private fun sendCeilingLight(command: CeilingLightCommand) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data = MainData(isLoading = true)
            runCatching { apiService.ceilingLight(command.rawValue) }
                .onSuccess { data = MainData() }
                .onFailure { onFailure(it) }
        }
    }

    internal fun requestEnvironmentalData() {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data = MainData(isLoading = true)
            kotlin.runCatching { apiService.getEnvironmentalData() }
                .onFailure { onFailure(it) }
                .onSuccess { data = MainData(environmentalData = it.data) }
        }
    }

    internal fun clearEnvironmentalData() {
        data = MainData()
    }

    private fun sendAirCond(command: AirCondCommand) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data = MainData(isLoading = true)
            runCatching { apiService.airCond(command.rawValue) }
                .onSuccess { data = MainData() }
                .onFailure { onFailure(it) }
        }
    }

    private fun sendAmp(command: AmpCommand) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data = MainData(isLoading = true)
            runCatching { apiService.amp(command.rawValue) }
                .onSuccess { data = MainData() }
                .onFailure { onFailure(it) }
        }
    }

    data class MainData(
        val environmentalData: EnvironmentalData? = null,
        val isLoading: Boolean = false,
        val error: Throwable? = null,
    )

    enum class Screen(val title: String) {
        CEILING_LIGHT("天井灯"),
        AIR_COND("エアコン"),
        AMP("アンプ"),
    }
}