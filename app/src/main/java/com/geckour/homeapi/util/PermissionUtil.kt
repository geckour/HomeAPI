package com.geckour.homeapi.util

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

fun AppCompatActivity.getRequestPermissionLauncher(onGrant: (() -> Unit)? = null) = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
    Timber.d("Permission is ${if (isGranted.not()) "not" else ""} granted")
    if (isGranted) onGrant?.invoke()
}