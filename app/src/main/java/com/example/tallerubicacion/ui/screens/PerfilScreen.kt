package com.example.tallerubicacion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tallerubicacion.R
import com.example.tallerubicacion.ui.components.CampoPassword
import com.example.tallerubicacion.ui.components.SelectorFoto
import com.example.tallerubicacion.viewmodels.PerfilViewModel

// el uid llega como extra de la ruta
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    uid: String,
    viewModel: PerfilViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(uid) {
        viewModel.cargar(uid)
    }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbarState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.perfil_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // dos columnas en pantalla ancha, una en celular
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        // el margen del teclado deja desplazar hasta el ultimo campo
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    val pantallaAncha = maxWidth >= 720.dp
                    if (pantallaAncha) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .widthIn(max = 1000.dp)
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                TarjetaFoto(viewModel)
                            }
                            Column(
                                modifier = Modifier.weight(1.4f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                TarjetaDatos(viewModel)
                                TarjetaPassword(viewModel)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .widthIn(max = 560.dp)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TarjetaFoto(viewModel)
                            TarjetaDatos(viewModel)
                            TarjetaPassword(viewModel)
                        }
                    }
                }
            }

            if (estado.ocupado) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
private fun TarjetaFoto(viewModel: PerfilViewModel) {
    val estado by viewModel.uiState.collectAsState()
    val usuario = estado.usuario

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectorFoto(
                foto = if (estado.subiendoFoto) estado.fotoPendiente else usuario.fotoUrl,
                nombre = usuario.nombre,
                onFotoElegida = { viewModel.cambiarFoto(it) },
                tamano = 128.dp,
                habilitado = !estado.ocupado
            )
            Text(
                stringResource(if (estado.subiendoFoto) R.string.subiendo_foto else R.string.ayuda_cambiar_foto),
                style = MaterialTheme.typography.bodySmall
            )
            estado.errorFoto?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
                OutlinedButton(
                    onClick = { estado.fotoPendiente?.let(viewModel::cambiarFoto) },
                    enabled = !estado.ocupado
                ) {
                    Text(stringResource(R.string.reintentar_foto))
                }
            }
            Text(text = usuario.nombre, style = MaterialTheme.typography.titleLarge)
            Text(text = usuario.email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TarjetaDatos(viewModel: PerfilViewModel) {
    val estado by viewModel.uiState.collectAsState()
    var nombre by rememberSaveable(estado.usuario.nombre) { mutableStateOf(estado.usuario.nombre) }
    var telefono by rememberSaveable(estado.usuario.telefono) { mutableStateOf(estado.usuario.telefono) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.datos_personales),
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.campo_nombre)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.usuario.email,
                onValueChange = {},
                label = { Text(stringResource(R.string.campo_email)) },
                supportingText = { Text(stringResource(R.string.email_no_editable)) },
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text(stringResource(R.string.campo_telefono)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.guardarDatos(nombre.trim(), telefono.trim()) },
                enabled = !estado.ocupado && nombre.isNotBlank() && telefono.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.guardar_cambios))
            }
        }
    }
}

@Composable
private fun TarjetaPassword(viewModel: PerfilViewModel) {
    val estado by viewModel.uiState.collectAsState()
    var actual by rememberSaveable { mutableStateOf("") }
    var nueva by rememberSaveable { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.cambiar_password),
                style = MaterialTheme.typography.titleMedium
            )
            CampoPassword(
                valor = actual,
                onCambio = { actual = it },
                etiqueta = stringResource(R.string.campo_password_actual)
            )
            CampoPassword(
                valor = nueva,
                onCambio = { nueva = it },
                etiqueta = stringResource(R.string.campo_password_nueva),
                ayuda = stringResource(R.string.error_password_corto)
            )
            OutlinedButton(
                onClick = {
                    viewModel.cambiarPassword(actual, nueva)
                    actual = ""
                    nueva = ""
                },
                enabled = !estado.ocupado && actual.isNotBlank() && nueva.length >= 6,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.boton_cambiar_password))
            }
        }
    }
}
