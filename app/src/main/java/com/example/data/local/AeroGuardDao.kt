package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PollutionReportDao {
    @Query("SELECT * FROM pollution_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<PollutionReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: PollutionReportEntity): Long

    @Query("UPDATE pollution_reports SET upvotes = upvotes + 1 WHERE id = :reportId")
    suspend fun upvoteReport(reportId: Long)

    @Query("UPDATE pollution_reports SET status = :newStatus WHERE id = :reportId")
    suspend fun updateStatus(reportId: Long, newStatus: String)

    @Delete
    suspend fun deleteReport(report: PollutionReportEntity)
}

@Dao
interface SavedLocationDao {
    @Query("SELECT * FROM saved_locations")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<SavedLocationEntity>)

    @Query("UPDATE saved_locations SET isSelected = (id = :selectedId)")
    suspend fun setSelectedLocation(selectedId: String)
}
