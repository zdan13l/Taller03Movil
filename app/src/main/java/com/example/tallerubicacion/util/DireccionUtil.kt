package com.example.tallerubicacion.util

import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

object DireccionUtil {

    // busca la direccion de unas coordenadas con la geocodificacion inversa de mapbox
    fun buscar(token: String, latitud: Double, longitud: Double, onResult: (String?) -> Unit) {
        thread {
            val direccion = try {
                val url = URL(
                    "https://api.mapbox.com/search/geocode/v6/reverse" +
                        "?longitude=$longitud&latitude=$latitud&language=es&access_token=$token"
                )
                JSONObject(url.readText())
                    .getJSONArray("features")
                    .optJSONObject(0)
                    ?.getJSONObject("properties")
                    ?.optString("full_address")
                    ?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
            onResult(direccion)
        }
    }
}
