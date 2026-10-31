package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SmartWindowData(
    val outdoorAqi: Int,
    val outdoorHumidity: Int,
    val indoorAqi: Int,
    val indoorHumidity: Int,
    val shouldOpenWindows: Boolean,
    val suggestion: String,
    val rationale: String
)

enum class PurifierMode(val label: String, val desc: String) {
    AUTO("Auto IAQ", "Modulates fan speed based on real-time room AQI"),
    TURBO("Turbo", "Maximum fan speed to rapidly clear airborne pollutants"),
    SLEEP("Sleep / Silent", "Ultra-quiet low-speed operation with dimmed LEDs")
}

data class AirPurifierState(
    val isPoweredOn: Boolean = true,
    val mode: PurifierMode = PurifierMode.AUTO,
    val fanSpeed: Int = 2, // 1 to 3
    val filterLifePct: Int = 87,
    val targetAqi: Int = 35,
    val isAutoTriggerActive: Boolean = true,
    val lastTriggerReason: String = "Activated automatically because outdoor AQI is poor and indoor IAQ is 45"
)

object MockAirPurifierApi {
    private val _state = MutableStateFlow(AirPurifierState())
    val state: StateFlow<AirPurifierState> = _state.asStateFlow()

    fun togglePower(currentIndoorAqi: Int, shouldCloseWindows: Boolean) {
        val current = _state.value
        val nextPower = !current.isPoweredOn
        val fan = if (nextPower) {
            if (currentIndoorAqi > 75) 3 else if (currentIndoorAqi > 40) 2 else 1
        } else 0

        _state.value = current.copy(
            isPoweredOn = nextPower,
            fanSpeed = fan,
            lastTriggerReason = if (nextPower) {
                if (shouldCloseWindows) "Manual activation (recommended while outdoor air is unhealthy)"
                else "Manual power ON"
            } else "Powered off by user"
        )
    }

    fun setMode(mode: PurifierMode, currentIndoorAqi: Int) {
        val fan = when (mode) {
            PurifierMode.TURBO -> 3
            PurifierMode.SLEEP -> 1
            PurifierMode.AUTO -> if (currentIndoorAqi > 65) 3 else if (currentIndoorAqi > 40) 2 else 1
        }
        _state.value = _state.value.copy(
            isPoweredOn = true,
            mode = mode,
            fanSpeed = fan,
            lastTriggerReason = "Operating in ${mode.label} mode (Fan Level $fan)"
        )
    }

    fun evaluateIaqAutomation(currentIndoorAqi: Int, shouldCloseWindows: Boolean) {
        val current = _state.value
        if (!current.isAutoTriggerActive) return

        if (shouldCloseWindows || currentIndoorAqi > 50) {
            if (!current.isPoweredOn || current.fanSpeed < 2) {
                val speed = if (currentIndoorAqi > 75) 3 else 2
                _state.value = current.copy(
                    isPoweredOn = true,
                    fanSpeed = speed,
                    lastTriggerReason = "IAQ automation: Triggered ON at Fan Speed $speed because outdoor air is polluted and room IAQ is $currentIndoorAqi"
                )
            }
        }
    }
}

