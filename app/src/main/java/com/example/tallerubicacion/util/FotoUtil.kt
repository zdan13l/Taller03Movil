package com.example.tallerubicacion.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlin.concurrent.thread

object FotoUtil {

    private const val LADO_MAXIMO = 512

    /** Crea un archivo vacio en la carpeta de fotos y devuelve su Uri para la app de camara. */
    fun crearUriParaCamara(context: Context): Uri {
        val carpeta = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val archivo = File(carpeta, "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", archivo)
    }

    /**
     * Lee la foto, la rota segun el EXIF (la camara guarda las fotos "acostadas" y marca la
     * orientacion en los metadatos) y la reduce de tamano. Asi la foto del marcador queda igual
     * a la que se tomo.
     */
    private fun prepararJpeg(context: Context, uri: Uri): ByteArray? {
        val resolver = context.contentResolver

        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

        var muestreo = 1
        while (limites.outWidth / muestreo > LADO_MAXIMO * 2 || limites.outHeight / muestreo > LADO_MAXIMO * 2) {
            muestreo *= 2
        }
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        val original = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opciones)
        } ?: return null

        val orientacion = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        val matriz = Matrix()
        when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matriz.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matriz.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matriz.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matriz.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matriz.postScale(1f, -1f)
        }
        val escala = LADO_MAXIMO.toFloat() / maxOf(original.width, original.height)
        if (escala < 1f) matriz.postScale(escala, escala)

        val bitmap = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, salida)
        return salida.toByteArray()
    }

    /** Sube la foto a Storage (fotos/uid.jpg) y devuelve la URL de descarga, o null si fallo. */
    fun subirFoto(context: Context, uid: String, uri: Uri, onResult: (String?) -> Unit) {
        val appContext = context.applicationContext
        thread {
            val bytes = try {
                prepararJpeg(appContext, uri)
            } catch (e: Exception) {
                null
            }
            if (bytes == null) {
                onResult(null)
                return@thread
            }
            val referencia = FirebaseStorage.getInstance().reference.child("fotos/$uid.jpg")
            val metadatos = StorageMetadata.Builder().setContentType("image/jpeg").build()
            referencia.putBytes(bytes, metadatos)
                .addOnSuccessListener {
                    referencia.downloadUrl
                        .addOnSuccessListener { url ->
                            // El parametro v evita que Coil muestre la foto vieja guardada en cache.
                            onResult("$url&v=${System.currentTimeMillis()}")
                        }
                        .addOnFailureListener { onResult(null) }
                }
                .addOnFailureListener { onResult(null) }
        }
    }
}
