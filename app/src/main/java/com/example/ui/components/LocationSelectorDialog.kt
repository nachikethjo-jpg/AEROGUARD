package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedLocationEntity
import com.example.data.model.AqiCategory
import com.example.data.model.IndiaLocations

@Composable
fun LocationSelectorDialog(
    locations: List<SavedLocationEntity>,
    currentLocationName: String,
    onSelectLocation: (SavedLocationEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedZoneFilter by remember { mutableStateOf("All") }

    // Merge passed database locations with fallback from IndiaLocations if needed
    val combinedLocations = remember(locations) {
        if (locations.isNotEmpty() && locations.size > 10) {
            locations
        } else {
            IndiaLocations.allCities.mapIndexed { index, city ->
                SavedLocationEntity(
                    id = city.id,
                    cityName = city.cityName,
                    state = city.state,
                    subText = "${city.subText} • ${city.zone} Zone",
                    baseAqi = city.baseAqi,
                    isSelected = index == 0
                )
            }
        }
    }

    val zones = listOf("All", "North", "West", "South", "East", "Central", "North-East")

    val filteredLocations = remember(searchQuery, selectedZoneFilter, combinedLocations) {
        combinedLocations.filter { loc ->
            val matchesSearch = searchQuery.isBlank() ||
                    loc.cityName.contains(searchQuery, ignoreCase = true) ||
                    loc.state.contains(searchQuery, ignoreCase = true) ||
                    loc.subText.contains(searchQuery, ignoreCase = true)

            val matchesZone = if (selectedZoneFilter == "All") {
                true
            } else {
                val cityProfile = IndiaLocations.getCityById(loc.id)
                cityProfile?.zone.equals(selectedZoneFilter, ignoreCase = true) ||
                        loc.subText.contains(selectedZoneFilter, ignoreCase = true)
            }

            matchesSearch && matchesZone
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Pan-India City Monitor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("National Clean Air Programme (NCAP)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city, state or hub...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("city_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Zone Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    zones.forEach { zone ->
                        val isSelected = selectedZoneFilter == zone
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedZoneFilter = zone },
                            label = { Text(zone, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Counter label
                Text(
                    text = "${filteredLocations.size} Indian cities available",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // City List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .testTag("location_picker_list")
                ) {
                    if (filteredLocations.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No cities found matching '$searchQuery'", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(filteredLocations) { loc ->
                            val isSelected = currentLocationName.contains(loc.cityName, ignoreCase = true)
                            val category = AqiCategory.fromAqi(loc.baseAqi)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        onSelectLocation(loc)
                                        onDismiss()
                                    }
                                    .testTag("city_item_${loc.cityName.lowercase().replace(" ", "_")}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        MeteoconIcon(
                                            name = getAqiMeteoconName(loc.baseAqi),
                                            size = 28.dp,
                                            contentDescription = "AQI ${loc.baseAqi}"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "${loc.cityName}, ${loc.state}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = loc.subText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = category.color.copy(alpha = 0.2f),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Text(
                                                text = "AQI ${loc.baseAqi}",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = category.color,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
