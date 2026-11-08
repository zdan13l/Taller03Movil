package com.example.tallerubicacion.model

/**
 * Datos del usuario guardados en Firestore (coleccion "usuarios", documento = uid).
 * El password NO se guarda aqui, eso lo maneja Firebase Authentication.
 */
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val enLinea: Boolean = false,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotoUrl: String = ""
)
