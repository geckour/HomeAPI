package com.geckour.homeapi.ui

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

internal object Colors {

    val TEAL200 = Color(0xFF03DAC5)
    val TEAL700 = Color(0xFF018786)
    val TEAL900 = Color(0xFF1F2727)
    val BLACK = Color(0xFF000000)
    val TAR_BLACK = Color(0xE0000000)
    val WHITE = Color(0xFFFFFFFF)
    val PALE_WHITE = Color(0xE0FFFFFF)
}

internal val DarkColors = darkColors(
    primary = Colors.BLACK,
    primaryVariant = Colors.BLACK,
    secondary = Colors.TEAL200,
    secondaryVariant = Colors.TEAL700,
    background = Colors.BLACK,
    surface = Colors.BLACK,
    onPrimary = Colors.WHITE,
    onSecondary = Colors.WHITE,
    onBackground = Colors.PALE_WHITE,
    onSurface = Colors.PALE_WHITE
)

internal val LightColors = lightColors(
    primary = Colors.WHITE,
    primaryVariant = Colors.WHITE,
    secondary = Colors.TEAL200,
    secondaryVariant = Colors.TEAL700,
    background = Colors.WHITE,
    surface = Colors.WHITE,
    onPrimary = Colors.BLACK,
    onSecondary = Colors.BLACK,
    onBackground = Colors.TAR_BLACK,
    onSurface = Colors.TAR_BLACK
)