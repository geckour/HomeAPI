package com.geckour.homeapi.util

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

fun AppCompatActivity.getRequestPermissionsLauncher(onGrant: ((permission: String) -> Unit)? = null) = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
    results.forEach { (permission, isGranted) ->
        Timber.d("$permission is ${if (isGranted.not()) "not " else ""}granted")
        if (isGranted) onGrant?.invoke(permission)
    }
}