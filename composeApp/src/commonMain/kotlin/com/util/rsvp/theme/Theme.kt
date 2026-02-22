package com.util.rsvp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun LocalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    primary: Color,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkAppColorScheme(primary = primary)
    } else {
        lightAppColorScheme(primary = primary)
    }

    MaterialTheme(
        colorScheme = colors,
        typography = appTypography(),
        shapes = AppShapes,
        content = content
    )
}