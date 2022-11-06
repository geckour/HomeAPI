package com.geckour.homeapi.ui.splash

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geckour.homeapi.PREF_KEY_TOKEN
import com.geckour.homeapi.ui.login.LoginActivity
import com.geckour.homeapi.ui.main.MainActivity
import org.koin.android.ext.android.get

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val token = get<SharedPreferences>().getString(PREF_KEY_TOKEN, null)
        val intent = if (token == null) {
            LoginActivity.newIntent(this)
        } else {
            MainActivity.newIntent(this)
        }

        startActivity(intent)
    }
}