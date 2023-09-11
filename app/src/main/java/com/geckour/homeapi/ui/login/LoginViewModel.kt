package com.geckour.homeapi.ui.login

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geckour.homeapi.PREF_KEY_TOKEN
import com.geckour.homeapi.api.AuthService
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class LoginViewModel(
    private val json: Json,
    private val sharedPreferences: SharedPreferences,
    private val authService: AuthService,
) : ViewModel() {

    internal var data: LoginData by mutableStateOf(LoginData(success = false, isLoading = false, error = null))
        private set

    init {
        sharedPreferences.edit { remove(PREF_KEY_TOKEN) }
    }

    private fun onFailure(throwable: Throwable) {
        if (throwable is CancellationException) return

        data = data.copy(isLoading = false, error = throwable)
        Timber.e(throwable)
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            data = data.copy(isLoading = true, error = null)
            runCatching {
                authService.getAccessToken(
                    username = username,
                    password = password
                )
            }
                .onSuccess {
                    sharedPreferences.edit {
                        putString(PREF_KEY_TOKEN, json.encodeToString(it))
                    }
                    data = data.copy(isLoading = false, success = true)
                }
                .onFailure { onFailure(it) }
        }
    }

    data class LoginData(
        val success: Boolean,
        val isLoading: Boolean = false,
        val error: Throwable? = null,
    )
}