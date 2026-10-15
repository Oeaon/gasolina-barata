package com.gasolinabarata.app.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gasolinabarata.app.R
import com.gasolinabarata.app.monetization.Monetization
import com.gasolinabarata.app.monetization.MonetizationState
import com.gasolinabarata.app.monetization.PremiumScreen
import com.gasolinabarata.core.Station

private enum class Screen { STATIONS, SETTINGS, PREMIUM }

@Composable
fun GasolinaBarataRoot(
    state: MainUiState,
    viewModel: MainViewModel,
    monetization: Monetization,
    monetizationState: MonetizationState,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var screen by rememberSaveable { mutableStateOf(Screen.STATIONS) }
    // Volver a la lista es una pausa natural para un intersticial.
    val backToStations: () -> Unit = {
        screen = Screen.STATIONS
        activity?.let { monetization.maybeShowInterstitial(it) }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results -> viewModel.onPermissionResult(results.values.any { it }) }

    when (screen) {
        Screen.SETTINGS -> {
            BackHandler(onBack = backToStations)
            SettingsScreen(
                settings = state.settings,
                showPrivacyOptions = remember { activity?.let(monetization::isPrivacyOptionsRequired) ?: false },
                isPremium = monetizationState.isPremium,
                onBack = backToStations,
                onTankLitersChange = viewModel::setTankLiters,
                onConsumptionChange = viewModel::setConsumptionTenths,
                onPrivacyOptions = { activity?.let(monetization::showPrivacyOptions) },
                onOpenPremium = { screen = Screen.PREMIUM },
            )
        }
        Screen.PREMIUM -> {
            BackHandler { screen = Screen.STATIONS }
            PremiumScreen(
                state = monetizationState,
                monetization = monetization,
                features = listOf(stringResource(R.string.premium_feature_trip)),
                onBack = { screen = Screen.STATIONS },
            )
        }
        Screen.STATIONS -> StationsScreen(
            state = state,
            monetization = monetization,
            monetizationState = monetizationState,
            onFuelChange = viewModel::setFuel,
            onRadiusChange = viewModel::setRadius,
            onSortChange = viewModel::setSort,
            onRefresh = viewModel::refresh,
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                )
            },
            onOpenLocationSettings = { context.startActivitySafely(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
            onNavigate = { station -> context.openDirections(station) },
            onOpenSettings = { screen = Screen.SETTINGS },
            onOpenPremium = { screen = Screen.PREMIUM },
        )
    }
}

/** Abre la app de mapas del móvil con la ruta hasta la gasolinera. */
private fun Context.openDirections(station: Station) {
    val label = Uri.encode(station.brand.ifBlank { station.address })
    val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${station.latitude},${station.longitude}($label)"))
    if (!startActivitySafely(geo)) {
        startActivitySafely(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${station.latitude},${station.longitude}"),
            ),
        )
    }
}

private fun Context.startActivitySafely(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (e: ActivityNotFoundException) {
    false
}
