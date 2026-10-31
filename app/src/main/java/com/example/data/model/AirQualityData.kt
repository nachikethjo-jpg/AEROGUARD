package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class AqiCategory(
    val title: String,
    val range: IntRange,
    val color: Color,
    val advisory: String,
    val maskAdvice: String,
    val outdoorScore: String
) {
    GOOD(
        title = "Good",
        range = 0..50,
        color = AqiGood,
        advisory = "Air quality is ideal for all outdoor activities. Enjoy the fresh air!",
        maskAdvice = "No mask needed for healthy individuals.",
        outdoorScore = "10/10 Excellent"
    ),
    SATISFACTORY(
        title = "Satisfactory",
        range = 51..100,
        color = AqiSatisfactory,
        advisory = "Acceptable air quality. Unusually sensitive individuals should monitor any cough or irritation.",
        maskAdvice = "Optional for sensitive individuals.",
        outdoorScore = "8/10 Great"
    ),
    MODERATE(
        title = "Moderate / Poor",
        range = 101..150,
        color = AqiModerate,
        advisory = "Sensitive individuals should consider limiting prolonged outdoor exertion.",
        maskAdvice = "Recommended for children, elderly & asthmatics.",
        outdoorScore = "5/10 Caution"
    ),
    POOR(
        title = "Poor / Unhealthy",
        range = 151..200,
        color = AqiPoor,
        advisory = "Increased likelihood of respiratory symptoms in general population. Avoid vigorous outdoor workouts.",
        maskAdvice = "Wear N95/KN95 outdoors.",
        outdoorScore = "3/10 Poor"
    ),
    SEVERE(
        title = "Very Poor / Severe",
        range = 201..300,
        color = AqiSevere,
        advisory = "Health alert: Serious risk of respiratory & cardiovascular stress. Keep windows shut and run HEPA purifiers.",
        maskAdvice = "Strictly wear well-fitting N95 mask.",
        outdoorScore = "1/10 Dangerous"
    ),
    HAZARDOUS(
        title = "Hazardous",
        range = 301..500,
        color = AqiHazardous,
        advisory = "Emergency health conditions. Entire population is prone to respiratory injury. Stay indoors!",
        maskAdvice = "Emergency respirator / N95 essential outdoors.",
        outdoorScore = "0/10 Prohibited"
    );

    companion object {
        fun fromAqi(aqi: Int): AqiCategory {
            return when {
                aqi <= 50 -> GOOD
                aqi <= 100 -> SATISFACTORY
                aqi <= 150 -> MODERATE
                aqi <= 200 -> POOR
                aqi <= 300 -> SEVERE
                else -> HAZARDOUS
            }
        }
    }
}

data class Pollutant(
    val name: String,
    val fullName: String,
    val value: Double,
    val unit: String = "µg/m³",
    val safeLimit: Double,
    val percentage: Float, // 0..1 for bar
    val description: String,
    val sourceFactors: String,
    val color: Color
)

data class HourlyForecast(
    val hourLabel: String,
    val predictedAqi: Int,
    val pm25: Double,
    val pm10: Double,
    val note: String
)

data class WeatherInfo(
    val tempC: Double = 31.0,
    val humidityPct: Int = 48,
    val windKmh: Double = 12.5,
    val windDir: String = "NW",
    val pressureHpa: Int = 1012,
    val uvIndex: Int = 6
)

data class AirQualityData(
    val locationName: String = "Durg, Chhattisgarh",
    val subLocality: String = "Padmanabhpur & G.E. Road Corridor",
    val stationName: String = "CECB Regional Monitoring Station, Durg",
    val aqi: Int = 142,
    val category: AqiCategory = AqiCategory.fromAqi(142),
    val pollutants: List<Pollutant> = defaultPollutants(),
    val forecast: List<HourlyForecast> = defaultForecast(),
    val weather: WeatherInfo = WeatherInfo(),
    val lastUpdated: String = "Just now"
)

