package com.gasolinabarata.core

/** Carburantes que se muestran, con su campo en la API del Ministerio. */
enum class Fuel(val jsonKey: String) {
    GASOLINA_95("Precio Gasolina 95 E5"),
    GASOLINA_98("Precio Gasolina 98 E5"),
    DIESEL("Precio Gasoleo A"),
    DIESEL_PREMIUM("Precio Gasoleo Premium"),
    GLP("Precio Gases licuados del petróleo"),
}
