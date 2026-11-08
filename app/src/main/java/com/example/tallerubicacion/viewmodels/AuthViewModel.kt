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

    /**
     * Crea el usuario en Authentication (email y password) y guarda el resto de datos
     * en Firestore. Si el usuario eligio foto, tambien la sube a Storage.
     */
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
                    FotoUtil.subirFoto(context, uid, foto) { url ->
                        guardarUsuario(usuario.copy(fotoUrl = url ?: ""), onResult)
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
