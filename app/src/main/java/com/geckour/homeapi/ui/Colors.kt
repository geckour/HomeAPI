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
    val TEMPERATURE = Color.Yellow
    val HUMIDITY = Color.Cyan
    val PRESSURE = Color.Green
    val SOIL_HUMIDITY = Color(0xffff8000)
    val CO2 = Color.White
}

internal val DarkColors = darkColors(
    primary = Colors.TEAL200,
    primaryVariant = Colors.TEAL700,
    secondary = Colors.TEAL200,
    secondaryVariant = Colors.TEAL700,
    background = Colors.BLACK,
    surface = Colors.TAR_BLACK,
    onPrimary = Colors.BLACK,
    onSecondary = Colors.BLACK,
    onBackground = Colors.PALE_WHITE,
    onSurface = Colors.PALE_WHITE
)

internal val LightColors = lightColors(
    primary = Colors.TEAL200,
    primaryVariant = Colors.TEAL700,
    secondary = Colors.TEAL200,
    secondaryVariant = Colors.TEAL700,
    background = Colors.WHITE,
    surface = Colors.PALE_WHITE,
    onPrimary = Colors.WHITE,
    onSecondary = Colors.WHITE,
    onBackground = Colors.TAR_BLACK,
    onSurface = Colors.TAR_BLACK
)