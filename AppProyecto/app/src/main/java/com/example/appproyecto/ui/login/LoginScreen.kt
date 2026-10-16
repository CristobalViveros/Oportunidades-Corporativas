package com.example.appproyecto.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var verPassword by remember { mutableStateOf(false) }

    // Cuando el ViewModel avisa que el login fue exitoso, navegamos.
    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) onLoginExitoso()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Text("Oportunidades corporativas", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Inicia sesión con tu cuenta de prueba",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = estado.correo,
                onValueChange = viewModel::cambiarCorreo,
                label = { Text("Correo") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                trailingIcon = {
                    if (estado.errorCorreo != null) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = estado.errorCorreo != null,
                supportingText = { estado.errorCorreo?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.password,
                onValueChange = viewModel::cambiarPassword,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { verPassword = !verPassword }) {
                        Icon(
                            imageVector = if (verPassword) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = if (verPassword) "Ocultar contraseña"
                            else "Mostrar contraseña"
                        )
                    }
                },
                isError = estado.errorPassword != null,
                supportingText = { estado.errorPassword?.let { Text(it) } },
                singleLine = true,
                visualTransformation = if (verPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Animación: el error general aparece y desaparece suavemente.
            AnimatedVisibility(
                visible = estado.errorGeneral != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = estado.errorGeneral.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = viewModel::iniciarSesion,
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ingresar")
                }
            }
        }
    }
}