package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pollution_reports")
data class PollutionReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceType: String,
    val location: String,
    val description: String,
    val severity: String = "Moderate",
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = 1,
    val status: String = "Under Review",
    val imageUri: String? = null,
    val authorName: String = "Local Citizen"
)

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey
    val id: String,
    val cityName: String,
    val state: String,
    val subText: String,
    val baseAqi: Int,
    val isSelected: Boolean = false
)
