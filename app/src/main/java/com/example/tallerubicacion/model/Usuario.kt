package com.example.tallerubicacion.model

// datos del usuario en firestore, la contrasena queda en authentication
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val enLinea: Boolean = false,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fotoUrl: String = "",
    // cambia al encender el switch para reiniciar el recorrido
    val sesion: Long = 0L
)
