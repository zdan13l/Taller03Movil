package com.example.tallerubicacion.ui.screens

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tallerubicacion.R
import com.example.tallerubicacion.ui.components.AvatarPequeno
import com.example.tallerubicacion.ui.components.MapaUsuarios
import com.example.tallerubicacion.ui.components.PerfilVista
import com.example.tallerubicacion.ui.components.VistaPreviaUsuario
import com.example.tallerubicacion.ui.theme.Grafito
import com.example.tallerubicacion.ui.theme.Gris
import com.example.tallerubicacion.ui.theme.Rojo
import com.example.tallerubicacion.ui.theme.RojoVivo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.CameraOptions
import com.example.tallerubicacion.viewmodels.MapaUiState
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
    var listaAbierta by remember { mutableStateOf(false) }
    var uidSeleccionado by remember { mutableStateOf<String?>(null) }

    // panel lateral en tablet, lista desde la barra en celular
    val pantallaAncha = LocalConfiguration.current.screenWidthDp >= 720

    // usuario tocado en el mapa, se calcula con el estado para que siga la posicion en vivo
    val vista = when (uidSeleccionado) {
        null -> null
        estado.yo?.uid -> estado.yo?.let { PerfilVista(it.nombre, it.fotoUrl, estado.miPosicion, true) }
        else -> estado.otros.find { it.usuario.uid == uidSeleccionado }
            ?.let { PerfilVista(it.usuario.nombre, it.usuario.fotoUrl, it.posicion, false) }
    }

    // el total incluye al usuario si esta compartiendo
    val totalEnLinea = estado.otros.size + if (estado.compartiendo) 1 else 0

    val mensajePermiso = stringResource(R.string.permiso_ubicacion_requerido)
    val mensajeActivar = stringResource(R.string.activar_ubicacion_primero)
    val mensajeEsperando = stringResource(R.string.esperando_ubicacion)
    // permiso de ubicacion, al aceptarlo enciende el rastreo
    val permisos = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    ) { resultado ->
        if (resultado.values.any { it }) {
            viewModel.encenderUbicacion()
        } else {
            scope.launch { snackbarState.showSnackbar(mensajePermiso) }
        }
    }

    // camara inicial en bogota
    val mapViewportState = rememberMapViewportState {
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

    // centra el mapa cuando llega la primera posicion
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
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondary
                ),
                actions = {
                    if (!pantallaAncha) {
                        IconButton(onClick = { listaAbierta = true }) {
                            BadgedBox(
                                badge = {
                                    if (totalEnLinea > 0) {
                                        Badge { Text(totalEnLinea.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Groups,
                                    contentDescription = stringResource(R.string.ver_usuarios)
                                )
                            }
                        }
                    }
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
                                // cierra la sesion y vuelve al login
                                viewModel.cerrarSesion(onCerrarSesion)
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    MapaUsuarios(
                        estado = estado,
                        mapViewportState = mapViewportState,
                        onMarcadorClick = { uidSeleccionado = it },
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
                            onClick = {
                                val miPosicion = estado.miPosicion
                                if (miPosicion != null) {
                                    irA(miPosicion)
                                } else {
                                    // sin posicion: ubicacion apagada o esperando el primer punto
                                    scope.launch {
                                        snackbarState.currentSnackbarData?.dismiss()
                                        snackbarState.showSnackbar(
                                            if (estado.compartiendo) mensajeEsperando else mensajeActivar
                                        )
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = stringResource(R.string.centrar_mapa)
                            )
                        }

                        TarjetaUbicacion(
                            compartiendo = estado.compartiendo,
                            enLinea = totalEnLinea,
                            // el switch enciende o apaga el rastreo
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

    if (vista != null) {
        ModalBottomSheet(onDismissRequest = { uidSeleccionado = null }) {
            VistaPreviaUsuario(vista = vista)
        }
    }

    if (listaAbierta && !pantallaAncha) {
        ModalBottomSheet(onDismissRequest = { listaAbierta = false }) {
            ListaUsuarios(
                estado = estado,
                onUsuarioClick = {
                    listaAbierta = false
                    irA(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
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
                    color = if (compartiendo) RojoVivo
                    else MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.65f)
                )
                Text(
                    text = pluralStringResource(R.plurals.usuarios_en_linea, enLinea, enLinea),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = compartiendo,
                onCheckedChange = onCambio,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Rojo,
                    uncheckedThumbColor = Color(0xFFBDBDBD),
                    uncheckedTrackColor = Grafito,
                    uncheckedBorderColor = Gris
                )
            )
        }
    }
}

@Composable
private fun PanelUsuarios(
    estado: MapaUiState,
    onUsuarioClick: (Point) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, tonalElevation = 2.dp) {
        ListaUsuarios(
            estado = estado,
            onUsuarioClick = onUsuarioClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// lista de usuarios en linea, al tocar uno el mapa se mueve hasta el
@Composable
private fun ListaUsuarios(
    estado: MapaUiState,
    onUsuarioClick: (Point) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (estado.compartiendo) {
                item(key = "yo") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { estado.miPosicion?.let { onUsuarioClick(it) } }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarPequeno(foto = estado.yo?.fotoUrl, nombre = estado.yo?.nombre ?: "")
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.nombre_tu_usuario, estado.yo?.nombre ?: ""),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
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
