package com.geckour.homeapi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import com.geckour.homeapi.api.CeilingLightCommand
import com.google.android.gms.location.SleepClassifyEvent
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import timber.log.Timber

class SleepReceiver : BroadcastReceiver() {

    companion object {

        fun newSleepReceiverPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, SleepReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
    }

    private val scope: CoroutineScope = MainScope()
    private val component: SleepReceiverComponent by lazy { SleepReceiverComponent() }

    @OptIn(ExperimentalSerializationApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        if (SleepClassifyEvent.hasEvents(intent)) {
            val events = SleepClassifyEvent.extractEvents(intent)
            Timber.d("Sleep events: $events")
            FirebaseAnalytics.getInstance(context)
                .logEvent(
                    "sleep",
                    bundleOf(
                        "hasEvents" to true,
                        "events" to events.toString()
                    )
                )
            if (events.any { it.confidence > 50 }) {
                Timber.d("Sleep had been detected.")
                FirebaseAnalytics.getInstance(context)
                    .logEvent(
                        "sleep",
                        bundleOf("sleeping" to true)
                    )

                val currentTemperature = component.sharedPreferences.getFloat(PREF_KEY_TEMPERATURE, 20f)

                scope.launch {
                    component.apiService.ceilingLight(CeilingLightCommand.OFF.rawValue)
                    component.apiService.airCond(0, currentTemperature)
                }
            } else {
                FirebaseAnalytics.getInstance(context)
                    .logEvent(
                        "sleep",
                        bundleOf("sleeping" to false)
                    )
            }
        } else {
            Timber.d("Sleep events cannot be extracted from Intent.")
            FirebaseAnalytics.getInstance(context)
                .logEvent(
                    "sleep",
                    bundleOf("hasEvents" to false)
                )
        }
    }
}