package com.example.tallerubicacion.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.tallerubicacion.model.Usuario
import com.example.tallerubicacion.util.FotoUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun estaAutenticado(): Boolean = auth.currentUser != null

    fun uidActual(): String = auth.currentUser?.uid ?: ""

    // inicia sesion con email y contrasena
    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage)
                }
            }
    }

    // crea el usuario en authentication y guarda sus datos en firestore
    fun registrar(
        context: Context,
        nombre: String,
        email: String,
        telefono: String,
        pass: String,
        foto: Uri?,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(false, task.exception?.localizedMessage)
                    return@addOnCompleteListener
                }
                val uid = auth.currentUser?.uid ?: ""
                val usuario = Usuario(
                    uid = uid,
                    nombre = nombre,
                    email = email,
                    telefono = telefono,
                    enLinea = false
                )
                if (foto == null) {
                    guardarUsuario(usuario, onResult)
                } else {
                    FotoUtil.subirFoto(context, uid, foto) { resultado ->
                        guardarUsuario(usuario.copy(fotoUrl = resultado.getOrDefault(""))) { exito, mensaje ->
                            val aviso = if (exito && resultado.isFailure) context.getString(com.example.tallerubicacion.R.string.registro_sin_foto) else mensaje
                            onResult(exito, aviso)
                        }
                    }
                }
            }
    }

    private fun guardarUsuario(usuario: Usuario, onResult: (Boolean, String?) -> Unit) {
        db.collection("usuarios").document(usuario.uid)
            .set(usuario)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
