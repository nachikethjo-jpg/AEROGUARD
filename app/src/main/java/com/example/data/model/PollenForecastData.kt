package com.example.data.model

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

enum class PollenRiskLevel(
    val title: String,
    val color: Color,
    val guidance: String
) {
    LOW(
        title = "Low",
        color = Color(0xFF10B981),
        guidance = "Pollen counts are low. Safe for all sensitive individuals with minimal risk of allergic rhinitis."
    ),
    MODERATE(
        title = "Moderate",
        color = Color(0xFFF59E0B),
        guidance = "Only individuals with severe seasonal sensitivities or asthma may notice mild eye/nose irritation."
    ),
    HIGH(
        title = "High",
        color = Color(0xFFEA580C),
        guidance = "Active allergy symptoms expected. Wear sunglasses outside and keep antihistamines accessible."
    ),
    VERY_HIGH(
        title = "Very High",
        color = Color(0xFFDC2626),
        guidance = "Significant bio-aerosol load. Keep windows closed, wash face upon returning home, and use HEPA air purifiers."
    );

    companion object {
        fun fromIndex(index: Int): PollenRiskLevel = when {
            index <= 2 -> LOW
            index <= 5 -> MODERATE
            index <= 8 -> HIGH
            else -> VERY_HIGH
        }
    }
}

data class DayPollenForecast(
    val dayLabel: String,
    val dateLabel: String,
    val overallIndex: Int, // 0 to 12 scale
    val riskLevel: PollenRiskLevel = PollenRiskLevel.fromIndex(overallIndex),
    val dominantPollen: String,
    val treePollenPct: Int,  // 0 - 100
    val grassPollenPct: Int, // 0 - 100
    val weedPollenPct: Int,  // 0 - 100
    val moldSporesPct: Int,  // 0 - 100
    val advisory: String
)

data class PollenCityForecast(
    val cityName: String,
    val todayIndex: Int,
    val todayRisk: PollenRiskLevel,
    val dominantAllergen: String,
    val days: List<DayPollenForecast>,
    val sensitiveAdvice: String
)

object PollenMockService {

    fun getPollenForecast(cityName: String): PollenCityForecast {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val isDelhi = cityName.contains("Delhi", ignoreCase = true) || cityName.contains("Noida", ignoreCase = true) || cityName.contains("Gurugram", ignoreCase = true)
        val isBengaluru = cityName.contains("Bengaluru", ignoreCase = true) || cityName.contains("Mysuru", ignoreCase = true)
        val isCoastal = cityName.contains("Mumbai", ignoreCase = true) || cityName.contains("Chennai", ignoreCase = true) || cityName.contains("Kochi", ignoreCase = true)

        val baseIndices = when {
            isBengaluru -> listOf(8, 7, 9, 8, 6) // Flowering tree canopy
            isDelhi -> listOf(7, 8, 8, 7, 6)    // Parthenium weed & agricultural fringe
            isCoastal -> listOf(5, 6, 5, 4, 5)  // High mold spores & fungal moisture
            else -> listOf(4, 5, 6, 5, 4)       // Central / Durg / Raipur seasonal weed
        }

        val dominant = when {
            isBengaluru -> "Tree Pollen (Gulmohar & Acacia)"
            isDelhi -> "Weed Pollen (Parthenium & Amaranthus)"
            isCoastal -> "Mold Spores & Fungal Aerosols"
            else -> "Grass & Parthenium Weed"
        }

        val days = baseIndices.mapIndexed { index, idxVal ->
            val dayCalendar = (calendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, index)
            }
            val label = when (index) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> dayFormat.format(dayCalendar.time)
            }
            val dateStr = dateFormat.format(dayCalendar.time)

            val risk = PollenRiskLevel.fromIndex(idxVal)
            val tree = if (isBengaluru) (idxVal * 9).coerceIn(20, 95) else (idxVal * 6).coerceIn(10, 75)
            val grass = (idxVal * 7).coerceIn(15, 80)
            val weed = if (isDelhi) (idxVal * 10).coerceIn(30, 95) else (idxVal * 6).coerceIn(15, 70)
            val mold = if (isCoastal) (idxVal * 9).coerceIn(25, 90) else (idxVal * 4).coerceIn(10, 50)

            val dayAdvisory = when (risk) {
                PollenRiskLevel.VERY_HIGH -> "Elevated bio-aerosol surge! Wear wrap-around glasses and run indoor HEPA purifiers."
                PollenRiskLevel.HIGH -> "High allergen activity. Shower and change clothes after extended outdoor walks."
                PollenRiskLevel.MODERATE -> "Moderate pollen levels. Asthmatic users should keep rescue inhalers accessible."
                PollenRiskLevel.LOW -> "Low allergen risk. Favorable conditions for morning outdoor exercise."
            }

            DayPollenForecast(
                dayLabel = label,
                dateLabel = dateStr,
                overallIndex = idxVal,
                riskLevel = risk,
                dominantPollen = dominant,
                treePollenPct = tree,
                grassPollenPct = grass,
                weedPollenPct = weed,
                moldSporesPct = mold,
                advisory = dayAdvisory
            )
        }

        val today = days.first()
        val sensitiveAdvice = when {
            isBengaluru -> "Bengaluru's rich urban tree canopy produces periodic tree pollen peaks. Sensitive individuals should avoid early morning jogging in dense parks."
            isDelhi -> "Parthenium weed and dust-bound bio-aerosols exacerbate urban smog in NCR. Use saline nasal sprays and avoid cycling near unpaved roads."
            isCoastal -> "Coastal humidity accelerates fungal mold spore propagation. Maintain indoor room humidity under 55% with air conditioning or dehumidifiers."
            else -> "Seasonal grass and roadside Parthenium weeds produce moderate morning pollen dispersal in Central India. Keep vehicle windows closed during highway travel."
        }

        return PollenCityForecast(
            cityName = cityName,
            todayIndex = today.overallIndex,
            todayRisk = today.riskLevel,
            dominantAllergen = dominant,
            days = days,
            sensitiveAdvice = sensitiveAdvice
        )
    }
}