object SmartWindowMockService {
    fun getSmartWindowRecommendation(
        outdoorAqi: Int,
        outdoorHumidity: Int = 62,
        simulateFreshMode: Boolean = false
    ): SmartWindowData {
        if (simulateFreshMode) {
            val simulatedOutdoorAqi = 42
            val simulatedOutdoorHumidity = 48
            val simulatedIndoorAqi = 68
            val simulatedIndoorHumidity = 58
            return SmartWindowData(
                outdoorAqi = simulatedOutdoorAqi,
                outdoorHumidity = simulatedOutdoorHumidity,
                indoorAqi = simulatedIndoorAqi,
                indoorHumidity = simulatedIndoorHumidity,
                shouldOpenWindows = true,
                suggestion = "Open windows now",
                rationale = "Outdoor air is cleaner (AQI $simulatedOutdoorAqi vs Indoor $simulatedIndoorAqi). Perfect time for natural cross-ventilation."
            )
        }

        // Realistic indoor readings with air purifier active
        val indoorAqi = 45
        val indoorHumidity = 49

        val shouldOpen = outdoorAqi <= 85 && outdoorAqi <= indoorAqi && outdoorHumidity in 35..65

        val suggestion = if (shouldOpen) {
            "Open windows now"
        } else {
            "Close windows to maintain air quality"
        }

        val rationale = if (shouldOpen) {
            "Outdoor air is fresh and pleasant. Open windows to flush CO2 and refresh indoor air."
        } else {
            "Outdoor air is polluted (AQI $outdoorAqi vs Indoor $indoorAqi). Seal windows to protect indoor air quality and run air purifiers."
        }

        return SmartWindowData(
            outdoorAqi = outdoorAqi,
            outdoorHumidity = outdoorHumidity,
            indoorAqi = indoorAqi,
            indoorHumidity = indoorHumidity,
            shouldOpenWindows = shouldOpen,
            suggestion = suggestion,
            rationale = rationale
        )
    }
}

