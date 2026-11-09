package com.example.tallerubicacion.util

import com.example.tallerubicacion.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.storage.StorageException

fun mensajeErrorFoto(error: Throwable?): Int = when (error) {
    is IllegalArgumentException, is java.io.IOException, is SecurityException -> R.string.error_leer_foto
    is FirebaseNetworkException -> R.string.error_foto_conexion
    is StorageException -> when (error.errorCode) {
        StorageException.ERROR_NOT_AUTHORIZED -> R.string.error_foto_permisos
        StorageException.ERROR_NOT_AUTHENTICATED -> R.string.error_foto_sesion
        StorageException.ERROR_OBJECT_NOT_FOUND, StorageException.ERROR_BUCKET_NOT_FOUND,
        StorageException.ERROR_PROJECT_NOT_FOUND -> R.string.error_foto_storage
        StorageException.ERROR_QUOTA_EXCEEDED -> R.string.error_foto_cuota
        StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> R.string.error_foto_conexion
        else -> R.string.error_subir_foto
    }
    else -> R.string.error_subir_foto
}
