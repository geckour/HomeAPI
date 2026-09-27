package com.geckour.homeapi.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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

internal val DarkColorScheme = darkColorScheme(
    primary = Colors.TEAL200,
    onPrimary = Colors.BLACK,
    secondary = Colors.TEAL200,
    onSecondary = Colors.BLACK,
    background = Colors.BLACK,
    onBackground = Colors.PALE_WHITE,
    surface = Colors.TAR_BLACK,
    onSurface = Colors.PALE_WHITE,
    inversePrimary = Colors.TEAL700
)

internal val LightColorScheme = lightColorScheme(
    primary = Colors.TEAL200,
    onPrimary = Colors.WHITE,
    secondary = Colors.TEAL200,
    onSecondary = Colors.WHITE,
    background = Colors.WHITE,
    onBackground = Colors.TAR_BLACK,
    surface = Colors.PALE_WHITE,
    onSurface = Colors.TAR_BLACK,
    inversePrimary = Colors.TEAL200
)

@Composable
internal fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme, content = content)
}
