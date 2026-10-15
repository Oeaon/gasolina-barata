package com.gasolinabarata.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gasolinabarata.app.AppContainer
import com.gasolinabarata.app.data.AppSettings
import com.gasolinabarata.app.data.SettingsRepository
import com.gasolinabarata.app.data.StationsRepository
import com.gasolinabarata.app.data.StationsResult
import com.gasolinabarata.app.location.Coordinates
import com.gasolinabarata.app.location.LocationProvider
import com.gasolinabarata.core.Fuel
import com.gasolinabarata.core.NearbyResult
import com.gasolinabarata.core.NearbySearch
import com.gasolinabarata.core.SortOrder
import com.gasolinabarata.core.StationList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

sealed interface LocationState {
    data object Locating : LocationState
    data object NeedsPermission : LocationState
    data object Disabled : LocationState
    data object Unavailable : LocationState
    data class Ready(val coordinates: Coordinates) : LocationState
}

sealed interface StationsState {
    data object Loading : StationsState
    data class Ready(val list: StationList, val offline: Boolean) : StationsState
    data object NetworkError : StationsState
    data object ServerError : StationsState
}

data class MainUiState(
    val settings: AppSettings = AppSettings(),
    val location: LocationState = LocationState.Locating,
    val stations: StationsState = StationsState.Loading,
    val nearby: NearbyResult? = null,
    val isRefreshing: Boolean = false,
)

class MainViewModel(
    private val stationsRepository: StationsRepository,
    private val settingsRepository: SettingsRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private var locateJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings -> updateAndSearch { it.copy(settings = settings) } }
        }
        viewModelScope.launch { loadStations(refresh = false) }
        locate()
    }

    /** Al volver a la app: puede que haya dado el permiso o activado la ubicación en Ajustes. */
    fun onResume() {
        val location = _state.value.location
        if (location == LocationState.NeedsPermission || location == LocationState.Disabled) locate()
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) locate() else updateAndSearch { it.copy(location = LocationState.NeedsPermission) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            joinAll(locate(), launch { loadStations(refresh = true) })
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun setFuel(fuel: Fuel) {
        viewModelScope.launch { settingsRepository.setFuel(fuel) }
    }

    fun setRadius(km: Int) {
        viewModelScope.launch { settingsRepository.setRadius(km) }
    }

    fun setSort(sort: SortOrder) {
        viewModelScope.launch { settingsRepository.setSort(sort) }
    }

    fun setConsumptionTenths(tenths: Int) {
        viewModelScope.launch { settingsRepository.setConsumptionTenths(tenths) }
    }

    fun setTankLiters(liters: Int) {
        viewModelScope.launch { settingsRepository.setTankLiters(liters) }
    }

    private fun locate(): Job {
        locateJob?.cancel()
        return viewModelScope.launch {
            val location = when {
                !locationProvider.hasPermission() -> LocationState.NeedsPermission
                !locationProvider.isEnabled() -> LocationState.Disabled
                else -> {
                    if (_state.value.location !is LocationState.Ready) {
                        _state.update { it.copy(location = LocationState.Locating) }
                    }
                    locationProvider.current()?.let { LocationState.Ready(it) } ?: LocationState.Unavailable
                }
            }
            updateAndSearch { it.copy(location = location) }
        }.also { locateJob = it }
    }

    private suspend fun loadStations(refresh: Boolean) {
        val stations = when (val result = stationsRepository.stations(refresh)) {
            is StationsResult.Available -> StationsState.Ready(result.list, result.offline)
            StationsResult.NetworkError -> StationsState.NetworkError
            StationsResult.ServerError -> StationsState.ServerError
        }
        // Si falla una actualización manual pero ya había datos, se mantienen.
        if (refresh && stations !is StationsState.Ready && _state.value.stations is StationsState.Ready) return
        updateAndSearch { it.copy(stations = stations) }
    }

    /** Aplica el cambio y recalcula las gasolineras cercanas (filtrar ~12.000 tarda milisegundos). */
    private fun updateAndSearch(change: (MainUiState) -> MainUiState) {
        _state.update { old ->
            val new = change(old)
            val location = new.location as? LocationState.Ready
            val stations = new.stations as? StationsState.Ready
            val nearby = if (location != null && stations != null) {
                NearbySearch.find(
                    stations = stations.list.stations,
                    latitude = location.coordinates.latitude,
                    longitude = location.coordinates.longitude,
                    radiusKm = new.settings.radiusKm.toDouble(),
                    fuel = new.settings.fuel,
                    sort = new.settings.sort,
                )
            } else {
                null
            }
            new.copy(nearby = nearby)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(container.stations, container.settings, container.location) }
        }
    }
}
