package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Renders an animated weather or air quality icon using the official Meteocons Lottie suite.
 */
@Composable
fun MeteoconIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    contentDescription: String? = null,
    backgroundColor: Color? = null,
    fallbackTint: Color = MaterialTheme.colorScheme.primary
) {
    val cleanName = name.removeSuffix(".json")
    val assetPath = "meteocons/$cleanName.json"

    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(assetPath)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    val boxModifier = if (backgroundColor != null) {
        modifier
            .size(size)
            .background(backgroundColor, shape = CircleShape)
            .testTag("meteocon_$cleanName")
    } else {
        modifier
            .size(size)
            .testTag("meteocon_$cleanName")
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(size)
            )
        } else {
            // Graceful fallback while loading or if asset is missing
            Icon(
                imageVector = if (cleanName.contains("sun") || cleanName.contains("clear")) {
                    Icons.Default.WbSunny
                } else {
                    Icons.Default.Air
                },
                contentDescription = contentDescription ?: cleanName,
                tint = fallbackTint,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

/**
 * Maps AQI values to representative Meteocon animated icon names.
 */
fun getAqiMeteoconName(aqi: Int, isDay: Boolean = true): String {
    return when {
        aqi <= 50 -> if (isDay) "clear-day" else "clear-night"
        aqi <= 100 -> if (isDay) "partly-cloudy-day" else "partly-cloudy-night"
        aqi <= 150 -> if (isDay) "haze-day" else "haze-night"
        aqi <= 200 -> if (isDay) "dust-day" else "dust-night"
        aqi <= 300 -> "smoke"
        else -> "extreme-thunderstorms"
    }
}

/**
 * Maps pollutant names to characteristic Meteocons.
 */
fun getPollutantMeteoconName(pollutantName: String): String {
    return when (pollutantName.uppercase().trim()) {
        "PM2.5" -> "smoke"
        "PM10" -> "dust"
        "SO₂", "SO2" -> "haze"
        "NO₂", "NO2" -> "mist"
        "CO" -> "smoke-particles"
        "O₃", "O3", "OZONE" -> "sun-hot"
        "NH₃", "NH3" -> "fog"
        "AQI" -> "barometer"
        else -> "wind"
    }
}
