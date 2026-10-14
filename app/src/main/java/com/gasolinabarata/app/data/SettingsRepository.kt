package com.gasolinabarata.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gasolinabarata.core.Fuel
import com.gasolinabarata.core.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val fuel: Fuel = Fuel.GASOLINA_95,
    val radiusKm: Int = 10,
    val sort: SortOrder = SortOrder.PRICE,
    /** Litros de un depósito, para calcular el ahorro. */
    val tankLiters: Int = 50,
)

class SettingsRepository(private val store: DataStore<Preferences>) {
    val settings: Flow<AppSettings> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            AppSettings(
                fuel = Fuel.entries.firstOrNull { it.name == prefs[FUEL] } ?: Fuel.GASOLINA_95,
                radiusKm = prefs[RADIUS]?.takeIf { it in RADIUS_OPTIONS } ?: 10,
                sort = SortOrder.entries.firstOrNull { it.name == prefs[SORT] } ?: SortOrder.PRICE,
                tankLiters = (prefs[TANK] ?: 50).coerceIn(MIN_TANK, MAX_TANK),
            )
        }

    suspend fun setFuel(fuel: Fuel) {
        store.edit { it[FUEL] = fuel.name }
    }

    suspend fun setRadius(km: Int) {
        store.edit { it[RADIUS] = km }
    }

    suspend fun setSort(sort: SortOrder) {
        store.edit { it[SORT] = sort.name }
    }

    suspend fun setTankLiters(liters: Int) {
        store.edit { it[TANK] = liters.coerceIn(MIN_TANK, MAX_TANK) }
    }

    companion object {
        val RADIUS_OPTIONS = listOf(5, 10, 25)
        const val MIN_TANK = 20
        const val MAX_TANK = 100

        private val FUEL = stringPreferencesKey("fuel")
        private val RADIUS = intPreferencesKey("radius_km")
        private val SORT = stringPreferencesKey("sort")
        private val TANK = intPreferencesKey("tank_liters")
    }
}
