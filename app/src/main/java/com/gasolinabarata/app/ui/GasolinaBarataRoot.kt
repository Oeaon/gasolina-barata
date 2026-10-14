package com.gasolinabarata.app.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gasolinabarata.core.Station

@Composable
fun GasolinaBarataRoot(
    state: MainUiState,
    viewModel: MainViewModel,
    adsReady: Boolean,
    isPrivacyOptionsRequired: () -> Boolean,
    onPrivacyOptions: () -> Unit,
) {
    val context = LocalContext.current
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results -> viewModel.onPermissionResult(results.values.any { it }) }

    if (showSettings) {
        BackHandler { showSettings = false }
        SettingsScreen(
            settings = state.settings,
            showPrivacyOptions = remember { isPrivacyOptionsRequired() },
            onBack = { showSettings = false },
            onTankLitersChange = viewModel::setTankLiters,
            onPrivacyOptions = onPrivacyOptions,
        )
    } else {
        StationsScreen(
            state = state,
            adsReady = adsReady,
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
            onOpenSettings = { showSettings = true },
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
