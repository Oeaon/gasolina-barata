package com.gasolinabarata.app

import android.app.Application

class GasolinaBarataApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
