package com.geckour.homeapi.ui.di

import androidx.preference.PreferenceManager
import com.geckour.homeapi.api.APIService
import com.geckour.homeapi.ui.MainViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create

val viewModelModule = module {
    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    @OptIn(ExperimentalSerializationApi::class)
    single {
        Retrofit.Builder()
            .client(OkHttpClient.Builder().build())
            .baseUrl("http://192.168.10.101:3000")
            .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
            .build()
            .create<APIService>()
    }

    viewModel { MainViewModel(get(), get()) }
}