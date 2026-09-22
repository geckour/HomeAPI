package com.geckour.homeapi

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.geckour.homeapi.ui.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            loadKoinModules(viewModelModule)
        }

        Timber.plant(Timber.DebugTree())

        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(NOTIFICATION_CHANNEL_ID_WIND_ALERT, "強風予報", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }
}