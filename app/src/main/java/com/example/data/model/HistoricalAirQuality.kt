package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class HistoryTimeRange(val label: String, val shortLabel: String, val pointCountDescription: String) {
    WEEK("Past Week", "7D", "Daily 24h Average"),
    MONTH("Past Month", "30D", "30-Day Trend"),
    YEAR("Past Year", "1Y", "12-Month Seasonal Trend")
}

enum class HistoryMetric(
    val key: String,
    val displayName: String,
    val unit: String,
    val safeThreshold: Float,
    val standardName: String
) {
    AQI("aqi", "AQI", "", 100f, "CPCB Satisfactory Threshold (100)"),
    PM25("pm25", "PM2.5", "µg/m³", 60f, "NAAQS 24-hr Standard (60 µg/m³)"),
    PM10("pm10", "PM10", "µg/m³", 100f, "NAAQS 24-hr Standard (100 µg/m³)"),
    NO2("no2", "NO₂", "µg/m³", 80f, "NAAQS Standard (80 µg/m³)"),
    SO2("so2", "SO₂", "µg/m³", 80f, "NAAQS Standard (80 µg/m³)"),
    CO("co", "CO", "mg/m³", 2.0f, "NAAQS 8-hr Standard (2.0 mg/m³)"),
    O3("o3", "O₃", "µg/m³", 100f, "NAAQS 8-hr Standard (100 µg/m³)")
}

data class HistoricalPoint(
    val label: String,
    val fullDate: String,
    val value: Float,
    val category: String,
    val color: Color,
    val eventNote: String? = null
)

data class TrendHighlight(
    val title: String,
    val changeText: String,
    val isWorsening: Boolean,
    val period: String,
    val description: String
)

data class HistoricalDataSet(
    val timeRange: HistoryTimeRange,
    val metric: HistoryMetric,
    val points: List<HistoricalPoint>,
    val avgValue: Float,
    val peakValue: Float,
    val peakDate: String,
    val minValue: Float,
    val minDate: String,
    val netChangePercent: Float,
    val safeThreshold: Float,
    val highlights: List<TrendHighlight>,
    val expertSummary: String
)

object HistoricalDataRepository {

    fun getHistoricalData(cityName: String, range: HistoryTimeRange, metric: HistoryMetric): HistoricalDataSet {
        val baseMultiplier = when {
            cityName.contains("Delhi", ignoreCase = true) -> 1.45f
            cityName.contains("Bhilai", ignoreCase = true) || cityName.contains("Durg", ignoreCase = true) -> 1.15f
            cityName.contains("Patna", ignoreCase = true) || cityName.contains("Kanpur", ignoreCase = true) -> 1.35f
            cityName.contains("Bengaluru", ignoreCase = true) || cityName.contains("Cochin", ignoreCase = true) -> 0.70f
            cityName.contains("Mumbai", ignoreCase = true) -> 0.95f
            else -> 1.0f
        }

        return when (range) {
            HistoryTimeRange.WEEK -> generateWeekData(baseMultiplier, metric)
            HistoryTimeRange.MONTH -> generateMonthData(baseMultiplier, metric)
            HistoryTimeRange.YEAR -> generateYearData(baseMultiplier, metric)
        }
    }

    private fun generateWeekData(multiplier: Float, metric: HistoryMetric): HistoricalDataSet {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val fullDates = listOf("Oct 14", "Oct 15", "Oct 16", "Oct 17", "Oct 18", "Oct 19", "Oct 20")

        // Relative baseline weights for last 7 days (midweek industrial shift peak + weekend respite)
        val relativeWeights = listOf(0.92f, 1.05f, 1.28f, 1.35f, 1.15f, 0.88f, 0.82f)

        val baseVal = when (metric) {
            HistoryMetric.AQI -> 138f * multiplier
            HistoryMetric.PM25 -> 74f * multiplier
            HistoryMetric.PM10 -> 142f * multiplier
            HistoryMetric.NO2 -> 48f * multiplier
            HistoryMetric.SO2 -> 34f * multiplier
            HistoryMetric.CO -> 1.4f * multiplier
            HistoryMetric.O3 -> 52f * multiplier
        }

        val points = days.mapIndexed { idx, day ->
            val v = (baseVal * relativeWeights[idx]).coerceAtLeast(10f)
            val event = when (idx) {
                3 -> "Industrial Peak Shift (+28%)"
                6 -> "Sunday Traffic Reduction (-24%)"
                else -> null
            }
            HistoricalPoint(
                label = day,
                fullDate = fullDates[idx],
                value = kotlin.math.round(v * 10f) / 10f,
                category = getCategoryForValue(v, metric),
                color = getColorForValue(v, metric),
                eventNote = event
            )
        }

        val values = points.map { it.value }
        val avg = values.average().toFloat()
        val peak = values.maxOrNull() ?: 0f
        val peakPoint = points.firstOrNull { it.value == peak }
        val min = values.minOrNull() ?: 0f
        val minPoint = points.firstOrNull { it.value == min }
        val netChange = ((values.last() - values.first()) / values.first()) * 100f

        val highlights = listOf(
            TrendHighlight(
                title = "Midweek Industrial Plume",
                changeText = "+35% Surge",
                isWorsening = true,
                period = "Wed - Thu",
                description = "Intensive metallurgical casting cycles and calm wind conditions accelerated particulate accumulation."
            ),
            TrendHighlight(
                title = "Weekend Dispersal",
                changeText = "-24% Drop",
                isWorsening = false,
                period = "Sat - Sun",
                description = "Reduced commercial heavy-duty diesel transit along NH-53 lowered regional emissions."
            )
        )

        return HistoricalDataSet(
            timeRange = HistoryTimeRange.WEEK,
            metric = metric,
            points = points,
            avgValue = kotlin.math.round(avg * 10f) / 10f,
            peakValue = peak,
            peakDate = "${peakPoint?.fullDate} (${peakPoint?.label})",
            minValue = min,
            minDate = "${minPoint?.fullDate} (${minPoint?.label})",
            netChangePercent = kotlin.math.round(netChange * 10f) / 10f,
            safeThreshold = metric.safeThreshold,
            highlights = highlights,
            expertSummary = "Air pollution exhibited a marked mid-week surge driven by localized industrial shift operations and calm surface winds, followed by a 24% clearance over the weekend."
        )
    }

