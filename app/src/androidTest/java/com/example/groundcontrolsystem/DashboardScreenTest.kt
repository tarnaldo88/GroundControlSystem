package com.example.groundcontrolsystem

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.groundcontrolsystem.ui.screens.dashboard.DashboardScreen
import com.example.groundcontrolsystem.ui.viewmodel.MissionLog
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TelemetryViewModel
    private val mockApplication = mock(Application::class.java)

    @Before
    fun setup() {
        viewModel = TelemetryViewModel(mockApplication)
    }

    @Test
    fun dashboardShowsInitialState() {
        composeTestRule.setContent {
            DashboardScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Ground Control System").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battery").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connect").assertIsDisplayed()
        
        // Report button should be disabled when logs are empty
        composeTestRule.onNodeWithText("Post-Flight Report").assertIsNotEnabled()
    }

    @Test
    fun clickingConnectTogglesState() {
        composeTestRule.setContent {
            DashboardScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Connect").performClick()
        composeTestRule.onNodeWithText("Disconnect").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Disconnect").performClick()
        composeTestRule.onNodeWithText("Connect").assertIsDisplayed()
    }

    @Test
    fun reportButtonEnabledWithLogs() {
        // Add a dummy log
        viewModel.missionLogs.add(MissionLog("12:00:00", 10f, 50f, 1.35, 103.8))

        composeTestRule.setContent {
            DashboardScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Post-Flight Report").assertIsEnabled()
        
        composeTestRule.onNodeWithText("Post-Flight Report").performClick()
        composeTestRule.onNodeWithText("Post-Flight Mission Report").assertIsDisplayed()
    }
}
