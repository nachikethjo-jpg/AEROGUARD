package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PollutionReportEntity
import com.example.data.local.SavedLocationEntity
import com.example.data.model.AirQualityData
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeneratedImageResult
import com.example.data.remote.GroundedResult
import com.example.data.remote.UserProfile
import com.example.data.repository.AeroGuardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    REPORT,
    HEALTH,
    CHAT,
    GROUNDING,
    VISUALIZER,
    WEB_VIEW
}

class AeroGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AeroGuardRepository(application)

    val currentAirQuality: StateFlow<AirQualityData> = repository.currentAirQuality
    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatHistory

    val reports: StateFlow<List<PollutionReportEntity>> = repository.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations: StateFlow<List<SavedLocationEntity>> = repository.getAllSavedLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (_currentScreen.value != Screen.DASHBOARD) {
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    fun refreshAirQuality() {
        repository.refreshData()
    }

    fun selectLocation(location: SavedLocationEntity) {
        viewModelScope.launch {
            repository.switchLocation(location)
            if (location.baseAqi > 150) {
                AeroGuardNotificationHelper.sendAqiThresholdNotification(
                    getApplication(),
                    "${location.cityName}, ${location.state}",
                    location.baseAqi
                )
            }
        }
    }

    fun triggerTestThresholdAlert() {
        AeroGuardNotificationHelper.sendAqiThresholdNotification(
            getApplication(),
            currentAirQuality.value.locationName,
            168
        )
    }

    suspend fun submitReport(
        sourceType: String,
        location: String,
        description: String,
        severity: String
    ) {
        repository.submitReport(
            sourceType = sourceType,
            location = location,
            description = description,
            severity = severity
        )
    }

    fun upvoteReport(reportId: Long) {
        viewModelScope.launch {
            repository.upvoteReport(reportId)
        }
    }

    suspend fun sendChatMessage(text: String) {
        repository.sendChatMessage(text)
    }

    fun clearChat() {
        repository.clearChat()
    }

    suspend fun searchGrounding(query: String): GroundedResult {
        return repository.getSearchGroundedInfo(query)
    }

    suspend fun mapsGrounding(query: String): GroundedResult {
        return repository.getMapsGroundedInfo(query)
    }

    suspend fun generateImage(prompt: String, aspectRatio: String, imageSize: String): GeneratedImageResult {
        return repository.generateImageVisualization(prompt, aspectRatio, imageSize)
    }

    suspend fun getFastAdvice(profileType: String): String {
        return repository.getInstantHealthAdvice(profileType)
    }
}