    private fun generateMonthData(multiplier: Float, metric: HistoryMetric): HistoricalDataSet {
        // 30 days divided into 10 intervals
        val labels = listOf("Day 1", "Day 4", "Day 7", "Day 10", "Day 13", "Day 16", "Day 19", "Day 22", "Day 25", "Day 28", "Day 30")
        val fullDates = listOf("Sep 21", "Sep 24", "Sep 27", "Sep 30", "Oct 3", "Oct 6", "Oct 9", "Oct 12", "Oct 15", "Oct 18", "Oct 20")
        val relativeWeights = listOf(0.78f, 0.82f, 0.74f, 0.89f, 1.12f, 1.38f, 1.45f, 1.25f, 1.18f, 1.05f, 0.94f)

        val baseVal = when (metric) {
            HistoryMetric.AQI -> 132f * multiplier
            HistoryMetric.PM25 -> 70f * multiplier
            HistoryMetric.PM10 -> 135f * multiplier
            HistoryMetric.NO2 -> 45f * multiplier
            HistoryMetric.SO2 -> 32f * multiplier
            HistoryMetric.CO -> 1.3f * multiplier
            HistoryMetric.O3 -> 50f * multiplier
        }

        val points = labels.mapIndexed { idx, label ->
            val v = (baseVal * relativeWeights[idx]).coerceAtLeast(10f)
            val event = when (idx) {
                2 -> "Late Monsoon Scrubbing (-26%)"
                6 -> "Pre-Harvest Stubble & Firework Surge (+52%)"
                else -> null
            }
            HistoricalPoint(
                label = label,
                fullDate = fullDates[idx],
                value = kotlin.math.round(v * 10f) / 10f,
                category = getCategoryForValue(v, metric),
                color = getColorForValue(v, metric),
                eventNote = event
            )
        }

        val values = points.map { it.value }
        val avg = values.average().toFloat()
        val peak = values.maxOrNull() ?: 0f
        val peakPoint = points.firstOrNull { it.value == peak }
        val min = values.minOrNull() ?: 0f
        val minPoint = points.firstOrNull { it.value == min }
        val netChange = ((values.last() - values.first()) / values.first()) * 100f

        val highlights = listOf(
            TrendHighlight(
                title = "Seasonal Transition Spike",
                changeText = "+52% Spike",
                isWorsening = true,
                period = "Oct 6 - Oct 12",
                description = "Onset of post-monsoon dry air and lower boundary layer depth trapped vehicular and stubble smoke."
            ),
            TrendHighlight(
                title = "Monsoon Retreat Rainfall",
                changeText = "-26% Clearance",
                isWorsening = false,
                period = "Late September",
                description = "Precipitation scavenging effectively settled coarse PM10 and ambient particulate concentrations."
            )
        )

        return HistoricalDataSet(
            timeRange = HistoryTimeRange.MONTH,
            metric = metric,
            points = points,
            avgValue = kotlin.math.round(avg * 10f) / 10f,
            peakValue = peak,
            peakDate = "${peakPoint?.fullDate} (${peakPoint?.label})",
            minValue = min,
            minDate = "${minPoint?.fullDate} (${minPoint?.label})",
            netChangePercent = kotlin.math.round(netChange * 10f) / 10f,
            safeThreshold = metric.safeThreshold,
            highlights = highlights,
            expertSummary = "Over the past 30 days, air quality transitioned from late-monsoon cleanliness into the autumn inversion phase, exhibiting a notable +52% particulate increase."
        )
    }

