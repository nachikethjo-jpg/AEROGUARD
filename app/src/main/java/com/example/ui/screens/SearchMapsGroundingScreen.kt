package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GroundedResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMapsGroundingScreen(
    onSearchGroundingQuery: suspend (String) -> GroundedResult,
    onMapsGroundingQuery: suspend (String) -> GroundedResult,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Search Grounding, 1: Maps Grounding
    var queryInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf<String?>(null) }
    var sourcesList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val searchPresets = listOf(
        "Latest air quality news and advisories in Durg",
        "Chhattisgarh Environment Conservation Board guidelines",
        "Recent penalties on open burning in Durg Bhilai",
        "Seasonal smog patterns in central India"
    )

    val mapsPresets = listOf(
        "Major pollution hotspots and industrial zones in Durg-Bhilai",
        "Air quality monitoring stations in Durg and Raipur",
        "Cleanest green zones and parks in Durg (e.g. Maitri Bagh)",
        "Traffic congestion and diesel emission corridors on NH-53"
    )

    fun executeQuery(query: String) {
        coroutineScope.launch {
            isLoading = true
            queryInput = query
            val res = if (selectedTab == 0) {
                onSearchGroundingQuery(query)
            } else {
                onMapsGroundingQuery(query)
            }
            resultText = res.text
            sourcesList = res.sources
            isLoading = false
        }
    }

    LaunchedEffect(selectedTab) {
        resultText = null
        sourcesList = emptyList()
        if (selectedTab == 0) {
            executeQuery(searchPresets.first())
        } else {
            executeQuery(mapsPresets.first())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedTab == 0) "Google Search Grounding" else "Google Maps Grounding",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Powered by gemini-3.5-flash with live tools",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("grounding_back_button")
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
            // Tab Selector: Search vs Maps
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Search Grounding", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Language, contentDescription = null) },
                    modifier = Modifier.testTag("tab_search_grounding")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Maps Grounding", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Place, contentDescription = null) },
                    modifier = Modifier.testTag("tab_maps_grounding")
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Search Input Field
                item {
                    OutlinedTextField(
                        value = queryInput,
                        onValueChange = { queryInput = it },
                        placeholder = {
                            Text(
                                if (selectedTab == 0) "Search live news & advisories..."
                                else "Explore pollution hotspots or green parks..."
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (queryInput.isNotBlank()) {
                                        executeQuery(queryInput)
                                    }
                                },
                                enabled = queryInput.isNotBlank() && !isLoading,
                                modifier = Modifier.testTag("execute_grounding_search_button")
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grounding_query_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Preset Pills
                item {
                    Text(
                        text = if (selectedTab == 0) "Suggested Live Search Topics:" else "Explore Map Locations & Hotspots:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val currentPresets = if (selectedTab == 0) searchPresets else mapsPresets
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        currentPresets.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isLoading) { executeQuery(preset) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.TravelExplore else Icons.Default.PinDrop,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = preset,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Loading Indicator
                if (isLoading) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (selectedTab == 0) "Fetching real-time Google Search data..."
                                    else "Grounding locations with Google Maps tool...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Results Card
                if (resultText != null && !isLoading) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grounding_result_card")
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
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedTab == 0) "Live Search Synthesis" else "Maps Spatial Analysis",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "Grounded",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = resultText ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp
                                )

                                if (sourcesList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Grounded Citations & Locations:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    sourcesList.forEach { src ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = src,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
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
}
