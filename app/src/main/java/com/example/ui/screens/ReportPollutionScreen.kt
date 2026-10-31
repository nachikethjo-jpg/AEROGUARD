package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.local.PollutionReportEntity
import com.example.ui.theme.AqiModerate
import com.example.ui.theme.AqiPoor
import com.example.ui.theme.AqiSevere
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPollutionScreen(
    reports: List<PollutionReportEntity>,
    onSubmitReport: suspend (sourceType: String, location: String, description: String, severity: String) -> Unit,
    onUpvoteReport: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Report Form, 1: Community Feed

    // Form fields
    val sourceOptions = listOf(
        "Open Garbage/Crop Burning",
        "Construction Dust",
        "Industrial Emission/Smoke",
        "Heavy Vehicle Exhaust",
        "Brick Kilns Smoke",
        "Other Visible Pollution"
    )
    var selectedSource by remember { mutableStateOf(sourceOptions[0]) }
    var locationInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var severityInput by remember { mutableStateOf("High") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var expandedSourceDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Pollution Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("report_back_button")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Report Incident", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.AddAlert, contentDescription = null) },
                    modifier = Modifier.testTag("tab_report_incident")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Community Feed (${reports.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    modifier = Modifier.testTag("tab_community_feed")
                )
            }

            if (selectedTab == 0) {
                // FORM TAB
                if (isSuccess) {
                    // Success View matching HTML design
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("report_success_view"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Report Submitted Successfully",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Thank you! Your report has been recorded locally and synced to the regional cloud feed. Nearby citizens and environmental sensors have been updated.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isSuccess = false
                                locationInput = ""
                                descriptionInput = ""
                            },
                            modifier = Modifier.testTag("submit_another_report_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Another Incident")
                        }

                        TextButton(
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("View Community Feed")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Help improve local accuracy by reporting visible pollution sources in Durg and neighboring sectors.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Source Type Picker
                        item {
                            Text(
                                text = "Source Type",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { expandedSourceDropdown = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("source_type_dropdown_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedSource)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedSourceDropdown,
                                    onDismissRequest = { expandedSourceDropdown = false }
                                ) {
                                    sourceOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                selectedSource = opt
                                                expandedSourceDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Location Details
                        item {
                            Text(
                                text = "Location Details",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = locationInput,
                                onValueChange = { locationInput = it },
                                placeholder = { Text("E.g., Near Padmanabhpur bypass, Durg...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("location_details_input"),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(Icons.Default.Place, contentDescription = null)
                                }
                            )

                            // Quick Location Pills
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Padmanabhpur", "G.E. Road", "Mohan Nagar", "Bhilai Border").forEach { chip ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { locationInput = chip }
                                    ) {
                                        Text(
                                            text = chip,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Severity Selector
                        item {
                            Text(
                                text = "Severity Level",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Moderate", "High", "Critical").forEach { level ->
                                    val isSelected = severityInput == level
                                    val color = when (level) {
                                        "Moderate" -> AqiModerate
                                        "High" -> AqiPoor
                                        else -> AqiSevere
                                    }
                                    OutlinedButton(
                                        onClick = { severityInput = level },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent
                                        ),
                                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                            width = if (isSelected) 2.dp else 1.dp
                                        )
                                    ) {
                                        Text(
                                            text = level,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Description
                        item {
                            Text(
                                text = "Description (Optional)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = descriptionInput,
                                onValueChange = { descriptionInput = it },
                                placeholder = { Text("E.g., Thick black smoke visible since 2 PM...") },
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("report_description_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Submit Button
                        item {
                            Button(
                                onClick = {
                                    if (locationInput.isBlank()) {
                                        locationInput = "Padmanabhpur, Durg"
                                    }
                                    isSubmitting = true
                                    coroutineScope.launch {
                                        onSubmitReport(
                                            selectedSource,
                                            locationInput,
                                            descriptionInput.ifBlank { "Localized pollution incident reported by resident." },
                                            severityInput
                                        )
                                        isSubmitting = false
                                        isSuccess = true
                                    }
                                },
                                enabled = !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_report_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submitting to Regional Network...")
                                } else {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit Local Report", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // COMMUNITY FEED TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Live Citizen Reports - Durg & Bhilai",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upvote reports you have observed to fast-track verification by municipal teams.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(reports) { item ->
                        ReportItemCard(
                            report = item,
                            onUpvote = { onUpvoteReport(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItemCard(
    report: PollutionReportEntity,
    onUpvote: () -> Unit
) {
    val timeAgo = remember(report.timestamp) {
        val diffMin = (System.currentTimeMillis() - report.timestamp) / (1000 * 60)
        when {
            diffMin < 60 -> "${diffMin.coerceAtLeast(1)}m ago"
            diffMin < 1440 -> "${diffMin / 60}h ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(report.timestamp))
        }
    }

    val statusColor = when (report.status) {
        "Verified" -> Color(0xFF10B981)
        "Action Taken" -> Color(0xFF0284C7)
        else -> Color(0xFFF59E0B)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("report_card_${report.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val meteo = when {
                        report.sourceType.contains("Burning", ignoreCase = true) || report.sourceType.contains("Smoke", ignoreCase = true) -> "smoke"
                        report.sourceType.contains("Dust", ignoreCase = true) -> "dust"
                        report.sourceType.contains("Industrial", ignoreCase = true) -> "smoke-particles"
                        else -> "haze"
                    }
                    com.example.ui.components.MeteoconIcon(
                        name = meteo,
                        size = 28.dp,
                        contentDescription = report.sourceType
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = report.sourceType,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = report.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.location,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = report.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reported by ${report.authorName} • $timeAgo",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Upvote Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clickable { onUpvote() }
                        .testTag("upvote_button_${report.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Upvote",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${report.upvotes}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
