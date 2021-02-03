package com.geckour.homeapi.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.geckour.homeapi.api.APIService
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

    private val _data = MutableLiveData<MainData>()
    internal val data: LiveData<MainData> = _data.distinctUntilChanged()

    private val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()

    @OptIn(ExperimentalSerializationApi::class)
    private val apiService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("http://192.168.10.101:3000")
        .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
        .build()
        .create<APIService>()

    internal val items = listOf(
        RequestData("👀 SOURCE DIRECT") { sendAmp(AmpCommand.MODE_SOURCE_DIRECT) },
        RequestData("🎯 COAXIAL") { sendAmp(AmpCommand.SELECT_COAXIAL) },
        RequestData("🎯 RECORDER") { sendAmp(AmpCommand.SELECT_RECORDER) },
        RequestData("🎯 TUNER") { sendAmp(AmpCommand.SELECT_TUNER) },
        RequestData("🎯 NETWORK") { sendAmp(AmpCommand.SELECT_NETWORK) },
        RequestData("🎯 CD") { sendAmp(AmpCommand.SELECT_CD) },
        RequestData("🎯 PHONO") { sendAmp(AmpCommand.SELECT_PHONO) },
        RequestData("🎯 OPTICAL") { sendAmp(AmpCommand.SELECT_OPTICAL) },
        RequestData("🙈 ミュート") { sendAmp(AmpCommand.VOL_TOGGLE_MUTE) },
        RequestData("🔼 ボリューム") { sendAmp(AmpCommand.VOL_UP) },
        RequestData("🔽 ボリューム") { sendAmp(AmpCommand.VOL_DOWN) },
        RequestData("🔌 アンプ電源") { sendAmp(AmpCommand.TOGGLE_POWER) },
        RequestData(null),
        RequestData("🌟 全灯") { sendCeilingLight(CeilingLightCommand.ALL_ON) },
        RequestData("💡 点灯") { sendCeilingLight(CeilingLightCommand.ON) },
        RequestData("🌚 常夜灯") { sendCeilingLight(CeilingLightCommand.NIGHT_ON) },
        RequestData("🌑 消灯") { sendCeilingLight(CeilingLightCommand.OFF) },
    )

    private var pendingRequest: Job? = null

    internal fun cancelPendingRequest() {
        pendingRequest?.cancel()
    }

    private fun onFailure(throwable: Throwable) {
        if (throwable is CancellationException) return

        _data.value = MainData(error = throwable)
        Timber.e(throwable)
    }

    private fun sendCeilingLight(command: CeilingLightCommand) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            _data.value = MainData(isLoading = true)
            runCatching { apiService.ceilingLight(command.rawValue) }
                .onFailure { onFailure(it) }
                .also { _data.value = (_data.value ?: MainData()).copy(isLoading = false) }
        }
    }

    internal fun requestEnvironmentalData() {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            _data.value = MainData(isLoading = true)
            kotlin.runCatching { apiService.getEnvironmentalData() }
                .onFailure { onFailure(it) }
                .onSuccess { _data.value = MainData(environmentalData = it.data) }
                .also { _data.value = (_data.value ?: MainData()).copy(isLoading = false) }
        }
    }

    private fun sendAmp(command: AmpCommand) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            _data.value = MainData(isLoading = true)
            runCatching { apiService.amp(command.rawValue) }
                .onFailure { onFailure(it) }
                .also { _data.value = (_data.value ?: MainData()).copy(isLoading = false) }
        }
    }

    data class MainData(
        val environmentalData: EnvironmentalData? = null,
        val isLoading: Boolean = false,
        val error: Throwable? = null,
    )
}