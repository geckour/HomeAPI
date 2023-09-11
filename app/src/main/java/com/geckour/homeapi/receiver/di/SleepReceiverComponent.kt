package com.geckour.homeapi.receiver.di

import android.content.SharedPreferences
import com.geckour.homeapi.api.APIService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SleepReceiverComponent : KoinComponent {

    val sharedPreferences: SharedPreferences by inject()
    val apiService: APIService by inject()
}