package com.example.data.repository

import android.content.Context
import com.example.data.local.AeroGuardDatabase
import com.example.data.local.PollutionReportEntity
import com.example.data.local.SavedLocationEntity
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.data.remote.ChatMessage
import com.example.data.remote.FirebaseService
import com.example.data.remote.GeminiService
import com.example.data.remote.GeneratedImageResult
import com.example.data.remote.GroundedResult
import com.example.data.remote.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class AeroGuardRepository(context: Context) {

    private val db = AeroGuardDatabase.getInstance(context)
    private val reportDao = db.pollutionReportDao()
    private val locationDao = db.savedLocationDao()
    val gemini = GeminiService()
    val firebase = FirebaseService(context)

    // Current AQI State
    private val _currentAirQuality = MutableStateFlow(AirQualityData())
    val currentAirQuality: StateFlow<AirQualityData> = _currentAirQuality.asStateFlow()

    // Current User Profile
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Chatbot message history
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "Welcome to AeroGuard! I am AeroBot, your Indian air quality and environmental health companion covering all states, union territories, and National Clean Air Programme (NCAP) cities across India. Ask me about CPCB standards, GRAP stages in Delhi-NCR, stubble burning in Punjab/Haryana, industrial emissions in Bhilai/Durg, coastal humidity in Mumbai/Chennai, or personalized health advisories."
            )
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    init {
        _currentUser.value = firebase.getCurrentUser() ?: UserProfile(
            uid = "user_guest_durg",
            displayName = "Nachiketh (Durg)",
            email = "nachiketh.JO@gmail.com",
            isAnonymous = false
        )
    }

    fun getAllReports(): Flow<List<PollutionReportEntity>> = reportDao.getAllReports()

    fun getAllSavedLocations(): Flow<List<SavedLocationEntity>> = locationDao.getAllLocations()

    suspend fun submitReport(
        sourceType: String,
        location: String,
        description: String,
        severity: String,
        imageUri: String? = null
    ): Long {
        val user = _currentUser.value
        val report = PollutionReportEntity(
            sourceType = sourceType,
            location = location,
            description = description,
            severity = severity,
            timestamp = System.currentTimeMillis(),
            upvotes = 1,
            status = "Under Review",
            imageUri = imageUri,
            authorName = user?.displayName ?: "Local Citizen"
        )
        val id = reportDao.insertReport(report)
        val createdReport = report.copy(id = id)
        firebase.syncReportToFirestore(createdReport)
        return id
    }

    suspend fun upvoteReport(reportId: Long) {
        reportDao.upvoteReport(reportId)
        firebase.upvoteReportInFirestore(reportId)
    }

    suspend fun switchLocation(location: SavedLocationEntity) {
        locationDao.setSelectedLocation(location.id)
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val updatedTime = timeFormat.format(Date())

        val aqi = location.baseAqi
        val category = AqiCategory.fromAqi(aqi)

        // Adjust pollutants based on location baseline
        val factor = aqi / 142.0
        val updatedPollutants = defaultPollutants().map { p ->
            val newVal = (p.value * factor * 10).toInt() / 10.0
            val pct = (newVal / (p.safeLimit * 1.5)).coerceIn(0.05, 1.0).toFloat()
            p.copy(
                value = newVal,
                percentage = pct,
                color = when {
                    pct > 0.8 -> AqiPoor
                    pct > 0.5 -> AqiModerate
                    pct > 0.3 -> AqiSatisfactory
                    else -> AqiGood
                }
            )
        }

        val updatedForecast = defaultForecast().map { f ->
            val newAqi = (f.predictedAqi * factor).toInt()
            f.copy(
                predictedAqi = newAqi,
                pm25 = (newAqi * 0.38).coerceAtLeast(10.0),
                pm10 = (newAqi * 0.78).coerceAtLeast(20.0)
            )
        }

        val cityProfile = IndiaLocations.getCityById(location.id) ?: IndiaLocations.findCityByName(location.cityName)

        _currentAirQuality.value = AirQualityData(
            locationName = "${location.cityName}, ${location.state}",
            subLocality = location.subText,
            stationName = cityProfile.stationName,
            aqi = aqi,
            category = category,
            pollutants = updatedPollutants,
            forecast = updatedForecast,
            weather = WeatherInfo(
                tempC = when (cityProfile.zone) {
                    "North" -> 26.5
                    "South" -> 30.0
                    "West" -> 32.0
                    "East" -> 29.5
                    else -> 31.0
                },
                humidityPct = when (cityProfile.zone) {
                    "West", "South" -> 68
                    "North" -> 44
                    "East" -> 62
                    else -> 48
                }
            ),
            lastUpdated = updatedTime
        )
    }

    fun refreshData() {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val updatedTime = timeFormat.format(Date())
        _currentAirQuality.value = _currentAirQuality.value.copy(
            lastUpdated = updatedTime
        )
    }

    suspend fun sendChatMessage(userText: String): ChatMessage {
        val currentHistory = _chatHistory.value
        val userMsg = ChatMessage(role = "user", text = userText)
        val updatedHistory = currentHistory + userMsg
        _chatHistory.value = updatedHistory

        val contextStr = "AQI ${_currentAirQuality.value.aqi} (${_currentAirQuality.value.category.title}) in ${_currentAirQuality.value.locationName}"
        val response = gemini.chat(
            history = updatedHistory,
            userMessage = userText,
            currentAqiContext = contextStr
        )

        _chatHistory.value = updatedHistory + response
        return response
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage(
                role = "model",
                text = "Chat cleared. I'm ready for any new questions regarding Durg's environmental conditions, health alerts, or local pollution sources!"
            )
        )
    }

    suspend fun getSearchGroundedInfo(query: String): GroundedResult {
        return gemini.queryWithSearchGrounding(query)
    }

    suspend fun getMapsGroundedInfo(query: String): GroundedResult {
        return gemini.queryWithMapsGrounding(query)
    }

    suspend fun generateImageVisualization(
        prompt: String,
        aspectRatio: String,
        imageSize: String
    ): GeneratedImageResult {
        return gemini.generateImage(
            prompt = prompt,
            aspectRatio = aspectRatio,
            imageSize = imageSize
        )
    }

    suspend fun getInstantHealthAdvice(userProfileType: String): String {
        val current = _currentAirQuality.value
        val prompt = "Provide rapid 2-bullet air quality advice for a person with profile: '$userProfileType'. " +
                "Current city: ${current.locationName}. AQI: ${current.aqi} (${current.category.title}), PM2.5: ${current.pollutants.firstOrNull { it.name == "PM2.5" }?.value} µg/m³. " +
                "Be extremely clear and clinically grounded."
        return gemini.generateLowLatency(prompt)
    }

    suspend fun signInDemoUser(email: String, name: String) {
        val result = firebase.signInWithDemoGoogleAccount(email, name)
        result.onSuccess {
            _currentUser.value = it
        }
    }

    fun signOut() {
        firebase.signOut()
        _currentUser.value = UserProfile(
            uid = "guest_${System.currentTimeMillis()}",
            displayName = "Guest Citizen",
            email = "guest@aeroguard.app",
            isAnonymous = true
        )
    }
}
