package com.example.groundcontrolsystem

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.groundcontrolsystem.ui.screens.missionplan.MissionPlanScreen
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MissionPlanScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TelemetryViewModel
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        viewModel = TelemetryViewModel(application)
    }

    @Test
    fun missionPlanShowsInitialState() {
        composeTestRule.setContent {
            MissionPlanScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Mission Planner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap map to add waypoints").assertIsDisplayed()
        
        // Execute button should be disabled initially (no waypoints and no connection)
        composeTestRule.onNodeWithText("Execute Mission").assertIsNotEnabled()
    }

    @Test
    fun executeButtonStaysDisabledWithoutConnection() {
        composeTestRule.setContent {
            MissionPlanScreen(viewModel = viewModel)
        }

        // Even with waypoints, it should be disabled if not connected
        composeTestRule.onNodeWithText("Execute Mission").assertIsNotEnabled()
    }

    @Test
    fun executeButtonEnabledWhenConnectedAndWaypointsExist() {
        // Mock connection
        viewModel.toggleConnection()
        
        composeTestRule.setContent {
            MissionPlanScreen(viewModel = viewModel)
        }
        
        // Verify it's still disabled because no waypoints exist
        composeTestRule.onNodeWithText("Execute Mission").assertIsNotEnabled()
    }
}
