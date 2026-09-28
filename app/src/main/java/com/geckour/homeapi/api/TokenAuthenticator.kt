package com.geckour.homeapi.api

import android.content.SharedPreferences
import androidx.core.content.edit
import com.geckour.homeapi.PREF_KEY_TOKEN
import com.geckour.homeapi.model.OAuthToken
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sharedPreferences: SharedPreferences,
    private val json: Json,
    private val authService: AuthService,
) : Authenticator {

    // Refreshes one at a time, since the server revokes a refresh token (and the access token paired with it) once it's used
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Gives up if the request has been rejected even with the refreshed token
        if (response.priorResponse?.code == 401) return null

        val token = synchronized(lock) {
            val storedToken = sharedPreferences.getString(PREF_KEY_TOKEN, null)
                ?.let { json.decodeFromString<OAuthToken>(it) }
                ?: return null

            // Another request has already refreshed the token while this one was in flight
            if (response.request.header("Authorization") != "Bearer ${storedToken.accessToken}") {
                return@synchronized storedToken
            }

            authService.refreshAccessToken(refreshToken = storedToken.refreshToken)
                .execute()
                .body()
                ?.also {
                    // Writes synchronously, since the old refresh token is no longer usable once the server has rotated it
                    sharedPreferences.edit(commit = true) { putString(PREF_KEY_TOKEN, json.encodeToString(it)) }
                }
                ?: return null
        }

        return response.request
            .newBuilder()
            .header("Authorization", "Bearer ${token.accessToken}")
            .build()
    }
}
