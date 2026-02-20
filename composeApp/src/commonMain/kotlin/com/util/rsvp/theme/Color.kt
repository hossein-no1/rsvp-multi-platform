package com.util.rsvp.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF080808)
val OnPrimary = Color.White
val Background = Color(0xFF080808)
val OnBackground = Color(0xFF1C1B1F)

val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = Background,
    onBackground = OnBackground,
)

val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = Background
)