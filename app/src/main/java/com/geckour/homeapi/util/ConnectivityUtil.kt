package com.geckour.homeapi.util

import android.net.wifi.WifiManager

fun WifiManager.isInHome(): Boolean = connectionInfo.bssid.lowercase() == "80:22:a7:44:fd:9f"