package com.gasolinabarata.core

import java.time.LocalDateTime

/** Gasolinera de venta al público. Los precios están en €/litro con impuestos. */
data class Station(
    val id: String,
    val brand: String,
    val address: String,
    val town: String,
    val province: String,
    val postalCode: String,
    val latitude: Double,
    val longitude: Double,
    val schedule: String,
    val prices: Map<Fuel, Double>,
)

data class StationList(
    /** Hora oficial de los precios según el Ministerio (hora peninsular). */
    val updatedAt: LocalDateTime?,
    val stations: List<Station>,
)
