package com.example.tallerubicacion.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp

// foto en circulo con una punta que senala la posicion
@Composable
fun MarcadorUsuario(
    foto: Any?,
    nombre: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            modifier = Modifier.border(3.dp, color, CircleShape)
        ) {
            AvatarUsuario(
                foto = foto,
                nombre = nombre,
                tamano = 48.dp,
                modifier = Modifier.padding(2.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 10.dp)
                .drawBehind {
                    val punta = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                    drawPath(punta, color)
                }
        )
    }
}
