package com.gasolinabarata.app.data

import com.gasolinabarata.core.StationFormatException
import com.gasolinabarata.core.StationList
import com.gasolinabarata.core.StationParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Duration
import java.time.Instant

sealed interface StationsResult {
    /** [offline] = no se pudo actualizar y se muestran los datos guardados. */
    data class Available(val list: StationList, val offline: Boolean) : StationsResult
    data object NetworkError : StationsResult
    data object ServerError : StationsResult
}

class StationsRepository(
    private val api: StationsApi,
    private val cache: StationsCache,
    private val clock: () -> Instant = Instant::now,
) {
    private val mutex = Mutex()
    private var memory: Pair<StationList, Instant>? = null

    suspend fun stations(refresh: Boolean = false): StationsResult = mutex.withLock {
        val loaded = memory ?: cache.read()?.let { cached -> parse(cached.body)?.let { it to cached.savedAt } }
        memory = loaded
        if (loaded != null && !refresh && isFresh(loaded.second)) {
            return@withLock StationsResult.Available(loaded.first, offline = false)
        }

        val body = try {
            api.fetchAll()
        } catch (e: HttpException) {
            return@withLock loaded?.let { StationsResult.Available(it.first, offline = true) } ?: StationsResult.ServerError
        } catch (e: IOException) {
            return@withLock loaded?.let { StationsResult.Available(it.first, offline = true) } ?: StationsResult.NetworkError
        }
        val list = parse(body)
        if (list == null || list.stations.isEmpty()) {
            return@withLock loaded?.let { StationsResult.Available(it.first, offline = true) } ?: StationsResult.ServerError
        }
        cache.write(body)
        memory = list to clock()
        StationsResult.Available(list, offline = false)
    }

    private fun isFresh(savedAt: Instant) = Duration.between(savedAt, clock()) < MAX_AGE

    private suspend fun parse(body: String): StationList? = withContext(Dispatchers.Default) {
        try {
            StationParser.parse(body)
        } catch (e: StationFormatException) {
            null
        }
    }

    private companion object {
        /** El Ministerio actualiza los precios cada media hora. */
        val MAX_AGE: Duration = Duration.ofMinutes(30)
    }
}
