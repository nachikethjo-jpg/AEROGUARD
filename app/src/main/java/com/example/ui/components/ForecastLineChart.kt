package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import com.example.data.model.AqiCategory
import com.example.data.model.HourlyForecast

data class HistoricalDayData(
    val dayLabel: String,
    val dateLabel: String,
    val aqi: Int,
    val pm25: Double,
    val note: String
)

@Composable
fun ForecastLineChart(
    forecast: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (forecast.isEmpty()) return

    // Historical 7-day trend sample data specifically modeled for Durg, Chhattisgarh
    val historicalDays = remember {
        listOf(
            HistoricalDayData("Thu", "Sep 25", 124, 46.2, "Moderate regional breeze, calm industrial dispersion along Shivnath basin"),
            HistoricalDayData("Fri", "Sep 26", 158, 62.4, "High industrial shift output near Bhilai boundary and Padmanabhpur"),
            HistoricalDayData("Sat", "Sep 27", 146, 56.8, "Weekend shopping vehicular congestion in Durg core and Indira market"),
            HistoricalDayData("Sun", "Sep 28", 112, 41.5, "Lowest weekly emissions; reduced commercial freight on NH-53"),
            HistoricalDayData("Mon", "Sep 29", 164, 66.0, "Weekday industrial restart and heavy highway transit dust"),
            HistoricalDayData("Tue", "Sep 30", 138, 52.1, "Clear afternoon skies, moderate particulate dispersion"),
            HistoricalDayData("Today", "Oct 01", 142, 54.2, "Current active sensor baseline across Durg urban core")
        )
    }

    var isHistoricalView by remember { mutableStateOf(false) }

    // Currently selected point index
    var selectedForecastIndex by remember { mutableStateOf<Int?>(null) }
    var selectedHistoryIndex by remember { mutableStateOf<Int?>(null) }

    val activeForecastItem = forecast.getOrNull(selectedForecastIndex ?: 0) ?: forecast.first()
    val activeHistoryItem = historicalDays.getOrNull(selectedHistoryIndex ?: (historicalDays.size - 1)) ?: historicalDays.last()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("forecast_chart_container")
    ) {
        // Toggle Row: 24h AI Forecast vs Past 7 Days History
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isHistoricalView) "7-Day Historical Trend" else "24-Hour Predictive Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHistoricalView) "Durg air quality history (Sep 25 - Oct 01)" else "Hourly variation model for Durg corridor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Segmented pill control
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.testTag("chart_mode_toggle_container")
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isHistoricalView) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .clickable { isHistoricalView = false }
                            .testTag("toggle_24h_forecast")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (!isHistoricalView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "24h Forecast",
                                fontSize = 11.sp,
                                fontWeight = if (!isHistoricalView) FontWeight.Bold else FontWeight.Normal,
                                color = if (!isHistoricalView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isHistoricalView) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .clickable { isHistoricalView = true }
                            .testTag("toggle_7d_history")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (isHistoricalView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "7-Day History",
                                fontSize = 11.sp,
                                fontWeight = if (isHistoricalView) FontWeight.Bold else FontWeight.Normal,
                                color = if (isHistoricalView) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Highlight Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isHistoricalView) "Date: ${activeHistoryItem.dayLabel} (${activeHistoryItem.dateLabel})" else "Time: ${activeForecastItem.hourLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val aqiVal = if (isHistoricalView) activeHistoryItem.aqi else activeForecastItem.predictedAqi
                    val pm25Val = if (isHistoricalView) activeHistoryItem.pm25 else activeForecastItem.pm25

                    MeteoconIcon(
                        name = getAqiMeteoconName(aqiVal),
                        size = 24.dp,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AQI $aqiVal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AqiCategory.fromAqi(aqiVal).color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PM2.5: ${String.format("%.1f", pm25Val)} µg/m³",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isHistoricalView) "Past 7 Days Trend" else "AI Model Projection",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active note for selected time / day
        Text(
            text = if (isHistoricalView) activeHistoryItem.note else activeForecastItem.note,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Line Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isHistoricalView, forecast) {
                        detectTapGestures { offset ->
                            if (isHistoricalView) {
                                val spacing = size.width / (historicalDays.size - 1).coerceAtLeast(1)
                                val tapped = ((offset.x + spacing / 2f) / spacing).toInt().coerceIn(0, historicalDays.lastIndex)
                                selectedHistoryIndex = tapped
                            } else {
                                val spacing = size.width / (forecast.size - 1).coerceAtLeast(1)
                                val tapped = ((offset.x + spacing / 2f) / spacing).toInt().coerceIn(0, forecast.lastIndex)
                                selectedForecastIndex = tapped
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val paddingBottom = 26.dp.toPx()
                val paddingTop = 16.dp.toPx()
                val usableHeight = height - paddingBottom - paddingTop

                val aqiValues = if (isHistoricalView) historicalDays.map { it.aqi } else forecast.map { it.predictedAqi }
                val maxAqi = (aqiValues.maxOrNull() ?: 200).coerceAtLeast(200)
                val minAqi = (aqiValues.minOrNull() ?: 50).coerceAtMost(50)
                val aqiRange = (maxAqi - minAqi).coerceAtLeast(1)

                val count = aqiValues.size
                val points = aqiValues.mapIndexed { index, aqi ->
                    val x = index * (width / (count - 1).coerceAtLeast(1))
                    val fraction = (aqi - minAqi).toFloat() / aqiRange
                    val y = (height - paddingBottom) - (fraction * usableHeight)
                    Offset(x, y)
                }

                // Grid horizontal guide lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = paddingTop + i * (usableHeight / gridLines)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Smooth Path Generation (cubic bezier)
                val strokePath = Path()
                val fillPath = Path()

                if (points.isNotEmpty()) {
                    strokePath.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, height - paddingBottom)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val current = points[i]
                        val next = points[i + 1]
                        val controlPoint1 = Offset(current.x + (next.x - current.x) / 2f, current.y)
                        val controlPoint2 = Offset(current.x + (next.x - current.x) / 2f, next.y)

                        strokePath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            next.x, next.y
                        )
                        fillPath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            next.x, next.y
                        )
                    }

                    fillPath.lineTo(points.last().x, height - paddingBottom)
                    fillPath.close()

                    val chartColor = if (isHistoricalView) Color(0xFF3B82F6) else Color(0xFFF97316)

                    // Gradient under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                chartColor.copy(alpha = 0.28f),
                                chartColor.copy(alpha = 0.0f)
                            ),
                            startY = paddingTop,
                            endY = height - paddingBottom
                        )
                    )

                    // Smooth line
                    drawPath(
                        path = strokePath,
                        color = chartColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw circles on points
                    val activeIndex = if (isHistoricalView) (selectedHistoryIndex ?: (historicalDays.size - 1)) else (selectedForecastIndex ?: 0)
                    points.forEachIndexed { index, point ->
                        val isSelected = index == activeIndex
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = chartColor,
                            radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                            center = point,
                            style = Stroke(width = if (isSelected) 3.dp.toPx() else 1.5.dp.toPx())
                        )
                    }
                }
            }

            // Labels along X Axis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isHistoricalView) {
                    historicalDays.forEachIndexed { index, item ->
                        Text(
                            text = item.dayLabel,
                            fontSize = 10.sp,
                            fontWeight = if (index == (selectedHistoryIndex ?: (historicalDays.size - 1))) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == (selectedHistoryIndex ?: (historicalDays.size - 1))) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    forecast.filterIndexed { index, _ -> index % 2 == 0 }.forEach { item ->
                        Text(
                            text = item.hourLabel,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Historical Data Export Action Bar
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "7-Day Historical Trend CSV",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Download recorded daily AQI & PM2.5 levels for Durg",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = {
                    val csvHeader = "Date,Day,AQI,PM2.5 (ug/m3),Category,Notes,Location\n"
                    val csvBody = historicalDays.joinToString("\n") { day ->
                        val category = AqiCategory.fromAqi(day.aqi).title
                        "\"${day.dateLabel}\",\"${day.dayLabel}\",${day.aqi},${day.pm25},\"$category\",\"${day.note}\",\"Durg, Chhattisgarh\""
                    }
                    val csvContent = csvHeader + csvBody

                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_SUBJECT, "Durg_AirQuality_7Day_Trend.csv")
                        putExtra(Intent.EXTRA_TEXT, csvContent)
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export Historical Data CSV")
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(shareIntent)
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("historical_data_export_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Historical Data Export",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