    private fun generateYearData(multiplier: Float, metric: HistoryMetric): HistoricalDataSet {
        val months = listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct")
        val fullDates = listOf("Nov 2024", "Dec 2024", "Jan 2025", "Feb 2025", "Mar 2025", "Apr 2025", "May 2025", "Jun 2025", "Jul 2025", "Aug 2025", "Sep 2025", "Oct 2025")
        
        // India 12-month meteorological curve: Winter inversion peak (Nov-Jan), Spring moderate (Feb-Apr), Pre-monsoon dust (May), Monsoon washout low (Jul-Aug), Autumn rise (Oct)
        val relativeWeights = listOf(1.75f, 1.85f, 1.65f, 1.25f, 1.05f, 1.15f, 1.20f, 0.75f, 0.48f, 0.44f, 0.62f, 1.12f)

        val baseVal = when (metric) {
            HistoryMetric.AQI -> 125f * multiplier
            HistoryMetric.PM25 -> 65f * multiplier
            HistoryMetric.PM10 -> 128f * multiplier
            HistoryMetric.NO2 -> 42f * multiplier
            HistoryMetric.SO2 -> 30f * multiplier
            HistoryMetric.CO -> 1.2f * multiplier
            HistoryMetric.O3 -> 46f * multiplier
        }

        val points = months.mapIndexed { idx, m ->
            val v = (baseVal * relativeWeights[idx]).coerceAtLeast(10f)
            val event = when (idx) {
                1 -> "Winter Thermal Inversion Peak (+85%)"
                6 -> "Pre-Monsoon Dust Storm (+20%)"
                9 -> "Southwest Monsoon Washout (-56%)"
                else -> null
            }
            HistoricalPoint(
                label = m,
                fullDate = fullDates[idx],
                value = kotlin.math.round(v * 10f) / 10f,
                category = getCategoryForValue(v, metric),
                color = getColorForValue(v, metric),
                eventNote = event
            )
        }

        val values = points.map { it.value }
        val avg = values.average().toFloat()
        val peak = values.maxOrNull() ?: 0f
        val peakPoint = points.firstOrNull { it.value == peak }
        val min = values.minOrNull() ?: 0f
        val minPoint = points.firstOrNull { it.value == min }
        val netChange = ((values.last() - values.first()) / values.first()) * 100f

        val highlights = listOf(
            TrendHighlight(
                title = "Monsoon Atmospheric Scrubbing",
                changeText = "-56% Annual Low",
                isWorsening = false,
                period = "July - August",
                description = "Consistent rainfall and turbulent convective mixing cleansed ambient PM2.5 and PM10 to yearly lows."
            ),
            TrendHighlight(
                title = "Winter Nocturnal Inversion",
                changeText = "+85% Extreme Peak",
                isWorsening = true,
                period = "November - December",
                description = "Severe radiation inversion locked cold ground air and prevented industrial plume dispersion."
            ),
            TrendHighlight(
                title = "Pre-Monsoon Convective Dust",
                changeText = "+20% Coarse PM Rise",
                isWorsening = true,
                period = "April - May",
                description = "High surface temperatures generated thermal winds carrying loose crustal dust across highway corridors."
            )
        )

        return HistoricalDataSet(
            timeRange = HistoryTimeRange.YEAR,
            metric = metric,
            points = points,
            avgValue = kotlin.math.round(avg * 10f) / 10f,
            peakValue = peak,
            peakDate = "${peakPoint?.fullDate}",
            minValue = min,
            minDate = "${minPoint?.fullDate}",
            netChangePercent = kotlin.math.round(netChange * 10f) / 10f,
            safeThreshold = metric.safeThreshold,
            highlights = highlights,
            expertSummary = "The annual cycle reflects classic Indian seasonality: intense winter inversion peaks (Nov-Jan), dust and photochemical ozone elevated in pre-monsoon (May), and pristine wet-scavenged air during the monsoon (Jul-Aug)."
        )
    }

    private fun getCategoryForValue(value: Float, metric: HistoryMetric): String {
        return when (metric) {
            HistoryMetric.AQI -> when {
                value <= 50 -> "Good"
                value <= 100 -> "Satisfactory"
                value <= 150 -> "Moderate"
                value <= 200 -> "Poor"
                value <= 300 -> "Severe"
                else -> "Hazardous"
            }
            HistoryMetric.PM25 -> when {
                value <= 30 -> "Good"
                value <= 60 -> "Satisfactory"
                value <= 90 -> "Moderate"
                value <= 120 -> "Poor"
                value <= 250 -> "Severe"
                else -> "Hazardous"
            }
            HistoryMetric.PM10 -> when {
                value <= 50 -> "Good"
                value <= 100 -> "Satisfactory"
                value <= 150 -> "Moderate"
                value <= 250 -> "Poor"
                value <= 350 -> "Severe"
                else -> "Hazardous"
            }
            else -> if (value <= metric.safeThreshold) "Acceptable" else "Elevated"
        }
    }

    private fun getColorForValue(value: Float, metric: HistoryMetric): Color {
        val normalized = value / metric.safeThreshold
        return when {
            normalized <= 0.6f -> AqiGood
            normalized <= 1.0f -> AqiSatisfactory
            normalized <= 1.5f -> AqiModerate
            normalized <= 2.2f -> AqiPoor
            normalized <= 3.0f -> AqiSevere
            else -> AqiHazardous
        }
    }
}
