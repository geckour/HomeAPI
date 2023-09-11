package com.geckour.homeapi.ui.di

import android.content.SharedPreferences
import android.net.wifi.WifiManager
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.geckour.homeapi.PREF_KEY_TOKEN
import com.geckour.homeapi.api.APIService
import com.geckour.homeapi.api.AuthService
import com.geckour.homeapi.model.OAuthToken
import com.geckour.homeapi.ui.login.LoginViewModel
import com.geckour.homeapi.ui.main.MainViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
val viewModelModule = module {
    single {
        Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        }
    }

    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    single {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                get<SharedPreferences>().getString(PREF_KEY_TOKEN, null)?.let { get<Json>().decodeFromString<OAuthToken>(it) }?.let {
                    return@addInterceptor chain.proceed(
                        chain.request()
                            .newBuilder()
                            .addHeader("Authorization", "Bearer ${it.accessToken}")
                            .build()
                    )
                }
                return@addInterceptor chain.proceed(chain.request())
            }
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.code() == 401) {
                    val sharedPreferences = get<SharedPreferences>()
                    val json = get<Json>()
                    val refreshToken = sharedPreferences.getString(PREF_KEY_TOKEN, null)?.let {
                        json.decodeFromString<OAuthToken>(it).refreshToken
                    }

                    refreshToken?.let {
                        get<AuthService>().refreshAccessToken(refreshToken = it).execute().body()?.let { token ->
                            sharedPreferences.edit { putString(PREF_KEY_TOKEN, json.encodeToString(token)) }
                            response.close()
                            return@addInterceptor chain.proceed(
                                chain.request()
                                    .newBuilder()
                                    .removeHeader("Authorization")
                                    .addHeader("Authorization", "Bearer ${token.accessToken}")
                                    .build()
                            )
                        }
                    }
                }
                return@addInterceptor response
            }
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl("https://api.geckour.com:5775")
            .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
            .build()
            .create<APIService>()
    }

    single {
        Retrofit.Builder()
            .client(OkHttpClient.Builder().build())
            .baseUrl("https://api.geckour.com:5775")
            .addConverterFactory(Json.asConverterFactory(MediaType.get("application/json")))
            .build()
            .create<AuthService>()
    }

    single {
        androidContext().getSystemService<WifiManager>()
    }

    viewModel { LoginViewModel(get(), get(), get()) }

    viewModel { MainViewModel(get(), get(), get()) }
}