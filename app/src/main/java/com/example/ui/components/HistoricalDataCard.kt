package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import kotlin.math.roundToInt

@Composable
fun HistoricalDataCard(
    currentCityName: String,
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf(HistoryTimeRange.WEEK) }
    var selectedMetric by remember { mutableStateOf(HistoryMetric.AQI) }

    val dataSet = remember(currentCityName, selectedRange, selectedMetric) {
        HistoricalDataRepository.getHistoricalData(currentCityName, selectedRange, selectedMetric)
    }

    // Interactive point scrubber index
    var activeScrubIndex by remember(dataSet) {
        mutableStateOf<Int?>(null)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("historical_data_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title, Subtitle, and Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MeteoconIcon(
                                name = getPollutantMeteoconName(selectedMetric.name),
                                size = 30.dp,
                                contentDescription = selectedMetric.displayName
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Historical Air Quality",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Past readings & trend shifts for $currentCityName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = selectedRange.shortLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Range Tabs (Week, Month, Year)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HistoryTimeRange.values().forEach { range ->
                        val isSelected = range == selectedRange
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedRange = range
                                    activeScrubIndex = null
                                }
                                .testTag("range_${range.name.lowercase()}_tab")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = range.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Selector Chips (AQI, PM2.5, PM10, NO2, SO2, CO, O3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryMetric.values().forEach { metric ->
                    val isSelected = metric == selectedMetric
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .clickable {
                                selectedMetric = metric
                                activeScrubIndex = null
                            }
                            .testTag("metric_chip_${metric.key}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            MeteoconIcon(
                                name = getPollutantMeteoconName(metric.name),
                                size = 18.dp,
                                contentDescription = metric.displayName
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = metric.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (metric.unit.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = metric.unit,
                                    fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4-Card Summary Metrics Row (Average, Peak, Lowest, Trend Change)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Average
                HistoricalStatBox(
                    title = "Average",
                    value = "${dataSet.avgValue}",
                    unit = dataSet.metric.unit,
                    subtitle = "Across ${dataSet.points.size} records",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f)
                )

                // Peak
                HistoricalStatBox(
                    title = "Peak High",
                    value = "${dataSet.peakValue}",
                    unit = dataSet.metric.unit,
                    subtitle = dataSet.peakDate,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    valueColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )

                // Minimum
                HistoricalStatBox(
                    title = "Lowest",
                    value = "${dataSet.minValue}",
                    unit = dataSet.metric.unit,
                    subtitle = dataSet.minDate,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Net Trend %
                val isDrop = dataSet.netChangePercent < 0
                val trendColor = if (isDrop) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
                HistoricalStatBox(
                    title = "Net Trend",
                    value = "${if (dataSet.netChangePercent > 0) "+" else ""}${dataSet.netChangePercent}%",
                    unit = "",
                    subtitle = if (isDrop) "Improved" else "Worsened",
                    containerColor = trendColor.copy(alpha = 0.12f),
                    valueColor = trendColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Title & Active Point Inspector Callout
            val displayPoint = activeScrubIndex?.let { dataSet.points.getOrNull(it) }
            AnimatedContent(
                targetState = displayPoint,
                label = "inspector_header"
            ) { point ->
                if (point != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = point.color.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, point.color.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${point.fullDate}: ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${point.value} ${dataSet.metric.unit}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = point.color
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = point.color
                                    ) {
                                        Text(
                                            text = point.category,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                point.eventNote?.let { note ->
                                    Text(
                                        text = "Significant Event: $note",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { activeScrubIndex = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${dataSet.metric.displayName} Historical Progression",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap or drag chart to inspect points",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Interactive Line Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.04f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .pointerInput(dataSet) {
                        detectTapGestures { offset ->
                            val pointCount = dataSet.points.size
                            if (pointCount > 1) {
                                val paddingHorizontal = 24.dp.toPx()
                                val chartWidth = size.width - (paddingHorizontal * 2)
                                val clampedX = (offset.x - paddingHorizontal).coerceIn(0f, chartWidth)
                                val step = chartWidth / (pointCount - 1)
                                val index = (clampedX / step).roundToInt().coerceIn(0, pointCount - 1)
                                activeScrubIndex = index
                            }
                        }
                    }
                    .pointerInput(dataSet) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                val pointCount = dataSet.points.size
                                if (pointCount > 1) {
                                    val paddingHorizontal = 24.dp.toPx()
                                    val chartWidth = size.width - (paddingHorizontal * 2)
                                    val clampedX = (change.position.x - paddingHorizontal).coerceIn(0f, chartWidth)
                                    val step = chartWidth / (pointCount - 1)
                                    val index = (clampedX / step).roundToInt().coerceIn(0, pointCount - 1)
                                    activeScrubIndex = index
                                }
                            }
                        )
                    }
                    .testTag("historical_line_chart_canvas")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val points = dataSet.points
                    if (points.isEmpty()) return@Canvas

                    val padL = 10.dp.toPx()
                    val padR = 10.dp.toPx()
                    val padTop = 16.dp.toPx()
                    val padBottom = 26.dp.toPx()

                    val graphW = w - padL - padR
                    val graphH = h - padTop - padBottom

                    val values = points.map { it.value }
                    val minVal = (values.minOrNull() ?: 0f) * 0.85f
                    val maxVal = ((values.maxOrNull() ?: 100f) * 1.15f).coerceAtLeast(dataSet.safeThreshold * 1.1f)
                    val range = if (maxVal - minVal > 0f) maxVal - minVal else 1f

                    val stepX = if (points.size > 1) graphW / (points.size - 1) else graphW

                    // Draw Threshold Safe Guideline
                    val thresholdRatio = ((dataSet.safeThreshold - minVal) / range).coerceIn(0f, 1f)
                    val thresholdY = padTop + graphH * (1f - thresholdRatio)
                    drawLine(
                        color = Color(0xFFEF4444).copy(alpha = 0.45f),
                        start = Offset(padL, thresholdY),
                        end = Offset(padL + graphW, thresholdY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                    )

                    // Draw Horizontal Gridlines (3 levels)
                    for (i in 0..3) {
                        val y = padTop + (graphH * (i / 3f))
                        drawLine(
                            color = Color(0xFF94A3B8).copy(alpha = 0.15f),
                            start = Offset(padL, y),
                            end = Offset(padL + graphW, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Compute point coordinates
                    val coords = points.mapIndexed { idx, pt ->
                        val x = padL + (idx * stepX)
                        val yRatio = ((pt.value - minVal) / range).coerceIn(0f, 1f)
                        val y = padTop + graphH * (1f - yRatio)
                        Offset(x, y)
                    }

                    // Build Curve Path & Gradient Fill
                    val linePath = Path()
                    val fillPath = Path()

                    coords.forEachIndexed { idx, pt ->
                        if (idx == 0) {
                            linePath.moveTo(pt.x, pt.y)
                            fillPath.moveTo(pt.x, padTop + graphH)
                            fillPath.lineTo(pt.x, pt.y)
                        } else {
                            val prev = coords[idx - 1]
                            val midX = (prev.x + pt.x) / 2f
                            linePath.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                            fillPath.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                        }
                    }

                    fillPath.lineTo(coords.last().x, padTop + graphH)
                    fillPath.close()

                    // Draw Gradient Fill under line
                    val primaryColor = Color(0xFF3B82F6)
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.02f)
                            ),
                            startY = padTop,
                            endY = padTop + graphH
                        )
                    )

                    // Draw Main Trend Line
                    drawPath(
                        path = linePath,
                        color = primaryColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // Draw Points and Highlight Rings
                    coords.forEachIndexed { idx, pt ->
                        val pointData = points[idx]
                        val isScrubbed = activeScrubIndex == idx
                        val radius = if (isScrubbed) 6.dp.toPx() else 3.5.dp.toPx()

                        // Outer ring for significant event or active scrub
                        if (pointData.eventNote != null || isScrubbed) {
                            drawCircle(
                                color = if (isScrubbed) pointData.color else Color(0xFFF59E0B),
                                radius = radius + 3.dp.toPx(),
                                center = pt,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        drawCircle(
                            color = pointData.color,
                            radius = radius,
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = radius * 0.45f,
                            center = pt
                        )
                    }

                    // Draw active scrub vertical indicator line
                    activeScrubIndex?.let { sIdx ->
                        if (sIdx in coords.indices) {
                            val activePt = coords[sIdx]
                            drawLine(
                                color = Color(0xFF3B82F6).copy(alpha = 0.8f),
                                start = Offset(activePt.x, padTop),
                                end = Offset(activePt.x, padTop + graphH),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }
                    }
                }

                // X-Axis Date Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val points = dataSet.points
                    // Display spaced labels depending on size
                    val stride = if (points.size > 8) 2 else 1
                    points.forEachIndexed { idx, pt ->
                        if (idx % stride == 0 || idx == points.lastIndex) {
                            val isScrubbed = activeScrubIndex == idx
                            Text(
                                text = pt.label,
                                fontSize = 9.sp,
                                fontWeight = if (isScrubbed) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isScrubbed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Threshold Label Tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp, top = 6.dp)
                ) {
                    Text(
                        text = "Standard Limit: ${dataSet.safeThreshold.toInt()} ${dataSet.metric.unit}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Significant Trend Changes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Key Trend Highlights & Anomalies",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${dataSet.highlights.size} Detected",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dataSet.highlights.forEach { highlight ->
                    TrendHighlightCard(highlight = highlight)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meteorological / Seasonality Insight Footer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dataSet.expertSummary,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoricalStatBox(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    containerColor: Color,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = valueColor
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = unit,
                        fontSize = 8.sp,
                        color = valueColor.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 8.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun TrendHighlightCard(
    highlight: TrendHighlight,
    modifier: Modifier = Modifier
) {
    val badgeColor = if (highlight.isWorsening) MaterialTheme.colorScheme.error else Color(0xFF16A34A)
    val badgeBg = if (highlight.isWorsening) MaterialTheme.colorScheme.errorContainer else Color(0xFFDCFCE7)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("trend_highlight_item")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = badgeBg,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (highlight.isWorsening) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = highlight.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = highlight.changeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Period: ${highlight.period}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = highlight.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
