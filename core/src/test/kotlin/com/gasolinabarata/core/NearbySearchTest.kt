package com.gasolinabarata.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbySearchTest {
    // Puerta del Sol, Madrid.
    private val sunLat = 40.416775
    private val sunLon = -3.703790

    private fun station(id: String, lat: Double, lon: Double, vararg prices: Pair<Fuel, Double>) = Station(
        id = id, brand = "B$id", address = "", town = "", province = "", postalCode = "",
        latitude = lat, longitude = lon, schedule = "", prices = prices.toMap(),
    )

    private val stations = listOf(
        station("cerca-cara", 40.4200, -3.7050, Fuel.GASOLINA_95 to 1.60),
        station("cerca-barata", 40.4300, -3.7000, Fuel.GASOLINA_95 to 1.45),
        station("media", 40.4500, -3.6900, Fuel.GASOLINA_95 to 1.50, Fuel.DIESEL to 1.40),
        station("toledo", 39.8628, -4.0273, Fuel.GASOLINA_95 to 1.30),
        station("solo-diesel", 40.4180, -3.7040, Fuel.DIESEL to 1.35),
    )

    @Test
    fun `distancia conocida Madrid Toledo`() {
        val km = NearbySearch.distanceKm(sunLat, sunLon, 39.8628, -4.0273)

        assertEquals(67.5, km, 1.5)
    }

    @Test
    fun `filtra por radio y carburante y ordena por precio`() {
        val result = NearbySearch.find(stations, sunLat, sunLon, 10.0, Fuel.GASOLINA_95, SortOrder.PRICE)

        assertEquals(listOf("cerca-barata", "media", "cerca-cara"), result.stations.map { it.station.id })
        assertEquals(1.45, result.cheapestPrice!!, 1e-9)
        assertEquals((1.45 + 1.50 + 1.60) / 3, result.averagePrice!!, 1e-9)
    }

    @Test
    fun `ordena por distancia`() {
        val result = NearbySearch.find(stations, sunLat, sunLon, 10.0, Fuel.GASOLINA_95, SortOrder.DISTANCE)

        assertEquals(listOf("cerca-cara", "cerca-barata", "media"), result.stations.map { it.station.id })
        assertTrue(result.stations.zipWithNext().all { (a, b) -> a.distanceKm <= b.distanceKm })
    }

    @Test
    fun `calcula el ahorro de un deposito frente a la media`() {
        val result = NearbySearch.find(stations, sunLat, sunLon, 10.0, Fuel.GASOLINA_95, SortOrder.PRICE)

        val saving = result.savingVsAverage(result.stations.first(), liters = 50)

        assertEquals((result.averagePrice!! - 1.45) * 50, saving, 1e-9)
    }

    @Test
    fun `sin gasolineras en el radio no hay media`() {
        val result = NearbySearch.find(stations, 43.0, -8.0, 5.0, Fuel.GASOLINA_95, SortOrder.PRICE)

        assertTrue(result.stations.isEmpty())
        assertNull(result.averagePrice)
    }

    @Test
    fun `pone en minusculas los textos del ministerio`() {
        assertEquals("Avenida de la Constitucion, 26", prettyCase("AVENIDA DE LA CONSTITUCION, 26"))
    }
}
