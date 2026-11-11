package com.example.tallerubicacion.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.tallerubicacion.R

// campo de contrasena con el ojito para mostrar u ocultar el texto
@Composable
fun CampoPassword(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    conCandado: Boolean = false,
    ayuda: String? = null
) {
    var verPassword by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = if (conCandado) {
            { Icon(Icons.Filled.Lock, contentDescription = null) }
        } else null,
        trailingIcon = {
            IconButton(onClick = { verPassword = !verPassword }) {
                Icon(
                    imageVector = if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(
                        if (verPassword) R.string.ocultar_password else R.string.mostrar_password
                    )
                )
            }
        },
        supportingText = if (ayuda != null) {
            { Text(ayuda) }
        } else null,
        singleLine = true,
        visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = modifier.fillMaxWidth()
    )
}
