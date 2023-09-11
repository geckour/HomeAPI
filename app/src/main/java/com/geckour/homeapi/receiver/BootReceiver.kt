package com.geckour.homeapi.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.SleepSegmentRequest
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        requestSleepData(context)
    }
}

fun requestSleepData(context: Context, onRequestPermissionRequired: () -> Unit = {}) {
    if (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
        ActivityRecognition
            .getClient(context.applicationContext)
            .requestSleepSegmentUpdates(
                SleepReceiver.newSleepReceiverPendingIntent(context.applicationContext),
                SleepSegmentRequest.getDefaultSleepSegmentRequest()
            )
            .addOnSuccessListener {
                Timber.d("Succeeded to start subscribing sleep event.")
            }
            .addOnFailureListener { e ->
                Timber.e(e)
            }
    } else {
        onRequestPermissionRequired()
    }
}
