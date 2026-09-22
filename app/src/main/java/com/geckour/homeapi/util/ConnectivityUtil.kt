package com.geckour.homeapi.util

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun ConnectivityManager.isInHomeFlow(): Flow<Boolean> = callbackFlow {
    val isWifiConnected = activeNetwork?.let { getNetworkCapabilities(it) }?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    if (isWifiConnected.not()) {
        trySend(false)
        awaitClose {}
        return@callbackFlow
    }

    val request = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()
    val callback = object : NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val wifiInfo = (networkCapabilities.transportInfo as? WifiInfo)

            trySend(wifiInfo.isInHome)
        }
    }

    registerNetworkCallback(request, callback)
    awaitClose {
        unregisterNetworkCallback(callback)
    }
}

val WifiInfo?.isInHome: Boolean
    get() {
        if (this == null) return false

        return bssid.lowercase() == "1c:7c:98:78:d8:0f"
    }