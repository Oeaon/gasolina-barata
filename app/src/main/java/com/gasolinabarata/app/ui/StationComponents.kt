package com.gasolinabarata.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gasolinabarata.app.R
import com.gasolinabarata.app.data.AppSettings
import com.gasolinabarata.app.ui.theme.cheapColor
import com.gasolinabarata.app.ui.theme.expensiveColor
import com.gasolinabarata.core.NearbyResult
import com.gasolinabarata.core.NearbyStation
import com.gasolinabarata.core.prettyCase

@Composable
fun SummaryCard(nearby: NearbyResult, settings: AppSettings) {
    val average = nearby.averagePrice ?: return
    val cheapest = nearby.stations.minBy { it.price }
    val saving = nearby.savingVsAverage(cheapest, settings.tankLiters)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.summary_title, nearby.stations.size, settings.radiusKm),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(stringResource(R.string.summary_average, formatPricePerLiter(average)))
            Text(
                stringResource(R.string.summary_cheapest, formatPricePerLiter(cheapest.price), formatDistance(cheapest.distanceKm)),
                fontWeight = FontWeight.SemiBold,
            )
            if (saving >= MIN_SAVING_EUR) {
                Text(
                    stringResource(R.string.summary_saving, formatEuros(saving), settings.tankLiters),
                    color = cheapColor(),
                )
            }
        }
    }
}

@Composable
fun StationCard(
    nearby: NearbyStation,
    averagePrice: Double?,
    saving: Double,
    tankLiters: Int,
    onNavigate: () -> Unit,
) {
    val station = nearby.station
    val priceColor = when {
        averagePrice == null -> MaterialTheme.colorScheme.onSurface
        nearby.price < averagePrice -> cheapColor()
        nearby.price > averagePrice -> expensiveColor()
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        station.brand.ifBlank { stringResource(R.string.unknown_brand) },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${prettyCase(station.address)} · ${prettyCase(station.town)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                    Text(
                        formatPricePerLiter(nearby.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = priceColor,
                    )
                    Text(formatDistance(nearby.distanceKm), style = MaterialTheme.typography.labelMedium)
                }
            }
            if (station.schedule.isNotBlank()) {
                Text(
                    station.schedule,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (saving >= MIN_SAVING_EUR) {
                    Text(
                        stringResource(R.string.station_saving, formatEuros(saving), tankLiters),
                        style = MaterialTheme.typography.labelMedium,
                        color = cheapColor(),
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                TextButton(onClick = onNavigate) { Text(stringResource(R.string.directions)) }
            }
        }
    }
}

/** Por debajo de unos céntimos no merece la pena destacar el ahorro. */
private const val MIN_SAVING_EUR = 0.10
