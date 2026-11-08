package com.example.tallerubicacion.ui.screens

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tallerubicacion.R
import com.example.tallerubicacion.ui.components.AvatarPequeno
import com.example.tallerubicacion.ui.components.MapaUsuarios
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.CameraOptions
import com.example.tallerubicacion.viewmodels.MapaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapaScreen(
    viewModel: MapaViewModel,
    onPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var menuAbierto by remember { mutableStateOf(false) }

    val mensajePermiso = stringResource(R.string.permiso_ubicacion_requerido)
    val permisos = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    ) { resultado ->
        if (resultado.values.any { it }) {
            viewModel.encenderUbicacion()
        } else {
            scope.launch { snackbarState.showSnackbar(mensajePermiso) }
        }
    }

    // Camara inicial: Bogota
    val mapViewportState = com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(-74.0637, 4.6286))
            zoom(14.0)
        }
    }

    fun irA(punto: Point) {
        mapViewportState.flyTo(
            CameraOptions.Builder().center(punto).zoom(16.0).build()
        )
    }

    LaunchedEffect(Unit) {
        viewModel.iniciarEscuchas()
    }

    // La primera vez que llega mi posicion se centra el mapa en ella
    var yaCentrado by remember { mutableStateOf(false) }
    LaunchedEffect(estado.miPosicion) {
        val posicion = estado.miPosicion
        if (posicion == null) {
            yaCentrado = false
        } else if (!yaCentrado) {
            yaCentrado = true
            irA(posicion)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.menu_abrir)
                        )
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_perfil)) },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                onPerfil()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                viewModel.cerrarSesion(onCerrarSesion)
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // En tabletas y plegables se muestra ademas un panel lateral con los usuarios en linea
            val pantallaAncha = maxWidth >= 720.dp

            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    MapaUsuarios(
                        estado = estado,
                        mapViewportState = mapViewportState,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier
                            .align(if (pantallaAncha) Alignment.BottomStart else Alignment.BottomCenter)
                            .padding(16.dp)
                            .widthIn(max = 420.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { estado.miPosicion?.let { irA(it) } },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = stringResource(R.string.centrar_mapa)
                            )
                        }

                        TarjetaUbicacion(
                            compartiendo = estado.compartiendo,
                            enLinea = estado.otros.size,
                            onCambio = { activar ->
                                if (!activar) {
                                    viewModel.apagarUbicacion()
                                } else if (permisos.permissions.any { it.status.isGranted }) {
                                    viewModel.encenderUbicacion()
                                } else {
                                    permisos.launchMultiplePermissionRequest()
                                }
                            }
                        )
                    }
                }

                if (pantallaAncha) {
                    PanelUsuarios(
                        estado = estado,
                        onUsuarioClick = { irA(it) },
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaUbicacion(
    compartiendo: Boolean,
    enLinea: Int,
    onCambio: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.compartir_ubicacion),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(
                        if (compartiendo) R.string.ubicacion_activa else R.string.ubicacion_apagada
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (compartiendo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = pluralStringResource(R.plurals.usuarios_en_linea, enLinea, enLinea),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = compartiendo, onCheckedChange = onCambio)
        }
    }
}

@Composable
private fun PanelUsuarios(
    estado: com.example.tallerubicacion.viewmodels.MapaUiState,
    onUsuarioClick: (Point) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, tonalElevation = 2.dp) {
        Column {
            Text(
                text = stringResource(R.string.panel_usuarios_titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider()
            if (estado.otros.isEmpty()) {
                Text(
                    text = stringResource(R.string.sin_usuarios_en_linea),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(estado.otros, key = { it.usuario.uid }) { otro ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUsuarioClick(otro.posicion) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarPequeno(foto = otro.usuario.fotoUrl, nombre = otro.usuario.nombre)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = otro.usuario.nombre,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
