package com.gasolinabarata.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B57D0),
    secondary = Color(0xFF545F71),
    tertiary = Color(0xFF8A5100),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    secondary = Color(0xFFBCC7DB),
    tertiary = Color(0xFFFFB86E),
)

@Composable
fun GasolinaBarataTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

/** Verde para precios por debajo de la media de la zona, rojo para los de encima. */
@Composable
fun cheapColor(): Color = if (isSystemInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)

@Composable
fun expensiveColor(): Color = if (isSystemInDarkTheme()) Color(0xFFE57373) else Color(0xFFC62828)
