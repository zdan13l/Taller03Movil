package com.example.tallerubicacion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Verde,
    onPrimary = Color.White,
    secondary = Naranja,
    background = FondoClaro,
    surface = Color.White,
    onSurface = VerdeOscuro
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeClaro,
    onPrimary = VerdeOscuro,
    secondary = Naranja,
    background = FondoOscuro,
    surface = SuperficieOscura,
    onSurface = Color.White
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
