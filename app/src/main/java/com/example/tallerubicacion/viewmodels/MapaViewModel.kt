package com.example.tallerubicacion.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import com.example.tallerubicacion.model.Usuario
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UsuarioEnMapa(
    val usuario: Usuario,
    val ruta: List<Point>
) {
    val posicion: Point
        get() = Point.fromLngLat(usuario.longitud, usuario.latitud)
}

data class MapaUiState(
    val compartiendo: Boolean = false,
    val yo: Usuario? = null,
    val miRuta: List<Point> = emptyList(),
    val otros: List<UsuarioEnMapa> = emptyList()
) {
    val miPosicion: Point?
        get() = miRuta.lastOrNull()
}

class MapaViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val clienteUbicacion = LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    private var escuchaYo: ListenerRegistration? = null
    private var escuchaOtros: ListenerRegistration? = null

    // usuarios en linea y su recorrido
    private val usuariosEnLinea = LinkedHashMap<String, Usuario>()
    private val rutasOtros = HashMap<String, MutableList<Point>>()

    // recibe cada nueva posicion del gps
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(resultado: LocationResult) {
            resultado.locations.forEach { location ->
                nuevaPosicion(Point.fromLngLat(location.longitude, location.latitude))
            }
        }
    }

    // escucha mis datos y a los usuarios en linea
    fun iniciarEscuchas() {
        val miUid = auth.currentUser?.uid ?: return

        if (escuchaYo == null) {
            escuchaYo = db.collection("usuarios").document(miUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val datos = snapshot.toObject(Usuario::class.java) ?: return@addSnapshotListener
                    _uiState.update { it.copy(yo = datos.copy(uid = miUid)) }
                }
        }

        if (escuchaOtros == null) {
            escuchaOtros = db.collection("usuarios")
                .whereEqualTo("enLinea", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    for (cambio in snapshot.documentChanges) {
                        val uid = cambio.document.id
                        if (uid == miUid) continue
                        val usuario = cambio.document.toObject(Usuario::class.java).copy(uid = uid)
                        // recien conectado trae 0,0 hasta su primera posicion
                        val sinPosicion = usuario.latitud == 0.0 && usuario.longitud == 0.0
                        if (cambio.type == DocumentChange.Type.REMOVED || sinPosicion) {
                            // desconectado: se borra marcador y linea
                            usuariosEnLinea.remove(uid)
                            rutasOtros.remove(uid)
                        } else {
                            // sesion nueva: empieza un recorrido nuevo
                            if (usuariosEnLinea[uid]?.sesion != usuario.sesion) rutasOtros.remove(uid)
                            val punto = Point.fromLngLat(usuario.longitud, usuario.latitud)
                            val ruta = rutasOtros.getOrPut(uid) { mutableListOf() }
                            if (ruta.lastOrNull() != punto) ruta.add(punto)
                            usuariosEnLinea[uid] = usuario
                        }
                    }
                    publicarOtros()
                }
        }
    }

    private fun publicarOtros() {
        val lista = usuariosEnLinea.values.map { usuario ->
            UsuarioEnMapa(usuario, rutasOtros[usuario.uid]?.toList() ?: emptyList())
        }
        _uiState.update { it.copy(otros = lista) }
    }

    @SuppressLint("MissingPermission")
    fun encenderUbicacion() {
        val uid = auth.currentUser?.uid ?: return
        if (_uiState.value.compartiendo) return

        // actualizaciones de ubicacion cada 2 segundos o 3 metros
        val peticion = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setMinUpdateDistanceMeters(3f)
            .build()
        clienteUbicacion.requestLocationUpdates(peticion, locationCallback, Looper.getMainLooper())

        _uiState.update { it.copy(compartiendo = true, miRuta = emptyList()) }
        // reinicia posicion y sesion para que el recorrido empiece limpio
        db.collection("usuarios").document(uid)
            .set(
                mapOf(
                    "enLinea" to true,
                    "latitud" to 0.0,
                    "longitud" to 0.0,
                    "sesion" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
    }

    fun apagarUbicacion(onListo: () -> Unit = {}) {
        clienteUbicacion.removeLocationUpdates(locationCallback)
        _uiState.update { it.copy(compartiendo = false, miRuta = emptyList()) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            onListo()
            return
        }
        db.collection("usuarios").document(uid)
            .set(mapOf("enLinea" to false), SetOptions.merge())
            .addOnCompleteListener { onListo() }
    }

    private fun nuevaPosicion(punto: Point) {
        val uid = auth.currentUser?.uid ?: return
        if (!_uiState.value.compartiendo) return

        // agrega el punto al recorrido y guarda la posicion
        _uiState.update { it.copy(miRuta = it.miRuta + punto) }
        db.collection("usuarios").document(uid).set(
            mapOf(
                "latitud" to punto.latitude(),
                "longitud" to punto.longitude(),
                "enLinea" to true
            ),
            SetOptions.merge()
        )
    }

    // deja al usuario desconectado y cierra la sesion
    fun cerrarSesion(onListo: () -> Unit) {
        apagarUbicacion {
            detenerEscuchas()
            auth.signOut()
            onListo()
        }
    }

    private fun detenerEscuchas() {
        escuchaYo?.remove()
        escuchaOtros?.remove()
        escuchaYo = null
        escuchaOtros = null
        usuariosEnLinea.clear()
        rutasOtros.clear()
        _uiState.value = MapaUiState()
    }

    override fun onCleared() {
        if (_uiState.value.compartiendo) {
            clienteUbicacion.removeLocationUpdates(locationCallback)
            val uid = auth.currentUser?.uid
            if (uid != null) {
                db.collection("usuarios").document(uid)
                    .set(mapOf("enLinea" to false), SetOptions.merge())
            }
        }
        escuchaYo?.remove()
        escuchaOtros?.remove()
    }
}
