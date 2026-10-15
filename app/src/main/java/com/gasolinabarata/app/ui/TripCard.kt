package com.gasolinabarata.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gasolinabarata.app.R
import com.gasolinabarata.app.ui.theme.cheapColor
import com.gasolinabarata.core.NearbyResult
import com.gasolinabarata.core.TripCost
import java.util.Locale

/** Función Premium: cuánto cuesta un viaje repostando en la más barata frente al precio medio. */
@Composable
fun TripCard(nearby: NearbyResult, litersPer100Km: Double) {
    var kmText by rememberSaveable { mutableStateOf("") }
    val km = kmText.replace(',', '.').toDoubleOrNull()
    val estimate = km?.let { TripCost.estimate(it, litersPer100Km, nearby) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.trip_title), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = kmText,
                onValueChange = { kmText = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(6) },
                label = { Text(stringResource(R.string.trip_distance)) },
                suffix = { Text("km") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.trip_consumption, formatConsumption((litersPer100Km * 10).toInt())),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (estimate != null) {
                Text(stringResource(R.string.trip_liters, String.format(Locale.getDefault(), "%.1f", estimate.liters)))
                Text(
                    stringResource(R.string.trip_cost_cheapest, formatEuros(estimate.costAtCheapest)),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(stringResource(R.string.trip_cost_average, formatEuros(estimate.costAtAverage)))
                if (estimate.saving >= 0.10) {
                    Text(stringResource(R.string.trip_saving, formatEuros(estimate.saving)), color = cheapColor())
                }
            }
        }
    }
}
