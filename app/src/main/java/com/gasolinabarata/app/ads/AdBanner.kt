package com.gasolinabarata.app.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.gasolinabarata.app.BuildConfig

/** Banner adaptativo anclado. Solo en pantallas de consulta. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    val adView = remember(widthDp) {
        AdView(context).apply {
            adUnitId = BuildConfig.BANNER_AD_UNIT
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }
    AndroidView(factory = { adView }, modifier = modifier.fillMaxWidth())
}
