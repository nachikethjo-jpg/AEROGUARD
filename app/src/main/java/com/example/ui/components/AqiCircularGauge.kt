package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AqiCategory

@Composable
fun AqiCircularGauge(
    aqi: Int,
    category: AqiCategory,
    modifier: Modifier = Modifier
) {
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Sweep percentage (AQI max scaled to 350 for gauge visual)
    val sweepFraction = (aqi / 300f).coerceIn(0.1f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("aqi_circular_gauge"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Subtle glowing halo
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.08f))
            )

            // Canvas Gauge Ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = size / 2f

                // Track Background
                drawCircle(
                    color = category.color.copy(alpha = 0.18f),
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                // Foreground Value Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            category.color.copy(alpha = 0.7f),
                            category.color,
                            category.color
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * sweepFraction,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Center AQI Value Readout with animated Meteocon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                MeteoconIcon(
                    name = getAqiMeteoconName(aqi),
                    size = 44.dp,
                    contentDescription = "Current atmospheric condition"
                )
                Text(
                    text = "$aqi",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = category.color,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.testTag("current_aqi_value")
                )
                Text(
                    text = "AQI (US)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(category.color.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("aqi_status_pill")
        ) {
            Text(
                text = category.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = category.color
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Health advisory snippet
        Text(
            text = category.advisory,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}
