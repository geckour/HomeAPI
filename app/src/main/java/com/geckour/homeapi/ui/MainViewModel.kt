package com.geckour.homeapi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.geckour.homeapi.api.APIService
import com.geckour.homeapi.api.CeilingLightCommand
import com.geckour.homeapi.model.RequestData
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import timber.log.Timber

class MainViewModel : ViewModel() {

    private val okHttpClient = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()
    private val apiService = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("http://192.168.10.101:3000")
        .build()
        .create<APIService>()

    internal val items = listOf(
        RequestData("🌟 全灯") { sendCeilingLightAllOn() },
        RequestData("💡 点灯") { sendCeilingLightOn() },
        RequestData("🌚 常夜灯") { sendCeilingLightNightOn() },
        RequestData("🌑 消灯") { sendCeilingLightOff() },
    )

    private fun sendCeilingLightAllOn() {
        viewModelScope.launch {
            runCatching { apiService.ceilingLight(CeilingLightCommand.ALL_ON.rawValue) }
                .onFailure { Timber.e(it) }
        }
    }

    private fun sendCeilingLightOn() {
        viewModelScope.launch {
            runCatching { apiService.ceilingLight(CeilingLightCommand.ON.rawValue) }
                .onFailure { Timber.e(it) }
        }
    }

    private fun sendCeilingLightNightOn() {
        viewModelScope.launch {
            runCatching { apiService.ceilingLight(CeilingLightCommand.NIGHT_ON.rawValue) }
                .onFailure { Timber.e(it) }
        }
    }

    internal fun sendCeilingLightOff() {
        viewModelScope.launch {
            runCatching { apiService.ceilingLight(CeilingLightCommand.OFF.rawValue) }
                .onFailure { Timber.e(it) }
        }
    }
}