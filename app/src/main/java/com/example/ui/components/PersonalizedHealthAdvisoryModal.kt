package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AirQualityData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HealthRecommendation(
    val riskLevel: String,
    val maskAdvice: String,
    val outdoorWindow: String,
    val indoorStrategy: String,
    val vulnerableTip: String,
    val clinicalSummary: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizedHealthAdvisoryModal(
    airQuality: AirQualityData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val profiles = listOf("General Public", "Asthma / Respiratory", "Children", "Elderly (60+)", "Outdoor Athletes")

    // Persistent storage for user profile preference
    val prefs = remember { context.getSharedPreferences("aeroguard_health_prefs", Context.MODE_PRIVATE) }
    var rememberProfile by remember {
        mutableStateOf(prefs.getBoolean("pref_remember_profile", false))
    }
    var selectedProfile by remember {
        val initial = if (prefs.getBoolean("pref_remember_profile", false)) {
            prefs.getString("pref_saved_profile", profiles[1]) ?: profiles[1]
        } else {
            profiles[1]
        }
        mutableStateOf(initial)
    }

    var isSimulatingGemini by remember { mutableStateOf(false) }

    // Generates simulated Gemini advisory tuned to current AQI and selected profile
    fun getAdviceForAqi(aqi: Int, profile: String): HealthRecommendation {
        return when {
            aqi <= 50 -> HealthRecommendation(
                riskLevel = "Minimal Risk",
                maskAdvice = "No respiratory protection required for $profile under normal conditions.",
                outdoorWindow = "All-day clearance. Morning and evening workouts are completely safe.",
                indoorStrategy = "Natural ventilation recommended. Open windows to circulate clean outdoor air.",
                vulnerableTip = "Great day for outdoor recreation and cardiovascular training in Durg parks.",
                clinicalSummary = "Gemini Analysis: Ambient PM2.5 and PM10 concentrations are well within WHO thresholds. Lung alveolar deposition risk is virtually zero."
            )
            aqi <= 100 -> HealthRecommendation(
                riskLevel = "Low to Mild Risk",
                maskAdvice = if (profile.contains("Asthma")) "Carry a basic mask if traveling near unpaved roads." else "Optional for general public.",
                outdoorWindow = "Safe for morning walks (6:00 AM - 9:00 AM). Avoid heavy truck corridors on NH-53.",
                indoorStrategy = "Standard ventilation. Keep cross-breeze flowing during afternoon hours.",
                vulnerableTip = "Those with severe respiratory hyper-responsiveness should keep inhalers handy.",
                clinicalSummary = "Gemini Analysis: Aerosol optical depth shows mild particulate accumulation from regional highway traffic. Baseline health impact remains minimal."
            )
            aqi <= 150 -> HealthRecommendation(
                riskLevel = "Moderate Bronchial Stress",
                maskAdvice = if (profile.contains("Asthma") || profile.contains("Children") || profile.contains("Elderly"))
                    "N95 or KN95 mask strongly advised for outdoor duration exceeding 20 minutes."
                else "N95 recommended near heavy traffic and construction zones.",
                outdoorWindow = "Best workout window: 5:30 AM - 7:00 AM. Avoid exercising between 7:00 PM - 10:00 PM due to temperature inversion.",
                indoorStrategy = "Operate True HEPA air purifier on medium speed in bedrooms. Keep road-facing windows closed during rush hour.",
                vulnerableTip = "Stay hydrated and consider warm saline gargles after prolonged exposure to reduce pharyngeal irritation.",
                clinicalSummary = "Gemini Analysis: At AQI $aqi (PM2.5: 54.2 µg/m³), fine particulates penetrate terminal bronchioles. Vulnerable groups will experience accelerated airway resistance."
            )
            aqi <= 200 -> HealthRecommendation(
                riskLevel = "High Respiratory Hazard",
                maskAdvice = "Strictly wear a certified N95 / FFP2 respirator with tight nasal seal outdoors.",
                outdoorWindow = "Postpone all vigorous outdoor cardio. Switch to indoor yoga or gym workouts.",
                indoorStrategy = "Run air purifiers with activated carbon layer continuously. Seal door gaps with damp towels if smoke penetrates.",
                vulnerableTip = "High risk of acute bronchospasm for asthmatics. Double check inhaler dosage with your pulmonologist.",
                clinicalSummary = "Gemini Clinical Advisory: High PM2.5 burden triggers systemic inflammatory cascades and oxidative stress. Immediate outdoor exposure reduction required."
            )
            else -> HealthRecommendation(
                riskLevel = "Severe Toxic Alert",
                maskAdvice = "Full N95 / P100 respirator mandatory. Avoid opening any exterior doors unnecessarily.",
                outdoorWindow = "Strict zero outdoor activity advisory. Remain indoors inside clean air shelter.",
                indoorStrategy = "Maximum HEPA filtration in sealed clean room. Avoid burning incense or cooking with open flame indoors.",
                vulnerableTip = "Emergency condition: Seek medical triage immediately if chest tightness or acute dyspnea occurs.",
                clinicalSummary = "Gemini Emergency Synthesis: Extreme particulate and aerosol saturation. Severe risk of cardiovascular and pulmonary distress across all age demographics."
            )
        }
    }

    var recommendation by remember {
        mutableStateOf(getAdviceForAqi(airQuality.aqi, selectedProfile))
    }

    fun triggerGeminiSimulation(profile: String) {
        coroutineScope.launch {
            isSimulatingGemini = true
            delay(550) // Realistic Gemini synthesis latency
            recommendation = getAdviceForAqi(airQuality.aqi, profile)
            isSimulatingGemini = false
        }
    }

    // Share Advisory text generator
    fun shareAdvisory() {
        val summaryText = buildString {
            appendLine("🛡️ AeroGuard - Personalized Health Advisory")
            appendLine("📍 Location: ${airQuality.locationName} (AQI ${airQuality.aqi} - ${airQuality.category.title})")
            appendLine("👤 Profile: $selectedProfile")
            appendLine("⚠️ Threat Level: ${recommendation.riskLevel}")
            appendLine()
            appendLine("😷 Mask Guidance:")
            appendLine(recommendation.maskAdvice)
            appendLine()
            appendLine("🏃 Outdoor Exercise Window:")
            appendLine(recommendation.outdoorWindow)
            appendLine()
            appendLine("🏠 Indoor Air Strategy:")
            appendLine(recommendation.indoorStrategy)
            appendLine()
            appendLine("💡 Clinical Summary:")
            appendLine(recommendation.clinicalSummary)
            appendLine()
            appendLine("Stay safe with AeroGuard • Hyper-Local Air Quality Intelligence")
        }

        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, summaryText)
                putExtra(Intent.EXTRA_SUBJECT, "AeroGuard Health Advisory for ${airQuality.locationName}")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Health Advisory via")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to launch share dialog", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("personalized_health_advisory_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Personalized Health Advisory",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = airQuality.category.color.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "AQI ${airQuality.aqi}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = airQuality.category.color,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = airQuality.locationName.split(",").firstOrNull() ?: "Durg",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_health_advisory_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Modal",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Selector chips
                Text(
                    text = "Select Demographic Health Profile:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profiles.take(3).forEach { prof ->
                        val isSelected = selectedProfile == prof
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedProfile = prof
                                if (rememberProfile) {
                                    prefs.edit().putString("pref_saved_profile", prof).apply()
                                }
                                triggerGeminiSimulation(prof)
                            },
                            label = { Text(prof, fontSize = 11.sp) },
                            modifier = Modifier.testTag("modal_profile_${prof.take(5)}")
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profiles.drop(3).forEach { prof ->
                        val isSelected = selectedProfile == prof
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedProfile = prof
                                if (rememberProfile) {
                                    prefs.edit().putString("pref_saved_profile", prof).apply()
                                }
                                triggerGeminiSimulation(prof)
                            },
                            label = { Text(prof, fontSize = 11.sp) },
                            modifier = Modifier.testTag("modal_profile_${prof.take(5)}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 'Remember my profile' Checkbox (Persistent storage)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val newChecked = !rememberProfile
                            rememberProfile = newChecked
                            prefs.edit().apply {
                                putBoolean("pref_remember_profile", newChecked)
                                if (newChecked) {
                                    putString("pref_saved_profile", selectedProfile)
                                }
                                apply()
                            }
                        }
                        .padding(vertical = 2.dp)
                        .testTag("remember_profile_checkbox_container")
                ) {
                    Checkbox(
                        checked = rememberProfile,
                        onCheckedChange = { checked ->
                            rememberProfile = checked
                            prefs.edit().apply {
                                putBoolean("pref_remember_profile", checked)
                                if (checked) {
                                    putString("pref_saved_profile", selectedProfile)
                                }
                                apply()
                            }
                        },
                        modifier = Modifier.testTag("remember_profile_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Remember my profile for future sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isSimulatingGemini) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Gemini AI is simulating hyper-local respiratory exposure...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // AI Clinical Summary banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Simulated Gemini Intelligence",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = recommendation.clinicalSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Risk Level Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Respiratory Threat Level",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = airQuality.category.color.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = recommendation.riskLevel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = airQuality.category.color,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Recommendations Cards
                        AdvisoryDetailRow(
                            icon = Icons.Default.Masks,
                            title = "Mask Recommendation",
                            body = recommendation.maskAdvice,
                            iconTint = MaterialTheme.colorScheme.error
                        )

                        AdvisoryDetailRow(
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            title = "Outdoor Workout Window",
                            body = recommendation.outdoorWindow,
                            iconTint = MaterialTheme.colorScheme.primary
                        )

                        AdvisoryDetailRow(
                            icon = Icons.Default.Air,
                            title = "Indoor Air Strategy",
                            body = recommendation.indoorStrategy,
                            iconTint = MaterialTheme.colorScheme.secondary
                        )

                        AdvisoryDetailRow(
                            icon = Icons.Default.Healing,
                            title = "Special Precautions",
                            body = recommendation.vulnerableTip,
                            iconTint = Color(0xFFD97706)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons: Share Advisory, Regenerate, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 'Share Advisory' Button
                    Button(
                        onClick = { shareAdvisory() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("share_advisory_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Advisory", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { triggerGeminiSimulation(selectedProfile) },
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("regenerate_gemini_advisory_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-run", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("dismiss_health_modal_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvisoryDetailRow(
    icon: ImageVector,
    title: String,
    body: String,
    iconTint: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
