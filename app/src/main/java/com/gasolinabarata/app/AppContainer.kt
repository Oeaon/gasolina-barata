package com.gasolinabarata.app

import android.app.Application
import com.gasolinabarata.app.data.SettingsRepository
import com.gasolinabarata.app.data.StationsApi
import com.gasolinabarata.app.data.StationsCache
import com.gasolinabarata.app.data.StationsRepository
import com.gasolinabarata.app.data.settingsDataStore
import com.gasolinabarata.app.location.LocationProvider
import com.gasolinabarata.app.monetization.Monetization
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/** Dependencias compartidas de la app, creadas una vez en [GasolinaBarataApp]. */
class AppContainer(context: Application) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        // La lista completa de gasolineras ocupa varios MB.
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val settings = SettingsRepository(context.settingsDataStore)
    val stations = StationsRepository(StationsApi(httpClient), StationsCache(File(context.filesDir, "stations.json")))
    val location = LocationProvider(context.applicationContext)
    val monetization = Monetization(context)
}
