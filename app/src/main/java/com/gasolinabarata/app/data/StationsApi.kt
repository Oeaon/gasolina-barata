package com.gasolinabarata.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class HttpException(val code: Int) : IOException("HTTP $code")

/** Precios oficiales del Ministerio para la Transición Ecológica (sin clave, se actualizan cada 30 min). */
class StationsApi(private val client: OkHttpClient) {
    /** Lanza IOException si no hay conexión y [HttpException] si el servidor falla. */
    suspend fun fetchAll(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(URL)
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw HttpException(response.code)
            response.body.string()
        }
    }

    private companion object {
        const val URL =
            "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/"
    }
}
