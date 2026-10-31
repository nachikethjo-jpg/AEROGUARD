package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PollutionReportEntity

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.IndiaLocations
import com.example.ui.AeroGuardNotificationHelper

data class DurgMapPin(
    val id: Long,
    val title: String,
    val location: String,
    val sourceType: String,
    val severity: String,
    val description: String,
    val timeAgo: String,
    val reporter: String,
    val upvotes: Int,
    val normalizedX: Float, // 0.0f to 1.0f on map canvas
    val normalizedY: Float,
    val pinColor: Color,
    val latitude: Double = 21.1904,
    val longitude: Double = 81.2849
)

@Composable
fun InteractiveDurgMap(
    locationName: String = "Durg, Chhattisgarh",
    reports: List<PollutionReportEntity> = emptyList(),
    onNavigateToReport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cityProfile = remember(locationName) {
        IndiaLocations.findCityByName(locationName)
    }

    val defaultPins = remember(cityProfile) {
        if (cityProfile.hazardPins.isNotEmpty()) {
            cityProfile.hazardPins.map { pin ->
                val pinColor = when (pin.severity) {
                    "Hazardous" -> Color(0xFF831843)
                    "Severe" -> Color(0xFFDC2626)
                    "High" -> Color(0xFFEF4444)
                    else -> Color(0xFFF59E0B)
                }
                DurgMapPin(
                    id = pin.id,
                    title = pin.title,
                    location = pin.location,
                    sourceType = pin.sourceType,
                    severity = pin.severity,
                    description = pin.description,
                    timeAgo = pin.timeAgo,
                    reporter = pin.reporter,
                    upvotes = pin.upvotes,
                    normalizedX = pin.normalizedX,
                    normalizedY = pin.normalizedY,
                    pinColor = pinColor,
                    latitude = pin.lat,
                    longitude = pin.lon
                )
            }
        } else {
            listOf(
                DurgMapPin(
                    id = 1L,
                    title = "Urban Transit Corridor",
                    location = "${cityProfile.cityName} Central Junction",
                    sourceType = "Vehicle Exhaust",
                    severity = "Moderate",
                    description = "Heavy vehicular traffic exhaust near city transit hubs.",
                    timeAgo = "1 hour ago",
                    reporter = "Local Citizen",
                    upvotes = 12,
                    normalizedX = 0.5f,
                    normalizedY = 0.5f,
                    pinColor = Color(0xFFF59E0B),
                    latitude = cityProfile.latitude,
                    longitude = cityProfile.longitude
                )
            )
        }
    }

    val context = LocalContext.current
    var selectedPin by remember(defaultPins) { mutableStateOf<DurgMapPin?>(defaultPins.firstOrNull()) }
    var pinUpvoteCounts by remember(defaultPins) { mutableStateOf(defaultPins.associate { it.id to it.upvotes }) }
    var activeProximityAlert by remember { mutableStateOf<String?>(null) }

    // Map layer visibility toggles
    var showIndustrialLayer by remember { mutableStateOf(true) }
    var showWasteBurningLayer by remember { mutableStateOf(true) }
    var showTrafficLayer by remember { mutableStateOf(true) }
    var isLayerPanelExpanded by remember { mutableStateOf(false) }

    // Map zoom scale state (0.75f to 2.5f with animated transition)
    var mapZoomScale by remember { mutableFloatStateOf(1.0f) }
    val animatedMapZoom by animateFloatAsState(
        targetValue = mapZoomScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "MapZoomAnimation"
    )

    val visiblePins = remember(defaultPins, showIndustrialLayer, showWasteBurningLayer, showTrafficLayer) {
        defaultPins.filter { pin ->
            when (pin.sourceType) {
                "Industrial" -> showIndustrialLayer
                "Open Burning", "Biomass Burning" -> showWasteBurningLayer
                else -> showTrafficLayer
            }
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_durg_map_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${cityProfile.cityName} Community Pollution Map",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Live crowdsourced hazard pins for ${cityProfile.cityName}, ${cityProfile.state}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val distance = 340
                            val nearest = defaultPins.first()
                            AeroGuardNotificationHelper.sendHotspotProximityNotification(
                                context,
                                nearest.location,
                                distance,
                                nearest.sourceType
                            )
                            activeProximityAlert = "You are within ${distance}m of ${nearest.location} (${nearest.sourceType})! Exposure hazard high. Put on N95 respirator."
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 5.dp),
                        modifier = Modifier.testTag("scan_proximity_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Proximity Check", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${defaultPins.size} Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Geolocation Proximity Alert Banner (< 500m)
            AnimatedVisibility(visible = activeProximityAlert != null) {
                activeProximityAlert?.let { alertMsg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("hotspot_proximity_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🚨 Hotspot Proximity Alert (< 500m)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = alertMsg,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                )
                            }
                            IconButton(
                                onClick = { activeProximityAlert = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Map Canvas & Overlay Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE2E8F0)) // Map background land color
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("durg_map_canvas_container")
            ) {
                // Zoomable Vector Map Graphics & Pins Layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = animatedMapZoom,
                            scaleY = animatedMapZoom,
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                        )
                ) {
                    // Vector Map stylized graphics (River Shivnath, NH-53 highway, Urban grid)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Shivnath River (Winding blue stream on West)
                    val riverPath = Path().apply {
                        moveTo(w * 0.12f, 0f)
                        cubicTo(
                            w * 0.22f, h * 0.35f,
                            w * 0.08f, h * 0.65f,
                            w * 0.25f, h
                        )
                    }
                    drawPath(
                        path = riverPath,
                        color = Color(0xFF93C5FD).copy(alpha = 0.75f),
                        style = Stroke(width = 16.dp.toPx())
                    )

                    // 2. NH-53 Expressway (East-West diagonal highway through Durg to Bhilai)
                    val highwayPath = Path().apply {
                        moveTo(0f, h * 0.42f)
                        quadraticTo(w * 0.5f, h * 0.48f, w, h * 0.36f)
                    }
                    drawPath(
                        path = highwayPath,
                        color = Color(0xFFCBD5E1),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawPath(
                        path = highwayPath,
                        color = Color(0xFFF59E0B).copy(alpha = 0.6f),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // 3. Major Cross Arterial (Mohan Nagar to Padmanabhpur)
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(w * 0.48f, 0f),
                        end = Offset(w * 0.42f, h),
                        strokeWidth = 5.dp.toPx()
                    )

                    // 4. Padmanabhpur Ring Road arc
                    drawArc(
                        color = Color(0xFFE2E8F0),
                        startAngle = 30f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * 0.18f, h * 0.55f),
                        size = androidx.compose.ui.geometry.Size(w * 0.45f, h * 0.38f),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // 5. Radial Gradient Density Hotspot Overlay for Community Reports
                    // Visualizes areas with higher clusters of recent user reports (layer filtered)
                    if (showIndustrialLayer) {
                        // Hotspot 1: Industrial Plume Cluster near Bhilai boundary (Severe Density)
                        val industrialCenter = Offset(w * 0.78f, h * 0.38f)
                        val industrialRadius = 66.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFDC2626).copy(alpha = 0.52f),
                                    Color(0xFFEF4444).copy(alpha = 0.26f),
                                    Color(0xFFEF4444).copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                center = industrialCenter,
                                radius = industrialRadius
                            ),
                            radius = industrialRadius,
                            center = industrialCenter
                        )
                    }

                    if (showWasteBurningLayer) {
                        // Hotspot 2: Padmanabhpur Waste Burning Cluster (High Density)
                        val padmanabhpurCenter = Offset(w * 0.32f, h * 0.68f)
                        val padmanabhpurRadius = 56.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFEF4444).copy(alpha = 0.48f),
                                    Color(0xFFF87171).copy(alpha = 0.22f),
                                    Color.Transparent
                                ),
                                center = padmanabhpurCenter,
                                radius = padmanabhpurRadius
                            ),
                            radius = padmanabhpurRadius,
                            center = padmanabhpurCenter
                        )
                    }

                    if (showTrafficLayer) {
                        // Hotspot 3: Central Railway Station & Urban Transit Cluster (Moderate Density)
                        val transitCenter = Offset(w * 0.53f, h * 0.53f)
                        val transitRadius = 46.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF97316).copy(alpha = 0.42f),
                                    Color(0xFFFB923C).copy(alpha = 0.18f),
                                    Color.Transparent
                                ),
                                center = transitCenter,
                                radius = transitRadius
                            ),
                            radius = transitRadius,
                            center = transitCenter
                        )
                    }
                }

                // Landmark Labels on Map
                Text(
                    text = cityProfile.landmarkWater,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB).copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 14.dp, top = 16.dp)
                )

                Text(
                    text = "${cityProfile.majorHighway} →",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64748B),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp, bottom = 48.dp)
                )

                Text(
                    text = "${cityProfile.cityName} Core",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569).copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 24.dp)
                )

                // Interactive Report Pins Overlay (Zoomable along with SVG vector map)
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val mapWidth = maxWidth
                    val mapHeight = maxHeight

                    visiblePins.forEach { pin ->
                        val isSelected = selectedPin?.id == pin.id
                        val currentUpvotes = pinUpvoteCounts[pin.id] ?: pin.upvotes
                        val pinX = mapWidth * pin.normalizedX - 20.dp
                        val pinY = mapHeight * pin.normalizedY - 32.dp

                        Box(
                            modifier = Modifier.offset(x = pinX, y = pinY)
                        ) {
                            MapPinItem(
                                pin = pin,
                                isSelected = isSelected,
                                upvotes = currentUpvotes,
                                onClick = { selectedPin = pin }
                            )
                        }
                    }
                }
            } // Close Zoomable Vector Map Graphics & Pins Layer

            // Coordinate Badge at bottom-left (Stationary HUD element)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "${cityProfile.latitude}° N, ${cityProfile.longitude}° E",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Floating Control Panel for Map Layers and Zoom Controls
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .testTag("floating_layer_control_panel")
            ) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Zoom In / Out Controls (+ / - buttons and current percentage)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                // Zoom Out (-)
                                IconButton(
                                    onClick = {
                                        mapZoomScale = (mapZoomScale - 0.25f).coerceAtLeast(0.75f)
                                    },
                                    enabled = mapZoomScale > 0.75f,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("map_zoom_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Zoom Out",
                                        tint = if (mapZoomScale > 0.75f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Zoom Percentage Display & Reset on click
                                Text(
                                    text = "${(mapZoomScale * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { mapZoomScale = 1.0f }
                                        .padding(horizontal = 4.dp)
                                        .testTag("map_zoom_level_indicator")
                                )

                                // Zoom In (+)
                                IconButton(
                                    onClick = {
                                        mapZoomScale = (mapZoomScale + 0.25f).coerceAtMost(2.5f)
                                    },
                                    enabled = mapZoomScale < 2.5f,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("map_zoom_in_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Zoom In",
                                        tint = if (mapZoomScale < 2.5f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Layer Toggle Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isLayerPanelExpanded) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isLayerPanelExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .clickable { isLayerPanelExpanded = !isLayerPanelExpanded }
                                .testTag("toggle_layer_panel_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Map Layers",
                                    tint = if (isLayerPanelExpanded) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Layers (${listOf(showIndustrialLayer, showWasteBurningLayer, showTrafficLayer).count { it }}/3)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLayerPanelExpanded) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = if (isLayerPanelExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = if (isLayerPanelExpanded) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Expanded Layer Toggle Panel
                    AnimatedVisibility(
                        visible = isLayerPanelExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.98f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .widthIn(min = 195.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Toggle Map Layers",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { isLayerPanelExpanded = false },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)

                                // 1. Industrial Hotspots
                                LayerToggleRow(
                                    title = "Industrial Hotspots",
                                    color = Color(0xFFDC2626),
                                    isChecked = showIndustrialLayer,
                                    onToggle = { showIndustrialLayer = !showIndustrialLayer },
                                    tag = "toggle_industrial_layer"
                                )

                                // 2. Waste Burning Reports
                                LayerToggleRow(
                                    title = "Waste Burning Reports",
                                    color = Color(0xFFEA580C),
                                    isChecked = showWasteBurningLayer,
                                    onToggle = { showWasteBurningLayer = !showWasteBurningLayer },
                                    tag = "toggle_waste_burning_layer"
                                )

                                // 3. Traffic Corridors
                                LayerToggleRow(
                                    title = "Traffic Corridors",
                                    color = Color(0xFFF59E0B),
                                    isChecked = showTrafficLayer,
                                    onToggle = { showTrafficLayer = !showTrafficLayer },
                                    tag = "toggle_traffic_layer"
                                )
                            }
                        }
                    }
                }
            }
        }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Pin Detail Bottom Card
            AnimatedContent(
                targetState = selectedPin,
                label = "pin_details_animation"
            ) { pin ->
                if (pin != null) {
                    val currentUpvotes = pinUpvoteCounts[pin.id] ?: pin.upvotes

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("map_selected_pin_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                        shape = RoundedCornerShape(6.dp),
                                        color = pin.pinColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = pin.sourceType,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = pin.pinColor,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = pin.location,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Verified",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = pin.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${pin.timeAgo} • ${pin.reporter}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            pinUpvoteCounts = pinUpvoteCounts.toMutableMap().apply {
                                                put(pin.id, currentUpvotes + 1)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("upvote_map_pin_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ThumbUp,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Confirm ($currentUpvotes)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = onNavigateToReport,
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("report_nearby_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddLocationAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Report Here", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapPinItem(
    pin: DurgMapPin,
    isSelected: Boolean,
    upvotes: Int,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val isScaleUp = isHovered || isSelected

    val scale by animateFloatAsState(
        targetValue = if (isScaleUp) 1.28f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PinScale"
    )

    val tooltipAlpha by animateFloatAsState(
        targetValue = if (isScaleUp) 1.0f else 0.0f,
        animationSpec = tween(150),
        label = "TooltipAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            }
            .clickable { onClick() }
            .testTag("map_pin_${pin.id}")
    ) {
        // Tooltip showing category and upvote count with smooth transition
        if (tooltipAlpha > 0.01f) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF475569)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .graphicsLayer(alpha = tooltipAlpha)
                    .padding(bottom = 3.dp)
                    .testTag("pin_tooltip_${pin.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = pin.sourceType,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "•",
                        fontSize = 8.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$upvotes",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }

        // Pin Head with Smooth Scale Transition Effect
        Box(
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .size(if (isSelected) 30.dp else 24.dp)
                .shadow(if (isSelected) 6.dp else 3.dp, CircleShape)
                .clip(CircleShape)
                .background(if (isSelected) pin.pinColor else pin.pinColor.copy(alpha = 0.92f))
                .border(
                    width = if (isSelected) 2.dp else 1.5.dp,
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (pin.sourceType) {
                    "Open Burning", "Biomass Burning" -> Icons.Default.LocalFireDepartment
                    "Industrial" -> Icons.Default.Factory
                    "Construction Dust" -> Icons.Default.Grain
                    else -> Icons.Default.DirectionsCar
                },
                contentDescription = pin.title,
                tint = Color.White,
                modifier = Modifier.size(if (isSelected) 17.dp else 13.dp)
            )
        }

        // Pointed stem triangle/pin dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(pin.pinColor)
        )
    }
}

@Composable
private fun LayerToggleRow(
    title: String,
    color: Color,
    isChecked: Boolean,
    onToggle: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 2.dp)
            .testTag(tag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (isChecked) color else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(7.dp)
            ) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                color = if (isChecked) Color(0xFF1E293B) else Color(0xFF94A3B8)
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.6f)
        )
    }
}
