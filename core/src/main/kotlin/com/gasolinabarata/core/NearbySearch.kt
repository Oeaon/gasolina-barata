package com.gasolinabarata.core

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt

enum class SortOrder { PRICE, DISTANCE }

data class NearbyStation(val station: Station, val distanceKm: Double, val price: Double)

data class NearbyResult(val stations: List<NearbyStation>) {
    val averagePrice: Double? = stations.takeIf { it.isNotEmpty() }?.map { it.price }?.average()
    val cheapestPrice: Double? = stations.minOfOrNull { it.price }

    /** Euros que se ahorran llenando [liters] litros aquí frente a la media de la zona. */
    fun savingVsAverage(station: NearbyStation, liters: Int): Double =
        averagePrice?.let { (it - station.price) * liters } ?: 0.0
}

object NearbySearch {
    private const val EARTH_RADIUS_KM = 6371.0
    private const val KM_PER_DEGREE = 111.32

    fun find(
        stations: List<Station>,
        latitude: Double,
        longitude: Double,
        radiusKm: Double,
        fuel: Fuel,
        sort: SortOrder,
    ): NearbyResult {
        // Caja previa en grados para no calcular la distancia exacta de las ~12.000 gasolineras.
        val latDelta = radiusKm / KM_PER_DEGREE
        val lonDelta = radiusKm / (KM_PER_DEGREE * cos(Math.toRadians(latitude)).coerceAtLeast(0.01))
        val nearby = stations
            .asSequence()
            .filter { kotlin.math.abs(it.latitude - latitude) <= latDelta && kotlin.math.abs(it.longitude - longitude) <= lonDelta }
            .mapNotNull { station ->
                val price = station.prices[fuel] ?: return@mapNotNull null
                val distance = distanceKm(latitude, longitude, station.latitude, station.longitude)
                if (distance <= radiusKm) NearbyStation(station, distance, price) else null
            }
            .toList()
        val sorted = when (sort) {
            SortOrder.PRICE -> nearby.sortedWith(compareBy({ it.price }, { it.distanceKm }))
            SortOrder.DISTANCE -> nearby.sortedBy { it.distanceKm }
        }
        return NearbyResult(sorted)
    }

    /** Distancia en línea recta (fórmula del semiverseno). */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin2(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin2(dLon / 2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
    }

    private fun sin2(x: Double): Double = kotlin.math.sin(x).let { it * it }
}
