package com.gasolinabarata.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gasolinabarata.app.R
import com.gasolinabarata.app.monetization.AdBanner
import com.gasolinabarata.app.monetization.LockedFeature
import com.gasolinabarata.app.monetization.Monetization
import com.gasolinabarata.app.monetization.MonetizationState
import com.gasolinabarata.app.monetization.PremiumAction
import com.gasolinabarata.app.data.AppSettings
import com.gasolinabarata.app.data.SettingsRepository
import com.gasolinabarata.core.Fuel
import com.gasolinabarata.core.NearbyResult
import com.gasolinabarata.core.SortOrder
import com.gasolinabarata.core.Station
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsScreen(
    state: MainUiState,
    monetization: Monetization,
    monetizationState: MonetizationState,
    onFuelChange: (Fuel) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onNavigate: (Station) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPremium: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    PremiumAction(monetizationState, onOpenPremium)
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
        bottomBar = { AdBanner(monetizationState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Filters(state.settings, onFuelChange, onRadiusChange, onSortChange) }
                content(
                    state,
                    TripUnlock(monetization, monetizationState, onOpenPremium),
                    onRefresh,
                    onRequestPermission,
                    onOpenLocationSettings,
                    onNavigate,
                )
                item { Footer(state.stations) }
            }
        }
    }
}

/** Lo necesario para mostrar la calculadora de viajes bloqueada o desbloqueada. */
private class TripUnlock(
    val monetization: Monetization,
    val state: MonetizationState,
    val onOpenPremium: () -> Unit,
)

private fun LazyListScope.content(
    state: MainUiState,
    tripUnlock: TripUnlock,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onNavigate: (Station) -> Unit,
) {
    when (state.location) {
        LocationState.NeedsPermission -> item {
            ActionCard(stringResource(R.string.location_permission), stringResource(R.string.allow_location), onRequestPermission)
        }
        LocationState.Disabled -> item {
            ActionCard(stringResource(R.string.location_disabled), stringResource(R.string.open_settings), onOpenLocationSettings)
        }
        LocationState.Unavailable -> item {
            ActionCard(stringResource(R.string.location_unavailable), stringResource(R.string.retry), onRefresh)
        }
        LocationState.Locating -> item { Loading(stringResource(R.string.locating)) }
        is LocationState.Ready -> when (state.stations) {
            StationsState.Loading -> item { Loading(stringResource(R.string.loading_stations)) }
            StationsState.NetworkError -> item {
                ActionCard(stringResource(R.string.error_network), stringResource(R.string.retry), onRefresh)
            }
            StationsState.ServerError -> item {
                ActionCard(stringResource(R.string.error_server), stringResource(R.string.retry), onRefresh)
            }
            is StationsState.Ready -> results(state.nearby, state.settings, tripUnlock, onNavigate)
        }
    }
}

private fun LazyListScope.results(
    nearby: NearbyResult?,
    settings: AppSettings,
    tripUnlock: TripUnlock,
    onNavigate: (Station) -> Unit,
) {
    if (nearby == null || nearby.stations.isEmpty()) {
        item { Text(stringResource(R.string.no_stations, settings.radiusKm), style = MaterialTheme.typography.bodyLarge) }
        return
    }
    item { SummaryCard(nearby, settings) }
    item {
        LockedFeature(
            state = tripUnlock.state,
            monetization = tripUnlock.monetization,
            title = stringResource(R.string.trip_title),
            description = stringResource(R.string.trip_locked),
            onGoPremium = tripUnlock.onOpenPremium,
        ) {
            TripCard(nearby, settings.litersPer100Km)
        }
    }
    items(nearby.stations, key = { it.station.id }) { station ->
        StationCard(
            nearby = station,
            averagePrice = nearby.averagePrice,
            saving = nearby.savingVsAverage(station, settings.tankLiters),
            tankLiters = settings.tankLiters,
            onNavigate = { onNavigate(station.station) },
        )
    }
}

@Composable
private fun Filters(
    settings: AppSettings,
    onFuelChange: (Fuel) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onSortChange: (SortOrder) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Fuel.entries.forEach { fuel ->
                FilterChip(
                    selected = fuel == settings.fuel,
                    onClick = { onFuelChange(fuel) },
                    label = { Text(fuelName(fuel)) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val radii = SettingsRepository.RADIUS_OPTIONS
            SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                radii.forEachIndexed { index, km ->
                    SegmentedButton(
                        selected = km == settings.radiusKm,
                        onClick = { onRadiusChange(km) },
                        shape = SegmentedButtonDefaults.itemShape(index, radii.size),
                    ) { Text(stringResource(R.string.radius_km, km)) }
                }
            }
            val sorts = listOf(SortOrder.PRICE to R.string.sort_price, SortOrder.DISTANCE to R.string.sort_distance)
            SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                sorts.forEachIndexed { index, (sort, label) ->
                    SegmentedButton(
                        selected = sort == settings.sort,
                        onClick = { onSortChange(sort) },
                        shape = SegmentedButtonDefaults.itemShape(index, sorts.size),
                    ) { Text(stringResource(label)) }
                }
            }
        }
    }
}

@Composable
private fun Loading(message: String) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ActionCard(message: String, action: String, onAction: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun Footer(stations: StationsState) {
    val ready = stations as? StationsState.Ready
    Column {
        if (ready?.offline == true) {
            Text(
                stringResource(R.string.offline_data),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        ready?.list?.updatedAt?.let { updated ->
            Text(
                stringResource(R.string.updated_at, updated.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            stringResource(R.string.data_source),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun fuelName(fuel: Fuel): String = stringResource(
    when (fuel) {
        Fuel.GASOLINA_95 -> R.string.fuel_gasolina_95
        Fuel.GASOLINA_98 -> R.string.fuel_gasolina_98
        Fuel.DIESEL -> R.string.fuel_diesel
        Fuel.DIESEL_PREMIUM -> R.string.fuel_diesel_premium
        Fuel.GLP -> R.string.fuel_glp
    },
)
