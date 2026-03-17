package com.example.groundcontrolsystem

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.groundcontrolsystem.ui.screens.camera.CameraScreen
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CameraScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TelemetryViewModel
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        viewModel = TelemetryViewModel(application)
    }

    @Test
    fun cameraScreenShowsHudElements() {
        composeTestRule.setContent {
            CameraScreen(viewModel = viewModel)
        }

        // Check for HUD elements (using text labels defined in the composables)
        composeTestRule.onNodeWithText("SPD: 0KM/H").assertIsDisplayed()
        composeTestRule.onNodeWithText("ALT: 0M").assertIsDisplayed()
        composeTestRule.onNodeWithText("BAT").assertIsDisplayed()
        composeTestRule.onNodeWithText("SIG").assertIsDisplayed()
        composeTestRule.onNodeWithText("WIND").assertIsDisplayed()
    }

    @Test
    fun recordingToggleUpdatesState() {
        composeTestRule.setContent {
            CameraScreen(viewModel = viewModel)
        }

        assertFalse(viewModel.isRecording)
        
        // Find record button by content description
        composeTestRule.onNodeWithContentDescription("Record").performClick()
        assertTrue(viewModel.isRecording)
        
        // REC indicator should appear
        composeTestRule.onNodeWithText("REC").assertIsDisplayed()
        
        composeTestRule.onNodeWithContentDescription("Record").performClick()
        assertFalse(viewModel.isRecording)
    }

    @Test
    fun telemetryUpdatesReflectInHud() {
        composeTestRule.setContent {
            CameraScreen(viewModel = viewModel)
        }

        val state = viewModel.drones[viewModel.activeDroneId]!!
        state.speed = 45f
        state.altitude = 120f
        state.batteryLevel = 0.85f

        // Compose should recompose and show new values
        composeTestRule.onNodeWithText("SPD: 45KM/H").assertIsDisplayed()
        composeTestRule.onNodeWithText("ALT: 120M").assertIsDisplayed()
        composeTestRule.onNodeWithText("85%").assertIsDisplayed()
    }

    @Test
    fun weatherHudUpdates() {
        composeTestRule.setContent {
            CameraScreen(viewModel = viewModel)
        }

        val state = viewModel.drones[viewModel.activeDroneId]!!
        state.windSpeed = 12.5f
        state.windDirection = 180

        composeTestRule.onNodeWithText("12.5 m/s").assertIsDisplayed()
        composeTestRule.onNodeWithText("180°").assertIsDisplayed()
    }
}
