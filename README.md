# Mapa en vivo (Taller)

App Android (Jetpack Compose) con registro/login, perfil y un mapa donde se ven en tiempo real los usuarios que comparten su ubicación.

- Autenticación: Firebase Authentication (email + password)
- Datos del usuario (nombre, teléfono, enLinea, latitud, longitud, fotoUrl): Cloud Firestore, colección `usuarios/{uid}`
- Foto de perfil: Firebase Storage `fotos/{uid}.jpg` (cámara o galería)
- Mapa: Mapbox. Ubicación: FusedLocationProviderClient (`requestLocationUpdates`)

## Antes de correrla

1. **Firebase**: crea el proyecto, agrega la app Android con el paquete `com.example.tallerubicacion` y reemplaza `app/google-services.json` por el tuyo.
   Activa Authentication (Email/Password), Firestore y Storage.
2. **Reglas**: pega `firestore.rules` y `storage.rules` en la consola de Firebase.
3. **Mapbox**: pon tu token público (`pk.`) en `app/src/main/res/values/mapbox_access_token.xml`.
   El token secreto de descargas (`MAPBOX_DOWNLOADS_TOKEN`) va en `~/.gradle/gradle.properties`.

## Estructura

- `viewmodels/` AuthViewModel, PerfilViewModel, MapaViewModel (rastreo y listeners de Firestore)
- `ui/screens/` Login, Registro, Mapa, Perfil
- `ui/components/` mapa, marcador con foto, selector de foto
- `ui/navigation/` rutas (Navigation 3); el uid viaja como dato en `PerfilRoute`
- `util/FotoUtil.kt` corrige la rotación EXIF, reduce y sube la foto
