package com.example.tallerubicacion.ui.components

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tallerubicacion.R
import com.example.tallerubicacion.util.FotoUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

// foto de perfil que se cambia con camara o galeria
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SelectorFoto(
    foto: Any?,
    nombre: String,
    onFotoElegida: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    tamano: Dp = 120.dp,
    habilitado: Boolean = true
) {
    val context = LocalContext.current
    var mostrarOpciones by rememberSaveable { mutableStateOf(false) }
    var uriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { exito ->
            if (exito) uriCamara?.let { onFotoElegida(it) }
        }
    )

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) onFotoElegida(uri)
        }
    )

    val abrirCamara = {
        try {
            val uri = FotoUtil.crearUriParaCamara(context)
            uriCamara = uri
            camaraLauncher.launch(uri)
        } catch (_: Exception) {
            Toast.makeText(context, R.string.error_abrir_camara, Toast.LENGTH_LONG).show()
        }
    }

    val permisoCamara = rememberPermissionState(Manifest.permission.CAMERA) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(context, R.string.permiso_camara_requerido, Toast.LENGTH_LONG).show()
    }

    Box(modifier = modifier.clickable(enabled = habilitado) { mostrarOpciones = true }) {
        AvatarUsuario(foto = foto, nombre = nombre, tamano = tamano)
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = stringResource(R.string.cambiar_foto),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

    if (mostrarOpciones) {
        AlertDialog(
            onDismissRequest = { mostrarOpciones = false },
            title = { Text(stringResource(R.string.foto_perfil)) },
            text = { Text(stringResource(R.string.foto_elegir_origen)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarOpciones = false
                    if (permisoCamara.status.isGranted) {
                        abrirCamara()
                    } else {
                        permisoCamara.launchPermissionRequest()
                    }
                }) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Text(
                        text = stringResource(R.string.tomar_foto),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarOpciones = false
                    galeriaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Text(
                        text = stringResource(R.string.elegir_galeria),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        )
    }
}
