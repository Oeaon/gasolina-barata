package com.gasolinabarata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gasolinabarata.app.ads.AdsConsent
import com.gasolinabarata.app.ui.GasolinaBarataRoot
import com.gasolinabarata.app.ui.MainViewModel
import com.gasolinabarata.app.ui.theme.GasolinaBarataTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory((application as GasolinaBarataApp).container)
    }
    private var adsReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AdsConsent.gather(this) { adsReady = true }

        setContent {
            GasolinaBarataTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                GasolinaBarataRoot(
                    state = state,
                    viewModel = viewModel,
                    adsReady = adsReady,
                    isPrivacyOptionsRequired = { AdsConsent.isPrivacyOptionsRequired(this) },
                    onPrivacyOptions = { AdsConsent.showPrivacyOptions(this) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}
