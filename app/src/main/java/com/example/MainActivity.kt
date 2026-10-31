package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AeroGuardViewModel
import com.example.ui.Screen
import com.example.ui.components.MeteoconIcon
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: AeroGuardViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val airQuality by viewModel.currentAirQuality.collectAsStateWithLifecycle()
                val reports by viewModel.reports.collectAsStateWithLifecycle()
                val locations by viewModel.locations.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

                // BackHandler to return to Dashboard if on a sub-screen
                BackHandler(enabled = currentScreen != Screen.DASHBOARD) {
                    viewModel.navigateBack()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("main_bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == Screen.DASHBOARD,
                                onClick = { viewModel.navigateTo(Screen.DASHBOARD) },
                                icon = { MeteoconIcon(name = "wind", size = 24.dp, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("nav_item_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.REPORT,
                                onClick = { viewModel.navigateTo(Screen.REPORT) },
                                icon = { MeteoconIcon(name = "weather-alarm", size = 24.dp, contentDescription = "Report") },
                                label = { Text("Report") },
                                modifier = Modifier.testTag("nav_item_report")
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.HEALTH,
                                onClick = { viewModel.navigateTo(Screen.HEALTH) },
                                icon = { MeteoconIcon(name = "pollen", size = 24.dp, contentDescription = "Health") },
                                label = { Text("Health") },
                                modifier = Modifier.testTag("nav_item_health")
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.CHAT,
                                onClick = { viewModel.navigateTo(Screen.CHAT) },
                                icon = { Icon(Icons.Default.ChatBubble, contentDescription = "AeroBot") },
                                label = { Text("AeroBot") },
                                modifier = Modifier.testTag("nav_item_chat")
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.GROUNDING || currentScreen == Screen.VISUALIZER,
                                onClick = { viewModel.navigateTo(Screen.GROUNDING) },
                                icon = { Icon(Icons.Default.TravelExplore, contentDescription = "Grounding") },
                                label = { Text("AI Tools") },
                                modifier = Modifier.testTag("nav_item_grounding")
                            )
                        }
                    }
                ) { innerPadding ->
                    when (currentScreen) {
                        Screen.DASHBOARD -> {
                            DashboardScreen(
                                airQuality = airQuality,
                                locations = locations,
                                currentUser = currentUser,
                                onRefresh = { viewModel.refreshAirQuality() },
                                onSelectLocation = { viewModel.selectLocation(it) },
                                onNavigateToReport = { viewModel.navigateTo(Screen.REPORT) },
                                onNavigateToHealth = { viewModel.navigateTo(Screen.HEALTH) },
                                onNavigateToChat = { viewModel.navigateTo(Screen.CHAT) },
                                onNavigateToSearch = { viewModel.navigateTo(Screen.GROUNDING) },
                                onNavigateToMaps = { viewModel.navigateTo(Screen.GROUNDING) },
                                onNavigateToVisualizer = { viewModel.navigateTo(Screen.VISUALIZER) },
                                onNavigateToWebView = { viewModel.navigateTo(Screen.WEB_VIEW) },
                                onTriggerTestAlert = { viewModel.triggerTestThresholdAlert() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.REPORT -> {
                            ReportPollutionScreen(
                                reports = reports,
                                onSubmitReport = { sourceType, loc, desc, severity ->
                                    viewModel.submitReport(sourceType, loc, desc, severity)
                                },
                                onUpvoteReport = { reportId ->
                                    viewModel.upvoteReport(reportId)
                                },
                                onBack = { viewModel.navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.HEALTH -> {
                            HealthAdvisorScreen(
                                airQuality = airQuality,
                                onGetFastAdvice = { profile ->
                                    viewModel.getFastAdvice(profile)
                                },
                                onBack = { viewModel.navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.CHAT -> {
                            ChatbotScreen(
                                messages = chatMessages,
                                onSendMessage = { text ->
                                    viewModel.sendChatMessage(text)
                                },
                                onClearChat = { viewModel.clearChat() },
                                onBack = { viewModel.navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.GROUNDING -> {
                            SearchMapsGroundingScreen(
                                onSearchGroundingQuery = { query ->
                                    viewModel.searchGrounding(query)
                                },
                                onMapsGroundingQuery = { query ->
                                    viewModel.mapsGrounding(query)
                                },
                                onBack = { viewModel.navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.VISUALIZER -> {
                            ImageGeneratorScreen(
                                onGenerateImage = { prompt, ratio, size ->
                                    viewModel.generateImage(prompt, ratio, size)
                                },
                                onBack = { viewModel.navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        Screen.WEB_VIEW -> {
                            WebDashboardScreen(
                                onBack = { viewModel.navigateBack() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
