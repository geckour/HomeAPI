package com.geckour.homeapi.ui.main

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator

fun Context.haptic() {
    if (Build.VERSION.SDK_INT > 32) {
        getSystemService(Vibrator::class.java)?.vibrate(
            VibrationEffect
                .startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK)
                .compose(),
            VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)
        )
    }
}