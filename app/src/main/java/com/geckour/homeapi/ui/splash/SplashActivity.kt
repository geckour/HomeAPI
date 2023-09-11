package com.geckour.homeapi.ui.splash

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geckour.homeapi.PREF_KEY_TOKEN
import com.geckour.homeapi.model.OAuthToken
import com.geckour.homeapi.ui.login.LoginActivity
import com.geckour.homeapi.ui.main.MainActivity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.get
import java.util.Date
import java.util.Locale

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = get<SharedPreferences>().getString(PREF_KEY_TOKEN, null)?.let { get<Json>().decodeFromString<OAuthToken>(it) }
        val intent =
            if (token == null || SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssXXX", Locale.JAPAN).parse(token.accessTokenExpiresAt).before(Date())) {
                LoginActivity.newIntent(this)
            } else {
                MainActivity.newIntent(this)
            }

        startActivity(intent)
    }
}