@Composable
fun SmartWindowHeaderIndicator(
    outdoorAqi: Int,
    outdoorHumidity: Int = 62,
    modifier: Modifier = Modifier
) {
    var simulateFreshAir by remember { mutableStateOf(false) }
    var showDetailsModal by remember { mutableStateOf(false) }

    val purifierState by MockAirPurifierApi.state.collectAsState()

    val windowData = remember(outdoorAqi, outdoorHumidity, simulateFreshAir) {
        SmartWindowMockService.getSmartWindowRecommendation(outdoorAqi, outdoorHumidity, simulateFreshAir)
    }

    // Automatically synchronize purifier state with real-time IAQ recommendation
    LaunchedEffect(windowData.indoorAqi, windowData.shouldOpenWindows) {
        MockAirPurifierApi.evaluateIaqAutomation(windowData.indoorAqi, !windowData.shouldOpenWindows)
    }

    // Fan animation for purifier state
    val infiniteTransition = rememberInfiniteTransition(label = "PurifierFanSpin")
    val fanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (purifierState.fanSpeed) {
                    3 -> 500
                    2 -> 1000
                    1 -> 1800
                    else -> 3000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "FanRotationAngle"
    )

    val isOpen = windowData.shouldOpenWindows
    val accentColor = if (isOpen) Color(0xFF10B981) else Color(0xFFF59E0B)
    val containerColor = if (isOpen) Color(0xFFECFDF5) else Color(0xFFFFFBEB)
    val borderColor = if (isOpen) Color(0xFFA7F3D0) else Color(0xFFFDE68A)
    val textColor = if (isOpen) Color(0xFF065F46) else Color(0xFF92400E)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_window_header_indicator")
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Top Row: Smart Window Recommendation & Simulator Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDetailsModal = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MeteoconIcon(
                                name = if (isOpen) "clear-day" else "dust",
                                size = 26.dp,
                                contentDescription = if (isOpen) "Fresh air - Open windows" else "High dust - Keep windows closed"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Smart Window",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = accentColor
                            ) {
                                Text(
                                    text = if (isOpen) "OPEN" else "CLOSE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = windowData.suggestion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    }
                }

                // Interactive Simulator Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(0.75.dp, borderColor),
                    modifier = Modifier
                        .clickable { simulateFreshAir = !simulateFreshAir }
                        .testTag("toggle_smart_window_sim")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (simulateFreshAir) "Sim: Fresh" else "Sim: Live",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle comparison
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDetailsModal = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Outdoor: AQI ${windowData.outdoorAqi} • ${windowData.outdoorHumidity}% RH  |  Indoor: AQI ${windowData.indoorAqi} • ${windowData.indoorHumidity}% RH",
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Details →",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = borderColor.copy(alpha = 0.7f),
                thickness = 0.8.dp
            )

            // --- 'Control My Purifier' Section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("control_my_purifier_section")
            ) {
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
                            color = if (purifierState.isPoweredOn) Color(0xFF3B82F6).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = "Air Purifier",
                                    tint = if (purifierState.isPoweredOn) Color(0xFF2563EB) else Color.Gray,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .then(
                                            if (purifierState.isPoweredOn) Modifier.rotate(fanRotation)
                                            else Modifier
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Control My Purifier",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (purifierState.isPoweredOn) Color(0xFF10B981) else Color.Gray
                                ) {
                                    Text(
                                        text = if (purifierState.isPoweredOn) "ON (${purifierState.mode.label})" else "OFF",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (purifierState.isPoweredOn) {
                                    "Target IAQ: <${purifierState.targetAqi} • Fan Spd: ${purifierState.fanSpeed}/3 • HEPA: ${purifierState.filterLifePct}%"
                                } else {
                                    "Purifier standby. Tap switch to filter indoor air."
                                },
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Power Switch for Purifier (Mock API toggle)
                    Switch(
                        checked = purifierState.isPoweredOn,
                        onCheckedChange = {
                            MockAirPurifierApi.togglePower(windowData.indoorAqi, !windowData.shouldOpenWindows)
                        },
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("purifier_power_switch")
                    )
                }

                // Quick Mode Selector Chips
                AnimatedVisibility(visible = purifierState.isPoweredOn) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PurifierMode.values().forEach { mode ->
                            val isSelected = purifierState.mode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF2563EB) else Color.White.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.75.dp,
                                    if (isSelected) Color(0xFF1D4ED8) else borderColor
                                ),
                                modifier = Modifier
                                    .clickable {
                                        MockAirPurifierApi.setMode(mode, windowData.indoorAqi)
                                    }
                                    .testTag("purifier_mode_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else textColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for detailed indoor vs outdoor comparison & Purifier settings
    if (showDetailsModal) {
        AlertDialog(
            onDismissRequest = { showDetailsModal = false },
            icon = {
                MeteoconIcon(
                    name = if (isOpen) "clear-day" else "dust",
                    size = 48.dp,
                    contentDescription = null
                )
            },
            title = {
                Text(
                    text = windowData.suggestion,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = windowData.rationale,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Sensor Comparison Table
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Metric", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Outdoor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Indoor (Room)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("AQI (US)", fontSize = 11.sp)
                                Text("${windowData.outdoorAqi}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${windowData.indoorAqi}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Humidity", fontSize = 11.sp)
                                Text("${windowData.outdoorHumidity}%", fontSize = 11.sp)
                                Text("${windowData.indoorHumidity}%", fontSize = 11.sp)
                            }
                        }
                    }

                    // Connected Purifier Telemetry Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Air,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AeroPure Pro H13 Purifier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                }

                                Switch(
                                    checked = purifierState.isPoweredOn,
                                    onCheckedChange = {
                                        MockAirPurifierApi.togglePower(windowData.indoorAqi, !windowData.shouldOpenWindows)
                                    },
                                    modifier = Modifier.scale(0.75f)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = purifierState.lastTriggerReason,
                                fontSize = 10.sp,
                                color = Color(0xFF1E40AF),
                                lineHeight = 13.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { purifierState.filterLifePct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFF2563EB),
                                trackColor = Color(0xFFDBEAFE)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("HEPA Filter Health", fontSize = 9.sp, color = Color(0xFF6B7280))
                                Text("${purifierState.filterLifePct}% Remaining", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            }
                        }
                    }

                    Text(
                        text = "Tip: AeroGuard IAQ automation triggers connected purifiers whenever outdoor air pollution exceeds safety thresholds or indoor PM2.5 rises.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsModal = false }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    simulateFreshAir = !simulateFreshAir
                }) {
                    Text(if (simulateFreshAir) "Reset to Live Data" else "Test Fresh Air Scenario")
                }
            }
        )
    }
}
