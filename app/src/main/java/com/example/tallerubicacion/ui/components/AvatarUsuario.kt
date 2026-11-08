package com.example.tallerubicacion.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/** Foto redonda del usuario. Si no tiene foto muestra la inicial de su nombre. */
@Composable
fun AvatarUsuario(
    foto: Any?,
    nombre: String,
    tamano: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        val hayFoto = foto != null && foto != "" && foto != Uri.EMPTY
        if (hayFoto) {
            AsyncImage(
                model = foto,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(tamano)
            )
        } else {
            Text(
                text = nombre.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.primary,
                fontSize = (tamano.value / 2.2f).sp
            )
        }
    }
}

@Composable
fun AvatarPequeno(foto: Any?, nombre: String, modifier: Modifier = Modifier) {
    AvatarUsuario(foto = foto, nombre = nombre, tamano = 40.dp, modifier = modifier)
}
