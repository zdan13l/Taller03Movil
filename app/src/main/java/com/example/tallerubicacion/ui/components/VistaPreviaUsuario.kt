package com.example.tallerubicacion.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tallerubicacion.R
import com.example.tallerubicacion.util.DireccionUtil
import com.mapbox.geojson.Point
import kotlinx.coroutines.delay

// datos que muestra la vista previa de un usuario
data class PerfilVista(
    val nombre: String,
    val fotoUrl: String,
    val posicion: Point?,
    val esYo: Boolean
)

// foto grande, nombre y ubicacion actual del usuario tocado en el mapa
@Composable
fun VistaPreviaUsuario(vista: PerfilVista, modifier: Modifier = Modifier) {
    val token = stringResource(R.string.mapbox_access_token)
    var direccion by remember { mutableStateOf<String?>(null) }
    var buscando by remember { mutableStateOf(true) }
    val punto = vista.posicion

    // vuelve a buscar la direccion cuando se mueve, con una pausa para no consultar en cada punto
    LaunchedEffect(punto?.latitude(), punto?.longitude()) {
        if (punto == null) {
            buscando = false
            return@LaunchedEffect
        }
        buscando = true
        delay(600)
        DireccionUtil.buscar(token, punto.latitude(), punto.longitude()) {
            direccion = it ?: direccion
            buscando = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AvatarUsuario(foto = vista.fotoUrl, nombre = vista.nombre, tamano = 160.dp)

        Text(
            text = if (vista.esYo) stringResource(R.string.nombre_tu_usuario, vista.nombre) else vista.nombre,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.en_linea),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.ubicacion_actual),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when {
                        punto == null -> stringResource(R.string.sin_ubicacion_aun)
                        direccion != null -> direccion ?: ""
                        buscando -> stringResource(R.string.buscando_direccion)
                        else -> stringResource(R.string.coordenadas, punto.latitude(), punto.longitude())
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
