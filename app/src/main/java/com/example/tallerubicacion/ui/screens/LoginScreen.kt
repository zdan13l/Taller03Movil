package com.example.tallerubicacion.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tallerubicacion.R
import com.example.tallerubicacion.ui.components.CampoPassword
import com.example.tallerubicacion.ui.components.FondoAuth
import com.example.tallerubicacion.ui.theme.Negro
import com.example.tallerubicacion.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var cargando by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val errorCampos = stringResource(R.string.error_campos_vacios)
    val errorEmail = stringResource(R.string.error_email_invalido)
    val errorLogin = stringResource(R.string.error_login)

    FondoAuth {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // logo sobre cuadro negro con borde rojo
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Negro)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_mapa),
                    contentDescription = stringResource(R.string.logo_descripcion),
                    modifier = Modifier.size(68.dp)
                )
            }
            Text(
                text = stringResource(R.string.login_encabezado),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(R.string.login_subtitulo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login_titulo),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
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
                            if (email.isBlank() || password.isBlank()) {
                                error = errorCampos
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                                error = errorEmail
                            } else {
                                error = null
                                cargando = true
                                viewModel.login(email.trim(), password) { exito, mensaje ->
                                    cargando = false
                                    if (exito) onLoginExitoso() else error = mensaje ?: errorLogin
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
                            Text(stringResource(R.string.boton_ingresar))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onCrearCuenta,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Text(stringResource(R.string.crear_cuenta))
            }
        }
    }
}
