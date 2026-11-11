package com.example.tallerubicacion.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute : NavKey

@Serializable
data object RegistroRoute : NavKey

@Serializable
data object MapaRoute : NavKey

// el uid viaja como extra de la ruta
@Serializable
data class PerfilRoute(val uid: String) : NavKey
