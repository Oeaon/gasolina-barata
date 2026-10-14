package com.gasolinabarata.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class Coordinates(val latitude: Double, val longitude: Double)

/** Ubicación con el LocationManager del sistema, sin depender de Google Play Services. */
class LocationProvider(private val context: Context) {
    private val manager = context.getSystemService(LocationManager::class.java)

    fun hasPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    fun isEnabled(): Boolean = manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    /** Una posición reciente si la hay; si no, pide una nueva. Null si no llega a tiempo. */
    @SuppressLint("MissingPermission") // comprobado en hasPermission
    suspend fun current(): Coordinates? {
        if (!hasPermission()) return null
        val providers = enabledProviders()
        val recent = providers
            .mapNotNull { manager.getLastKnownLocation(it) }
            .filter { ageMillis(it) < RECENT_MS }
            .minByOrNull { it.accuracy }
        if (recent != null) return recent.toCoordinates()

        val provider = providers.firstOrNull() ?: return null
        return withTimeoutOrNull(TIMEOUT_MS) { requestSingle(provider) }?.toCoordinates()
    }

    private fun enabledProviders(): List<String> {
        val preferred = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.GPS_PROVIDER)
        }
        return preferred.filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingle(provider: String): Location? = suspendCancellableCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            manager.getCurrentLocation(provider, signal, ContextCompat.getMainExecutor(context)) { location ->
                if (continuation.isActive) continuation.resume(location)
            }
        } else {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    manager.removeUpdates(this)
                    if (continuation.isActive) continuation.resume(location)
                }

                // Antes de Android 11 estos métodos no tienen implementación por defecto.
                override fun onProviderEnabled(provider: String) = Unit

                override fun onProviderDisabled(provider: String) = Unit

                @Deprecated("Solo se llama antes de Android 10")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
            }
            continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        }
    }

    private fun ageMillis(location: Location): Long =
        (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000

    private fun Location.toCoordinates() = Coordinates(latitude, longitude)

    private companion object {
        const val RECENT_MS = 10 * 60 * 1000L
        const val TIMEOUT_MS = 15_000L
    }
}