fun defaultPollutants(): List<Pollutant> = listOf(
    Pollutant(
        name = "PM2.5",
        fullName = "Fine Particulate Matter (< 2.5 µm)",
        value = 54.2,
        safeLimit = 60.0,
        percentage = 0.65f,
        description = "Fine particles from vehicular exhaust, open burning, and industrial smoke that penetrate deep into lungs.",
        sourceFactors = "Heavy traffic along G.E. Road, crop residue burning in rural fringe, and steel industrial flux.",
        color = AqiModerate
    ),
    Pollutant(
        name = "PM10",
        fullName = "Coarse Particulate Matter (< 10 µm)",
        value = 112.0,
        safeLimit = 100.0,
        percentage = 0.56f,
        description = "Coarse dust particles originating from construction, road dust, and mechanical operations.",
        sourceFactors = "Flyover construction work, unpaved shoulders, and truck movement.",
        color = AqiSatisfactory
    ),
    Pollutant(
        name = "SO₂",
        fullName = "Sulfur Dioxide",
        value = 12.4,
        safeLimit = 80.0,
        percentage = 0.20f,
        description = "Pungent gas produced by burning fossil fuels in power plants and metal smelting.",
        sourceFactors = "Bhilai industrial complex and nearby thermal processing units.",
        color = AqiGood
    ),
    Pollutant(
        name = "NO₂",
        fullName = "Nitrogen Dioxide",
        value = 28.1,
        safeLimit = 80.0,
        percentage = 0.35f,
        description = "Toxic gas emitted by vehicles, buses, and diesel generators.",
        sourceFactors = "National Highway 53 congestion and diesel auto-rickshaws.",
        color = AqiGood
    ),
    Pollutant(
        name = "CO",
        fullName = "Carbon Monoxide",
        value = 1.2,
        unit = "mg/m³",
        safeLimit = 2.0,
        percentage = 0.40f,
        description = "Colorless, odorless gas from incomplete combustion in vehicle engines and domestic biomass burning.",
        sourceFactors = "Stop-and-go traffic and street cooking fires.",
        color = AqiGood
    ),
    Pollutant(
        name = "O₃",
        fullName = "Ground-level Ozone",
        value = 34.0,
        safeLimit = 100.0,
        percentage = 0.34f,
        description = "Secondary pollutant formed when sunlight reacts with NOx and volatile organic compounds.",
        sourceFactors = "Intense afternoon solar radiation in central India.",
        color = AqiGood
    )
)

fun defaultForecast(): List<HourlyForecast> {
    val items = mutableListOf<HourlyForecast>()
    val hours = listOf(
        "Now" to 142, "1 PM" to 138, "2 PM" to 135, "3 PM" to 130,
        "4 PM" to 134, "5 PM" to 146, "6 PM" to 162, "7 PM" to 175,
        "8 PM" to 184, "9 PM" to 178, "10 PM" to 165, "11 PM" to 152,
        "12 AM" to 138, "1 AM" to 125, "2 AM" to 118, "3 AM" to 112,
        "4 AM" to 108, "5 AM" to 115, "6 AM" to 128, "7 AM" to 148,
        "8 AM" to 164, "9 AM" to 158, "10 AM" to 145, "11 AM" to 140
    )
    for ((time, aqi) in hours) {
        val note = when {
            aqi > 170 -> "Evening peak: Cooking biomass & evening industrial shifts"
            aqi > 150 -> "Morning rush hour vehicular emission surge"
            aqi < 120 -> "Nighttime dispersion & lower ground activity"
            else -> "Steady baseline pollution"
        }
        items.add(
            HourlyForecast(
                hourLabel = time,
                predictedAqi = aqi,
                pm25 = (aqi * 0.38).coerceAtLeast(10.0),
                pm10 = (aqi * 0.78).coerceAtLeast(20.0),
                note = note
            )
        )
    }
    return items
}
