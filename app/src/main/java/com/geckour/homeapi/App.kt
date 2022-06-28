package com.geckour.homeapi

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.geckour.homeapi.ui.di.viewModelModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    internal var isWiFiConnected: Boolean = false

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            loadKoinModules(viewModelModule)
        }

        Timber.plant(Timber.DebugTree())

        get<ConnectivityManager>().registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build(),
            object : ConnectivityManager.NetworkCallback() {

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities)

                    isWiFiConnected = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                }
            }
        )
    }
}