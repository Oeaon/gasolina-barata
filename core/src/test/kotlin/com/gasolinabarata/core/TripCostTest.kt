package com.gasolinabarata.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripCostTest {
    private fun nearby(vararg prices: Double) = NearbyResult(
        prices.mapIndexed { i, price ->
            NearbyStation(
                Station("$i", "", "", "", "", "", 0.0, 0.0, "", mapOf(Fuel.DIESEL to price)),
                distanceKm = i.toDouble(),
                price = price,
            )
        },
    )

    @Test
    fun `calcula litros y coste en la mas barata y a precio medio`() {
        val estimate = TripCost.estimate(500.0, 6.0, nearby(1.40, 1.50, 1.60))!!

        assertEquals(30.0, estimate.liters, 1e-9)
        assertEquals(42.0, estimate.costAtCheapest, 1e-9)
        assertEquals(45.0, estimate.costAtAverage, 1e-9)
        assertEquals(3.0, estimate.saving, 1e-9)
    }

    @Test
    fun `sin distancia o sin gasolineras no hay estimacion`() {
        assertNull(TripCost.estimate(0.0, 6.0, nearby(1.4)))
        assertNull(TripCost.estimate(100.0, 6.0, nearby()))
    }
}
