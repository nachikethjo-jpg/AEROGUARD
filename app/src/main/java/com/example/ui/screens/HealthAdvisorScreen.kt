package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AirQualityData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAdvisorScreen(
    airQuality: AirQualityData,
    onGetFastAdvice: suspend (profileType: String) -> String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val profiles = listOf(
        "General Adult",
        "Asthma & Respiratory",
        "Children (< 12 yrs)",
        "Elderly (60+ yrs)",
        "Athletes & Runners"
    )
    var selectedProfile by remember { mutableStateOf(profiles[1]) }
    var fastAdviceText by remember { mutableStateOf<String?>(null) }
    var isLoadingFastAdvice by remember { mutableStateOf(false) }

    LaunchedEffect(selectedProfile) {
        isLoadingFastAdvice = true
        fastAdviceText = onGetFastAdvice(selectedProfile)
        isLoadingFastAdvice = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Health Advisor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("health_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header summary
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                com.example.ui.components.MeteoconIcon(
                                    name = com.example.ui.components.getAqiMeteoconName(airQuality.aqi),
                                    size = 38.dp,
                                    contentDescription = null
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Personalized Lung Protection",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tailored for ${airQuality.locationName} (AQI ${airQuality.aqi} - ${airQuality.category.title})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Profile Selector
            item {
                Text(
                    text = "Select Health Profile",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profiles.take(3).forEach { prof ->
                        val isSelected = selectedProfile == prof
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedProfile = prof },
                            label = { Text(prof, fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_chip_${prof.take(5)}")
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profiles.drop(3).forEach { prof ->
                        val isSelected = selectedProfile == prof
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedProfile = prof },
                            label = { Text(prof, fontSize = 11.sp) },
                            modifier = Modifier.testTag("filter_chip_${prof.take(5)}")
                        )
                    }
                }
            }

            // Low Latency Gemini Flash-Lite Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.testTag("fast_advice_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Instant Clinical Prescription",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "gemini-3.1-flash-lite",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isLoadingFastAdvice) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Analyzing particulate exposure...", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            Text(
                                text = fastAdviceText ?: "Wear an N95 respirator if staying outdoors in Durg longer than 30 minutes. Keep rescue bronchodilators available.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoadingFastAdvice = true
                                    fastAdviceText = onGetFastAdvice(selectedProfile)
                                    isLoadingFastAdvice = false
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-assess", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Key Health Metrics Grid
            item {
                Text(
                    text = "Actionable Protections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                HealthActionCard(
                    icon = Icons.Default.Masks,
                    title = "Mask Recommendation",
                    description = airQuality.category.maskAdvice,
                    badge = if (airQuality.aqi > 100) "N95 / FFP2 Essential" else "Standard",
                    badgeColor = if (airQuality.aqi > 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            item {
                HealthActionCard(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    title = "Outdoor Workout Safety",
                    description = "Optimal window: 5:30 AM - 7:00 AM. Avoid workouts near G.E. Road after sunset when temperature inversion traps soot.",
                    badge = airQuality.category.outdoorScore,
                    badgeColor = airQuality.category.color
                )
            }

            item {
                HealthActionCard(
                    icon = Icons.Default.Air,
                    title = "Indoor Air & Ventilation",
                    description = "Run True HEPA air purifier on medium speed in sleeping quarters. Ventilate living room during 12:00 PM - 3:00 PM when atmospheric dispersion peaks.",
                    badge = "HEPA Recommended",
                    badgeColor = MaterialTheme.colorScheme.primary
                )
            }

            item {
                HealthActionCard(
                    icon = Icons.Default.Spa,
                    title = "Vulnerable Group Alert",
                    description = "Children walking to school near Padmanabhpur should wear filtration masks. Drink plenty of water and consume antioxidant-rich foods.",
                    badge = "High Vigilance",
                    badgeColor = MaterialTheme.colorScheme.error
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HealthActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
