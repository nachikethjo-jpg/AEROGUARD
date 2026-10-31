package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayPollenForecast
import com.example.data.model.PollenMockService

@Composable
fun PollenForecastCard(
    currentCityName: String,
    modifier: Modifier = Modifier
) {
    val forecast = remember(currentCityName) {
        PollenMockService.getPollenForecast(currentCityName)
    }

    var selectedDayIndex by remember(currentCityName) { mutableIntStateOf(0) }
    val selectedDay = forecast.days.getOrElse(selectedDayIndex) { forecast.days.first() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pollen_forecast_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MeteoconIcon(
                                name = "pollen",
                                size = 32.dp,
                                contentDescription = "Pollen Count"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "5-Day Pollen Forecast",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bio-aerosols for ${forecast.cityName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = forecast.todayRisk.color.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, forecast.todayRisk.color.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = forecast.todayRisk.color,
                            modifier = Modifier.size(7.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Today: ${forecast.todayIndex}/12 ${forecast.todayRisk.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = forecast.todayRisk.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5-Day Horizontal Selector Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                forecast.days.forEachIndexed { index, day ->
                    val isSelected = selectedDayIndex == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedDayIndex = index }
                            .testTag("pollen_day_$index"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.dayLabel,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = day.dateLabel,
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = CircleShape,
                                color = day.riskLevel.color.copy(alpha = 0.2f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${day.overallIndex}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = day.riskLevel.color
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = day.riskLevel.title,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = day.riskLevel.color
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Detailed Allergen Breakdown for Selected Day
            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "PollenDetailsAnimated"
            ) { targetDay ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FilterVintage,
                                        contentDescription = null,
                                        tint = targetDay.riskLevel.color,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${targetDay.dayLabel} Dominant Allergen:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = targetDay.riskLevel.color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = targetDay.dominantPollen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = targetDay.riskLevel.color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4 Allergen Category Meters with Meteocons
                            PollenMeterRow(label = "Tree Pollen (Neem/Acacia)", percentage = targetDay.treePollenPct, color = Color(0xFF10B981), meteoconName = "pollen-tree")
                            Spacer(modifier = Modifier.height(6.dp))
                            PollenMeterRow(label = "Grass Pollen (Lawn/Pasture)", percentage = targetDay.grassPollenPct, color = Color(0xFFF59E0B), meteoconName = "pollen-grass")
                            Spacer(modifier = Modifier.height(6.dp))
                            PollenMeterRow(label = "Weed Pollen (Parthenium)", percentage = targetDay.weedPollenPct, color = Color(0xFFEA580C), meteoconName = "pollen-flower")
                            Spacer(modifier = Modifier.height(6.dp))
                            PollenMeterRow(label = "Mold Spores & Fungi", percentage = targetDay.moldSporesPct, color = Color(0xFF3B82F6), meteoconName = "mist")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sensitive User Clinical Advisory Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Allergy & Asthma Advisory (${targetDay.dayLabel})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = targetDay.advisory,
                                    fontSize = 10.sp,
                                    color = Color(0xFF166534),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Regional Bio-climate Note
            Text(
                text = "Regional Flora Synthesis: ${forecast.sensitiveAdvice}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun PollenMeterRow(
    label: String,
    percentage: Int,
    color: Color,
    meteoconName: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (meteoconName != null) {
                    MeteoconIcon(name = meteoconName, size = 18.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "$percentage%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
