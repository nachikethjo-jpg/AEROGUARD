package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.model.IndiaLocations

@Database(
    entities = [PollutionReportEntity::class, SavedLocationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AeroGuardDatabase : RoomDatabase() {
    abstract fun pollutionReportDao(): PollutionReportDao
    abstract fun savedLocationDao(): SavedLocationDao

    companion object {
        @Volatile
        private var INSTANCE: AeroGuardDatabase? = null

        fun getInstance(context: Context): AeroGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AeroGuardDatabase::class.java,
                    "aeroguard_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AeroGuardDatabase) {
            val reportDao = db.pollutionReportDao()
            val now = System.currentTimeMillis()

            reportDao.insertReport(
                PollutionReportEntity(
                    sourceType = "Open Garbage/Crop Burning",
                    location = "Near Padmanabhpur Ring Road Bypass",
                    description = "Heavy pile of dried leaves and municipal waste set ablaze. Thick white smoke blowing towards residential apartments.",
                    severity = "High",
                    timestamp = now - (45 * 60 * 1000), // 45m ago
                    upvotes = 14,
                    status = "Verified",
                    authorName = "Aman K. (Padmanabhpur)"
                )
            )

            reportDao.insertReport(
                PollutionReportEntity(
                    sourceType = "Industrial Emission/Smoke",
                    location = "G.E. Road near Bhilai-Durg Border",
                    description = "Visible dark industrial plume blowing southwards. Noticeable sulfur smell in the air around 6 PM shift change.",
                    severity = "High",
                    timestamp = now - (3 * 3600 * 1000), // 3h ago
                    upvotes = 29,
                    status = "Under Review",
                    authorName = "Pooja S. (Sector 1)"
                )
            )

            reportDao.insertReport(
                PollutionReportEntity(
                    sourceType = "Construction Dust",
                    location = "Mohan Nagar Overbridge Construction",
                    description = "Uncovered cement and sand piles without dust water suppression screens. Heavy particulate dispersion with every passing truck.",
                    severity = "Moderate",
                    timestamp = now - (6 * 3600 * 1000), // 6h ago
                    upvotes = 8,
                    status = "Action Taken",
                    authorName = "Ramesh Verma"
                )
            )

            val locationDao = db.savedLocationDao()
            val panIndiaLocations = IndiaLocations.allCities.mapIndexed { index, city ->
                SavedLocationEntity(
                    id = city.id,
                    cityName = city.cityName,
                    state = city.state,
                    subText = "${city.subText} • ${city.zone} Zone",
                    baseAqi = city.baseAqi,
                    isSelected = index == 0
                )
            }
            locationDao.insertLocations(panIndiaLocations)
        }
    }
}
