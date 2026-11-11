package com.example.tallerubicacion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Rojo,
    onPrimary = Color.White,
    primaryContainer = RojoSuave,
    onPrimaryContainer = RojoOscuro,
    secondary = Negro,
    onSecondary = Color.White,
    background = Marfil,
    onBackground = Negro,
    surface = Color.White,
    onSurface = Negro,
    surfaceVariant = Color(0xFFEFE9E6),
    onSurfaceVariant = Gris,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBF8F6),
    surfaceContainer = Color(0xFFF5F0EC),
    surfaceContainerHigh = Color(0xFFF0EAE6),
    surfaceContainerHighest = Color.White,
    outline = Color(0xFFB8B0AC),
    error = Color(0xFFB00020)
)

private val EsquemaOscuro = darkColorScheme(
    primary = RojoVivo,
    onPrimary = Color.White,
    primaryContainer = RojoOscuro,
    onPrimaryContainer = RojoSuave,
    secondary = Negro,
    onSecondary = Color.White,
    background = Color(0xFF0D0D0D),
    onBackground = Color(0xFFF5F5F5),
    surface = Grafito,
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF2A2A2D),
    onSurfaceVariant = Color(0xFFB5B5B8),
    surfaceContainerLowest = Color(0xFF0D0D0D),
    surfaceContainerLow = Color(0xFF151515),
    surfaceContainer = Grafito,
    surfaceContainerHigh = Color(0xFF242426),
    surfaceContainerHighest = Color(0xFF2A2A2D),
    outline = Color(0xFF5A5A5E),
    error = Color(0xFFFF6B6B)
)

@Composable
fun TallerUbicacionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        content = content
    )
}
