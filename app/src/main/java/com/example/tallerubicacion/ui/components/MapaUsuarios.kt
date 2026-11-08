package com.example.tallerubicacion.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.tallerubicacion.ui.theme.ColorMio
import com.example.tallerubicacion.ui.theme.ColoresUsuarios
import com.example.tallerubicacion.viewmodels.MapaUiState
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.style.standard.LightPresetValue
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.compose.style.standard.rememberStandardStyleState
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions

fun colorDeUsuario(uid: String): Color {
    val indice = Math.abs(uid.hashCode()) % ColoresUsuarios.size
    return ColoresUsuarios[indice]
}

@OptIn(MapboxExperimental::class)
@Composable
fun MapaUsuarios(
    estado: MapaUiState,
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier
) {
    val oscuro = isSystemInDarkTheme()
    val estiloState = rememberStandardStyleState {
        configurationsState.lightPreset = if (oscuro) LightPresetValue.NIGHT else LightPresetValue.DAY
    }

    MapboxMap(
        modifier = modifier,
        mapViewportState = mapViewportState,
        scaleBar = {},
        style = { MapboxStandardStyle(standardStyleState = estiloState) }
    ) {
        // Lineas de los recorridos
        if (estado.miRuta.size >= 2) {
            PolylineAnnotation(points = estado.miRuta) {
                lineColor = ColorMio
                lineWidth = 6.0
                lineColorUseTheme = "none"
                lineEmissiveStrength = 1.0
                lineBorderColor = Color.White
                lineBorderWidth = 1.5
            }
        }
        estado.otros.forEach { otro ->
            if (otro.ruta.size >= 2) {
                key(otro.usuario.uid) {
                    PolylineAnnotation(points = otro.ruta) {
                        lineColor = colorDeUsuario(otro.usuario.uid)
                        lineWidth = 5.0
                        lineColorUseTheme = "none"
                        lineEmissiveStrength = 1.0
                        lineBorderColor = Color.White
                        lineBorderWidth = 1.5
                    }
                }
            }
        }

        // Marcador de los otros usuarios en linea
        estado.otros.forEach { otro ->
            key(otro.usuario.uid) {
                ViewAnnotation(
                    options = viewAnnotationOptions {
                        geometry(otro.posicion)
                        allowOverlap(true)
                        annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                    }
                ) {
                    MarcadorUsuario(
                        foto = otro.usuario.fotoUrl,
                        nombre = otro.usuario.nombre,
                        color = colorDeUsuario(otro.usuario.uid)
                    )
                }
            }
        }

        // Mi marcador
        val miPosicion = estado.miPosicion
        if (miPosicion != null) {
            ViewAnnotation(
                options = viewAnnotationOptions {
                    geometry(miPosicion)
                    allowOverlap(true)
                    annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                }
            ) {
                MarcadorUsuario(
                    foto = estado.yo?.fotoUrl,
                    nombre = estado.yo?.nombre ?: "",
                    color = ColorMio
                )
            }
        }
    }
}
