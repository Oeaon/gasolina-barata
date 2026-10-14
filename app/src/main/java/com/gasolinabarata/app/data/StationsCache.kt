package com.gasolinabarata.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.Instant

class CachedBody(val body: String, val savedAt: Instant)

/** Última lista descargada, para abrir rápido y funcionar sin conexión. */
class StationsCache(private val file: File) {
    suspend fun read(): CachedBody? = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) CachedBody(file.readText(), Instant.ofEpochMilli(file.lastModified())) else null
        } catch (e: IOException) {
            null
        }
    }

    suspend fun write(body: String) = withContext(Dispatchers.IO) {
        try {
            val tmp = File(file.parentFile, "${file.name}.tmp")
            tmp.writeText(body)
            if (!tmp.renameTo(file)) {
                file.delete()
                tmp.renameTo(file)
            }
        } catch (e: IOException) {
            // Sin caché la app sigue funcionando; solo vuelve a descargar.
        }
    }
}
