package com.example.tallerubicacion.ui.screens

import android.net.Uri
import android.widget.Toast
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tallerubicacion.R
import com.example.tallerubicacion.ui.components.CampoPassword
import com.example.tallerubicacion.ui.components.FondoAuth
import com.example.tallerubicacion.ui.components.SelectorFoto
import com.example.tallerubicacion.viewmodels.AuthViewModel

@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onRegistroExitoso: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var cargando by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val errorCampos = stringResource(R.string.error_campos_vacios)
    val errorEmail = stringResource(R.string.error_email_invalido)
    val errorPasswordCorto = stringResource(R.string.error_password_corto)
    val errorRegistro = stringResource(R.string.error_registro)

    FondoAuth {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.registro_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // foto de perfil opcional
                    SelectorFoto(
                        foto = foto,
                        nombre = nombre,
                        onFotoElegida = { foto = it },
                        tamano = 104.dp,
                        habilitado = !cargando
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text(stringResource(R.string.campo_nombre)) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.campo_email)) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text(stringResource(R.string.campo_telefono)) },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    CampoPassword(
                        valor = password,
                        onCambio = { password = it },
                        etiqueta = stringResource(R.string.campo_password),
                        conCandado = true
                    )

                    if (error != null) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            // valida los campos y muestra el error
                            if (nombre.isBlank() || email.isBlank() || telefono.isBlank() || password.isBlank()) {
                                error = errorCampos
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                error = errorEmail
                            } else if (password.length < 6) {
                                error = errorPasswordCorto
                            } else {
                                error = null
                                cargando = true
                                viewModel.registrar(
                                    context = context,
                                    nombre = nombre.trim(),
                                    email = email.trim(),
                                    telefono = telefono.trim(),
                                    pass = password,
                                    foto = foto
                                ) { exito, mensaje ->
                                    cargando = false
                                    if (exito) {
                                        if (mensaje != null) Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                                        onRegistroExitoso()
                                    } else error = mensaje ?: errorRegistro
                                }
                            }
                        },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.boton_registrar))
                        }
                    }
                }
            }
        }

        // volver va al final para quedar sobre la columna y recibir el toque
        IconButton(
            onClick = onVolver,
            modifier = Modifier
                .align(Alignment.TopStart)
                .systemBarsPadding()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.volver)
            )
        }
    }
}
