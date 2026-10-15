package com.gasolinabarata.core

data class TripEstimate(val liters: Double, val costAtCheapest: Double, val costAtAverage: Double) {
    val saving: Double get() = costAtAverage - costAtCheapest
}

object TripCost {
    /** Coste de un viaje de [distanceKm] con un consumo de [litersPer100Km], repostando en la más barata o a precio medio. */
    fun estimate(distanceKm: Double, litersPer100Km: Double, nearby: NearbyResult): TripEstimate? {
        if (distanceKm <= 0 || litersPer100Km <= 0) return null
        val cheapest = nearby.cheapestPrice ?: return null
        val average = nearby.averagePrice ?: return null
        val liters = distanceKm * litersPer100Km / 100
        return TripEstimate(liters, liters * cheapest, liters * average)
    }
}
