package com.geckour.homeapi.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import com.geckour.homeapi.api.CeilingLightCommand
import com.geckour.homeapi.receiver.di.SleepReceiverComponent
import com.geckour.homeapi.ui.main.Bar
import com.google.android.gms.location.SleepClassifyEvent
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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

    override fun onReceive(context: Context, intent: Intent) {
        FirebaseAnalytics.getInstance(context)
            .logEvent(
                "sleep",
                bundleOf(
                    "packageName" to (intent.`package` ?: "null"),
                    "dataUri" to (intent.data ?: "null")
                )
            )
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
                Timber.d("Sleep was detected.")
                FirebaseAnalytics.getInstance(context)
                    .logEvent(
                        "sleep",
                        bundleOf("sleeping" to true)
                    )

                scope.launch {
                    Bar.Room.values().forEach { component.apiService.ceilingLight(it.id, CeilingLightCommand.OFF.rawValue) }
                }
            } else {
                FirebaseAnalytics.getInstance(context)
                    .logEvent(
                        "sleep",
                        bundleOf("sleeping" to false)
                    )
            }
        } else {
            Timber.d("Sleep events cannot be extracted from the Intent.")
            FirebaseAnalytics.getInstance(context)
                .logEvent(
                    "sleep",
                    bundleOf("hasEvents" to false)
                )
        }
    }
}