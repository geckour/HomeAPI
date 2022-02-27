package com.geckour.homeapi.ui.di

import androidx.preference.PreferenceManager
import com.geckour.homeapi.ui.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    viewModel { MainViewModel(get()) }
}