package com.geckour.homeapi.ui.main

import android.content.SharedPreferences
import android.net.wifi.WifiManager
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geckour.homeapi.PREF_KEY_TEMPERATURE
import com.geckour.homeapi.api.APIService
import com.geckour.homeapi.api.AmpCommand
import com.geckour.homeapi.api.CeilingLightCommand
import com.geckour.homeapi.api.model.Co2Log
import com.geckour.homeapi.api.model.EnvironmentalData
import com.geckour.homeapi.api.model.EnvironmentalLog
import com.geckour.homeapi.api.model.SoilHumidityLog
import com.geckour.homeapi.model.RequestData
import com.geckour.homeapi.util.isInHome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val sharedPreferences: SharedPreferences,
    private val wifiManager: WifiManager,
    private val apiServiceForWifi: APIService,
    private val apiServiceForMobile: APIService
) : ViewModel() {

    internal val data = MutableStateFlow(MainData(temperature = sharedPreferences.getFloat(PREF_KEY_TEMPERATURE, 20f)))

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
        RequestData("🌚", "停止") { sendAirCond(0) },
        RequestData("🏜", "除湿") { sendAirCond(2) },
        RequestData("🆒", "冷房") { sendAirCond(3) },
        RequestData("🏮", "暖房") { sendAirCond(4) },
        RequestData("🌬", "送風") { sendAirCond(6) },
    )
    private val ampItems = listOf(
        RequestData("🐘", "ボリューム増") { sendAmp(AmpCommand.VOL_UP) },
        RequestData("🐜", "ボリューム減") { sendAmp(AmpCommand.VOL_DOWN) },
        RequestData("🙉", "ミュート") { sendAmp(AmpCommand.VOL_TOGGLE_MUTE) },
        RequestData("🔌", "アンプ電源") { sendAmp(AmpCommand.TOGGLE_POWER) },
        RequestData("🖖", "S/PDIF 4") { sendAmp(AmpCommand.SELECT_SPDIF_4) },
        RequestData("🤟", "S/PDIF 3") { sendAmp(AmpCommand.SELECT_SPDIF_3) },
        RequestData("✌️", "S/PDIF 2") { sendAmp(AmpCommand.SELECT_SPDIF_2) },
        RequestData("☝️", "S/PDIF 1") { sendAmp(AmpCommand.SELECT_SPDIF_1) },
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
        Page.Screen.CEILING_LIGHT to ceilingLightItems,
        Page.Screen.AIR_COND to airCondItems,
        Page.Screen.AMP to ampItems,
    )

    private var pendingRequest: Job? = null

    private fun cancelPendingRequest() {
        pendingRequest?.cancel()
    }

    private fun onFailure(throwable: Throwable) {
        if (throwable is CancellationException) return

        data.value = data.value.copy(isLoading = false, error = throwable)
        Timber.e(throwable)
    }

    private fun sendCeilingLight(command: CeilingLightCommand) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data.value = data.value.copy(isLoading = true, error = null)
            runCatching {
                (if (wifiManager.isInHome()) apiServiceForWifi else apiServiceForMobile)
                    .ceilingLight(
                        roomId = data.value.room.id,
                        command = command.rawValue
                    )
            }
                .onSuccess { data.value = data.value.copy(isLoading = false) }
                .onFailure { onFailure(it) }
        }
    }

    internal fun requestEnvironmentalData() {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data.value = data.value.copy(isLoading = true, error = null)
            runCatching {
                (if (wifiManager.isInHome()) apiServiceForWifi else apiServiceForMobile)
                    .getEnvironmentalData()
            }
                .onFailure { onFailure(it) }
                .onSuccess { data.value = data.value.copy(isLoading = false, environmentalData = it.data) }
        }
    }

    internal fun requestLogDialogData(end: Long = System.currentTimeMillis() / 1000, start: Long = end - 86400) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data.value = data.value.copy(isLoading = true, error = null)
            runCatching {
                val environmentalLog = requestEnvironmentalLog(null, end, start)
                val soilHumidityLog = requestSoilHumidityLog(null, end, start)
                val co2Log = requestCo2Log(null, end, start)
                data.value = data.value.copy(
                    isLoading = false,
                    environmentalLogData = environmentalLog,
                    soilHumidityLogData = soilHumidityLog,
                    co2LogData = co2Log
                )
            }.onFailure { onFailure(it) }
        }
    }

    internal fun requestLogDialogDataWithRange(range: Dialog.Range, end: Long = System.currentTimeMillis() / 1000) {
        requestLogDialogData(end = end, start = end - range.duration)
    }

    private suspend fun requestEnvironmentalLog(id: String?, end: Long, start: Long): List<EnvironmentalLog> =
        (if (wifiManager.isInHome()) apiServiceForWifi else apiServiceForMobile)
            .getEnvironmentalLog(
                id = id,
                end = end,
                start = start
            ).data

    private suspend fun requestSoilHumidityLog(id: String?, end: Long, start: Long): List<SoilHumidityLog> =
        (if (wifiManager.isInHome()) apiServiceForWifi else apiServiceForMobile)
            .getSoilHumidityLog(
                id = id,
                end = end,
                start = start
            ).data

    private suspend fun requestCo2Log(id: String?, end: Long, start: Long): List<Co2Log> =
        (if (wifiManager.isInHome()) apiServiceForWifi else apiServiceForMobile)
            .getCo2Log(
                id = id,
                end = end,
                start = start
            ).data

    internal fun sendSignal(signal: Boolean) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            runCatching {
                (if (wifiManager.isInHome()) apiServiceForWifi else apiServiceForMobile)
                    .signalLight(command = if (signal) 1 else 0)
            }.onFailure { onFailure(it) }
        }
    }

    internal fun clearEnvironmentalData() {
        data.value = data.value.copy(environmentalData = null)
    }

    internal fun clearEnvironmentalLogData() {
        data.value = data.value.copy(environmentalLogData = null)
    }

    internal fun upTemperature() {
        saveTemperature(data.value.temperature + 0.5f)
    }

    internal fun downTemperature() {
        saveTemperature(data.value.temperature - 0.5f)
    }

    private fun saveTemperature(temperature: Float) {
        sharedPreferences.edit(commit = true) { putFloat(PREF_KEY_TEMPERATURE, temperature) }
        data.value = data.value.copy(temperature = temperature)
    }

    internal fun setRoom(room: Bar.Room) {
        data.value = data.value.copy(room = room)
    }

    private fun sendAirCond(runMode: Int) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data.value = data.value.copy(isLoading = true, error = null)
            runCatching {
                (if (wifiManager.isInHome()) apiServiceForWifi else apiServiceForMobile)
                    .airCond(
                        runMode = runMode,
                        temperature = sharedPreferences.getFloat(
                            PREF_KEY_TEMPERATURE, 20f
                        )
                    )
            }
                .onSuccess { data.value = data.value.copy(isLoading = false) }
                .onFailure { onFailure(it) }
        }
    }

    private fun sendAmp(command: AmpCommand) {
        cancelPendingRequest()
        pendingRequest = viewModelScope.launch {
            data.value = data.value.copy(isLoading = true, error = null)
            runCatching {
                (if (wifiManager.isInHome()) apiServiceForWifi else apiServiceForMobile)
                    .amp(command = command.rawValue)
            }
                .onSuccess { data.value = data.value.copy(isLoading = false) }
                .onFailure { onFailure(it) }
        }
    }

    data class MainData(
        val room: Bar.Room = Bar.Room.LIVING,
        val environmentalData: EnvironmentalData? = null,
        val environmentalLogData: List<EnvironmentalLog>? = null,
        val soilHumidityLogData: List<SoilHumidityLog>? = null,
        val co2LogData: List<Co2Log>? = null,
        val temperature: Float = 20f,
        val isLoading: Boolean = false,
        val error: Throwable? = null,
    )
}