package com.geckour.homeapi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    companion object {

        fun newIntent(context: Context) = Intent(context, BootReceiver::class.java)

        internal fun activityRecognitionPermissionApproved(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (activityRecognitionPermissionApproved(context)) {
            ActivityRecognition.getClient(context)
                .requestSleepSegmentUpdates(
                    SleepReceiver.newSleepReceiverPendingIntent(context),
                    SleepSegmentRequest.getDefaultSleepSegmentRequest()
                )
                .addOnSuccessListener { Timber.d("Succeeded to start subscribing sleep event.") }
                .addOnFailureListener { e -> Timber.e(e) }
        }
    }
}