package com.example.tallerubicacion.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.tallerubicacion.R
import com.example.tallerubicacion.model.Usuario
import com.example.tallerubicacion.util.FotoUtil
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerfilUiState(
    val usuario: Usuario = Usuario(),
    val cargando: Boolean = true,
    val ocupado: Boolean = false,
    val mensaje: String? = null
)

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private fun texto(id: Int): String = getApplication<Application>().getString(id)

    private fun mostrar(mensaje: String) {
        _uiState.update { it.copy(ocupado = false, mensaje = mensaje) }
    }

    fun mensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    /** Lee los datos actuales del usuario desde Firestore. */
    fun cargar(uid: String) {
        val email = auth.currentUser?.email
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val datos = doc.toObject(Usuario::class.java) ?: Usuario()
                _uiState.update {
                    it.copy(
                        usuario = datos.copy(uid = uid, email = email ?: datos.email),
                        cargando = false
                    )
                }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(cargando = false) }
                mostrar(texto(R.string.error_cargar_perfil))
            }
    }

    /** Nombre y telefono viven en Firestore. */
    fun guardarDatos(nombre: String, telefono: String) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(ocupado = true) }
        db.collection("usuarios").document(uid)
            .update(mapOf("nombre" to nombre, "telefono" to telefono))
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(usuario = it.usuario.copy(nombre = nombre, telefono = telefono))
                }
                mostrar(texto(R.string.perfil_actualizado))
            }
            .addOnFailureListener { mostrar(texto(R.string.error_guardar)) }
    }

    /** La contrasena la maneja Authentication, hay que reautenticar antes de cambiarla. */
    fun cambiarPassword(actual: String, nueva: String) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return
        _uiState.update { it.copy(ocupado = true) }
        val credencial = EmailAuthProvider.getCredential(email, actual)
        user.reauthenticate(credencial)
            .addOnSuccessListener {
                user.updatePassword(nueva)
                    .addOnSuccessListener { mostrar(texto(R.string.password_actualizado)) }
                    .addOnFailureListener { e ->
                        mostrar(e.localizedMessage ?: texto(R.string.error_guardar))
                    }
            }
            .addOnFailureListener { mostrar(texto(R.string.error_password_actual)) }
    }

    fun cambiarFoto(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(ocupado = true) }
        FotoUtil.subirFoto(getApplication(), uid, uri) { url ->
            if (url == null) {
                mostrar(texto(R.string.error_subir_foto))
            } else {
                db.collection("usuarios").document(uid).update("fotoUrl", url)
                    .addOnSuccessListener {
                        _uiState.update { it.copy(usuario = it.usuario.copy(fotoUrl = url)) }
                        mostrar(texto(R.string.foto_actualizada))
                    }
                    .addOnFailureListener { mostrar(texto(R.string.error_guardar)) }
            }
        }
    }
}
