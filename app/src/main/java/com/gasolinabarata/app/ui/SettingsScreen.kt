package com.gasolinabarata.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gasolinabarata.app.BuildConfig
import com.gasolinabarata.app.R
import com.gasolinabarata.app.data.AppSettings
import com.gasolinabarata.app.data.SettingsRepository
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    showPrivacyOptions: Boolean,
    onBack: () -> Unit,
    onTankLitersChange: (Int) -> Unit,
    onConsumptionChange: (Int) -> Unit,
    onPrivacyOptions: () -> Unit,
    isPremium: Boolean,
    onOpenPremium: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            SectionTitle(stringResource(R.string.tank_title))
            var liters by remember(settings.tankLiters) { mutableFloatStateOf(settings.tankLiters.toFloat()) }
            Text(stringResource(R.string.tank_liters, liters.roundToInt()))
            Slider(
                value = liters,
                onValueChange = { liters = it },
                onValueChangeFinished = { onTankLitersChange(liters.roundToInt()) },
                valueRange = SettingsRepository.MIN_TANK.toFloat()..SettingsRepository.MAX_TANK.toFloat(),
                steps = (SettingsRepository.MAX_TANK - SettingsRepository.MIN_TANK) / 5 - 1,
            )
            Text(
                stringResource(R.string.tank_detail),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle(stringResource(R.string.consumption_title))
            var tenths by remember(settings.consumptionTenths) {
                mutableFloatStateOf(settings.consumptionTenths.toFloat())
            }
            Text(stringResource(R.string.consumption_value, formatConsumption(tenths.roundToInt())))
            Slider(
                value = tenths,
                onValueChange = { tenths = it },
                onValueChangeFinished = { onConsumptionChange(tenths.roundToInt()) },
                valueRange = SettingsRepository.MIN_CONSUMPTION.toFloat()..SettingsRepository.MAX_CONSUMPTION.toFloat(),
                steps = (SettingsRepository.MAX_CONSUMPTION - SettingsRepository.MIN_CONSUMPTION) / 5 - 1,
            )
            Text(
                stringResource(R.string.consumption_detail),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (!isPremium) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                SectionTitle(stringResource(R.string.premium_title))
                Text(stringResource(R.string.premium_settings_text), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onOpenPremium, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.remove_ads))
                }
            }

            if (showPrivacyOptions) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                OutlinedButton(onClick = onPrivacyOptions) { Text(stringResource(R.string.privacy_options)) }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle(stringResource(R.string.about_title))
            Text(stringResource(R.string.about_text), style = MaterialTheme.typography.bodyMedium)

            // Google Play exige enlazar la fuente oficial de los precios.
            val uriHandler = LocalUriHandler.current
            Text(
                stringResource(R.string.source_title),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
            TextButton(onClick = { uriHandler.openUri(GEOPORTAL_URL) }) {
                Text(stringResource(R.string.source_geoportal))
            }
            TextButton(onClick = { uriHandler.openUri(PRICES_SERVICE_URL) }) {
                Text(stringResource(R.string.source_service))
            }
            Text(
                stringResource(R.string.about_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private const val GEOPORTAL_URL = "https://geoportalgasolineras.es/geoportal-instalaciones/Inicio"
private const val PRICES_SERVICE_URL =
    "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/help"

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
