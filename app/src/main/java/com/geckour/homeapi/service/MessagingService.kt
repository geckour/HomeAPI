package com.geckour.homeapi.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.geckour.homeapi.NOTIFICATION_CHANNEL_ID_WIND_ALERT
import com.geckour.homeapi.R
import com.geckour.homeapi.api.APIService
import com.geckour.homeapi.ui.splash.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import timber.log.Timber

class MessagingService : FirebaseMessagingService() {

    private val apiService: APIService by inject()

    // Called on a background thread; blocks until the registration finishes so that the service isn't stopped midway
    override fun onNewToken(token: String) {
        runBlocking {
            runCatching {
                apiService.registerDevice(token = token, name = Build.MODEL)
            }.onFailure { Timber.e(it) }
        }
    }

    // Messages with a notification payload reach here only while the app is in the foreground; the system shows them otherwise
    override fun onMessageReceived(message: RemoteMessage) {
        val notification = message.notification ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, SplashActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        // Same tag and id as the ones the system uses, so that a newer notification for the same date replaces the older one
        NotificationManagerCompat.from(this).notify(
            notification.tag,
            0,
            NotificationCompat.Builder(this, notification.channelId ?: NOTIFICATION_CHANNEL_ID_WIND_ALERT)
                .setSmallIcon(R.drawable.ic_notification_wind_alert)
                .setContentTitle(notification.title)
                .setContentText(notification.body)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()
        )
    }
}
