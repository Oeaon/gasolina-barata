package com.gasolinabarata.core

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class StationFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Lee la respuesta de `EstacionesTerrestres` del Ministerio para la Transición Ecológica.
 * Todos los valores llegan como texto y los decimales con coma ("1,459").
 * Se descartan las gasolineras de venta restringida y las que no tienen coordenadas.
 */
object StationParser {
    private val json = Json { ignoreUnknownKeys = true }
    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss")
    private const val RESTRICTED_SALE = "R"

    fun parse(body: String): StationList {
        val root = try {
            json.parseToJsonElement(body) as? JsonObject
        } catch (e: SerializationException) {
            throw StationFormatException("Respuesta no es JSON", e)
        } ?: throw StationFormatException("Respuesta no es un objeto JSON")

        val list = root["ListaEESSPrecio"] as? JsonArray
            ?: throw StationFormatException("Falta ListaEESSPrecio")
        val stations = list.mapNotNull { (it as? JsonObject)?.toStation() }
        return StationList(parseDate(root.text("Fecha")), stations)
    }

    private fun JsonObject.toStation(): Station? {
        if (text("Tipo Venta") == RESTRICTED_SALE) return null
        val id = text("IDEESS") ?: return null
        val latitude = decimal("Latitud") ?: return null
        val longitude = decimal("Longitud (WGS84)") ?: return null
        val prices = Fuel.entries
            .mapNotNull { fuel -> decimal(fuel.jsonKey)?.takeIf { it > 0 }?.let { fuel to it } }
            .toMap()
        if (prices.isEmpty()) return null
        return Station(
            id = id,
            brand = text("Rótulo").orEmpty(),
            address = text("Dirección").orEmpty(),
            town = text("Localidad").orEmpty(),
            province = text("Provincia").orEmpty(),
            postalCode = text("C.P.").orEmpty(),
            latitude = latitude,
            longitude = longitude,
            schedule = text("Horario").orEmpty(),
            prices = prices,
        )
    }

    private fun JsonObject.text(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }

    private fun JsonObject.decimal(key: String): Double? = text(key)?.replace(',', '.')?.toDoubleOrNull()

    private fun parseDate(text: String?): LocalDateTime? = try {
        text?.let { LocalDateTime.parse(it, DATE_FORMAT) }
    } catch (e: DateTimeParseException) {
        null
    }
}
