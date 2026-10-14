package com.gasolinabarata.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class StationParserTest {
    private fun station(
        id: String,
        lat: String = "40,416775",
        lon: String = "-3,703790",
        gasolina95: String = "1,479",
        diesel: String = "1,379",
        sale: String = "P",
    ) = """
        {"C.P.":"28013","Dirección":"CALLE MAYOR, 1","Horario":"L-D: 24H","Latitud":"$lat",
         "Localidad":"MADRID","Longitud (WGS84)":"$lon","Margen":"D","Municipio":"Madrid",
         "Precio Gasoleo A":"$diesel","Precio Gasoleo Premium":"","Precio Gasolina 95 E5":"$gasolina95",
         "Precio Gasolina 98 E5":"","Precio Gases licuados del petróleo":"","Provincia":"MADRID",
         "Remisión":"dm","Rótulo":"REPSOL","Tipo Venta":"$sale","IDEESS":"$id","IDMunicipio":"4354",
         "IDProvincia":"28","IDCCAA":"13"}
    """.trimIndent()

    private fun response(vararg stations: String) =
        """{"Fecha":"13/10/2026 17:20:31","ListaEESSPrecio":[${stations.joinToString(",")}],"Nota":"","ResultadoConsulta":"OK"}"""

    @Test
    fun `lee decimales con coma y la fecha de actualizacion`() {
        val list = StationParser.parse(response(station("1")))

        assertEquals(LocalDateTime.of(2026, 10, 13, 17, 20, 31), list.updatedAt)
        val parsed = list.stations.single()
        assertEquals("REPSOL", parsed.brand)
        assertEquals(40.416775, parsed.latitude, 1e-9)
        assertEquals(-3.70379, parsed.longitude, 1e-9)
        assertEquals(1.479, parsed.prices.getValue(Fuel.GASOLINA_95), 1e-9)
        assertEquals(1.379, parsed.prices.getValue(Fuel.DIESEL), 1e-9)
    }

    @Test
    fun `los precios vacios no se guardan`() {
        val parsed = StationParser.parse(response(station("1", diesel = ""))).stations.single()

        assertFalse(Fuel.DIESEL in parsed.prices)
        assertFalse(Fuel.GLP in parsed.prices)
    }

    @Test
    fun `descarta venta restringida, sin coordenadas y sin precios`() {
        val list = StationParser.parse(
            response(
                station("1", sale = "R"),
                station("2", lat = ""),
                station("3", gasolina95 = "", diesel = ""),
                station("4"),
            ),
        )

        assertEquals(listOf("4"), list.stations.map { it.id })
    }

    @Test
    fun `una fecha rara no impide leer las gasolineras`() {
        val body = response(station("1")).replace("13/10/2026 17:20:31", "ayer")

        val list = StationParser.parse(body)

        assertNull(list.updatedAt)
        assertEquals(1, list.stations.size)
    }

    @Test(expected = StationFormatException::class)
    fun `sin lista de gasolineras lanza StationFormatException`() {
        StationParser.parse("""{"ResultadoConsulta":"ERROR"}""")
    }
}
