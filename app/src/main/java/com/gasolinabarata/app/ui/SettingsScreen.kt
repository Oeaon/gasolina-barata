package com.gasolinabarata.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    onPrivacyOptions: () -> Unit,
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

            if (showPrivacyOptions) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                OutlinedButton(onClick = onPrivacyOptions) { Text(stringResource(R.string.privacy_options)) }
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            SectionTitle(stringResource(R.string.about_title))
            Text(stringResource(R.string.about_text), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
