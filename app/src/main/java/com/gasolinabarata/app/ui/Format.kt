package com.gasolinabarata.app.ui

import java.util.Locale

fun formatPricePerLiter(eur: Double): String = String.format(Locale.getDefault(), "%.3f €/L", eur)

fun formatEuros(eur: Double): String = String.format(Locale.getDefault(), "%.2f €", eur)

fun formatDistance(km: Double): String =
    if (km < 1) String.format(Locale.getDefault(), "%.0f m", km * 1000) else String.format(Locale.getDefault(), "%.1f km", km)
