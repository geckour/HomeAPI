package com.geckour.homeapi.service

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.withRotation
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

    companion object {

        private const val MESSAGE_TYPE_WIND_ALERT = "wind_alert"
        private const val WIND_ALERT_GROUP_KEY = "wind_alert"
        private const val WIND_ALERT_SUMMARY_TAG = "wind-summary"

        // The icon depicts the wind blowing to the right (east) while the message's direction is where the wind blows from,
        // so turning the icon by the direction plus this points it to where the wind blows to
        private const val WIND_ALERT_ICON_ROTATION_OFFSET = 90f
    }

    private val apiService: APIService by inject()

    // Called on a background thread; blocks until the registration finishes so that the service isn't stopped midway
    override fun onNewToken(token: String) {
        runBlocking {
            runCatching {
                apiService.registerDevice(token = token, name = Build.MODEL)
            }.onFailure { Timber.e(it) }
        }
    }

    // The server sends data-only messages, which reach here even while the app is in the background
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data["type"] != MESSAGE_TYPE_WIND_ALERT) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val direction = data["direction"]?.toFloatOrNull()
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, SplashActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager = NotificationManagerCompat.from(this)
        // The tag is per date, so that a newer notification for the same date replaces the older one
        notificationManager.notify(
            data["tag"],
            0,
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_WIND_ALERT)
                .setSmallIcon(windAlertIcon(direction))
                // Recent systems show the app icon in place of the small icon in the shade, so the large icon shows the direction there
                .setLargeIcon(direction?.let { windAlertLargeIcon(it) })
                .setContentTitle(data["title"])
                .setContentText(data["body"])
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setGroup(WIND_ALERT_GROUP_KEY)
                .build()
        )
        // Groups the alerts by itself; otherwise the system groups them with a generic icon since their turned icons differ.
        // The summary stands for the group only while two or more alerts are shown, with the app icon
        // since the unturned wind icon would read as the wind blowing to the east.
        notificationManager.notify(
            WIND_ALERT_SUMMARY_TAG,
            0,
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_WIND_ALERT)
                .setSmallIcon(R.drawable.ic_notification_app)
                .setContentTitle(data["title"])
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setGroup(WIND_ALERT_GROUP_KEY)
                .setGroupSummary(true)
                // Leaves alerting to the alerts themselves, so that a single message doesn't alert twice
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .build()
        )
    }

    // Returns the icon turned to where the wind blows to, or the unturned one when the direction is unknown
    private fun windAlertIcon(direction: Float?): IconCompat {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_notification_wind_alert)
        if (direction == null || drawable == null) {
            return IconCompat.createWithResource(this, R.drawable.ic_notification_wind_alert)
        }

        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        Canvas(bitmap).drawTurnedWindAlertIcon(drawable, direction, Rect(0, 0, bitmap.width, bitmap.height))
        return IconCompat.createWithBitmap(bitmap)
    }

    // Returns the white icon turned to where the wind blows to on a transparent background
    private fun windAlertLargeIcon(direction: Float): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_notification_wind_alert) ?: return null

        val size = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
        val bitmap = createBitmap(size, size)
        Canvas(bitmap).drawTurnedWindAlertIcon(drawable, direction, Rect(0, 0, size, size))
        return bitmap
    }

    // The icon's paths fit in the circle inscribed in it, so turning it around the center cuts nothing off at any angle
    private fun Canvas.drawTurnedWindAlertIcon(drawable: Drawable, direction: Float, bounds: Rect) {
        withRotation(direction + WIND_ALERT_ICON_ROTATION_OFFSET, bounds.exactCenterX(), bounds.exactCenterY()) {
            drawable.bounds = bounds
            drawable.draw(this)
        }
    }
}
