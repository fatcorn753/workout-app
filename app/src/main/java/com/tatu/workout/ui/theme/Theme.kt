package com.tatu.workout.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Forest = Color(0xFF1B2A1E)
private val Lime = Color(0xFF9CCC65)
private val LimeDeep = Color(0xFF4C7A2E)
private val Ember = Color(0xFFE8926A)

private val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = Color(0xFF0E1A0A),
    primaryContainer = LimeDeep,
    onPrimaryContainer = Color(0xFFE6F5D6),
    secondary = Ember,
    onSecondary = Color(0xFF2E1608),
    background = Forest,
    onBackground = Color(0xFFECF2E6),
    surface = Color(0xFF223324),
    onSurface = Color(0xFFECF2E6),
    surfaceVariant = Color(0xFF2C4030),
    onSurfaceVariant = Color(0xFFC0D0B8),
    error = Color(0xFFE59A9A),
)

private val LightColors = lightColorScheme(
    primary = LimeDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEEC8),
    onPrimaryContainer = Color(0xFF14260C),
    secondary = Color(0xFFB2603A),
    background = Color(0xFFF7FAF3),
    onBackground = Color(0xFF17200F),
    surface = Color.White,
    onSurface = Color(0xFF17200F),
    surfaceVariant = Color(0xFFE3EBDC),
    onSurfaceVariant = Color(0xFF4A5843),
)

@Composable
fun WorkoutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
