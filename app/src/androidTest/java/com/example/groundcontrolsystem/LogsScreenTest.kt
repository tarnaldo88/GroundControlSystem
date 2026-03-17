package com.example.groundcontrolsystem

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.groundcontrolsystem.ui.screens.logs.LogsScreen
import com.example.groundcontrolsystem.ui.viewmodel.LogLevel
import com.example.groundcontrolsystem.ui.viewmodel.SystemLog
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: TelemetryViewModel
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        viewModel = TelemetryViewModel(application)
        viewModel.systemLogs.clear()
    }

    @Test
    fun logsScreenShowsEmptyState() {
        composeTestRule.setContent {
            LogsScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("No diagnostic events recorded").assertIsDisplayed()
    }

    @Test
    fun logsScreenDisplaysLogs() {
        val testMessage = "Test Log Message"
        viewModel.systemLogs.add(SystemLog("12:00:00", LogLevel.INFO, testMessage))

        composeTestRule.setContent {
            LogsScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText(testMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("INFO").assertIsDisplayed()
    }

    @Test
    fun filteringLogsByLevel() {
        viewModel.systemLogs.add(SystemLog("12:00:01", LogLevel.INFO, "Info Log"))
        viewModel.systemLogs.add(SystemLog("12:00:02", LogLevel.ERROR, "Error Log"))

        composeTestRule.setContent {
            LogsScreen(viewModel = viewModel)
        }

        // Initially both are displayed
        composeTestRule.onNodeWithText("Info Log").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error Log").assertIsDisplayed()

        // Filter by ERROR
        composeTestRule.onNodeWithText("ERROR", useUnmergedTree = true).performClick()

        // Only Error Log should be visible
        composeTestRule.onNodeWithText("Error Log").assertIsDisplayed()
        composeTestRule.onNodeWithText("Info Log").assertDoesNotExist()

        // Unselect filter
        composeTestRule.onNodeWithText("ERROR", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText("Info Log").assertIsDisplayed()
    }

    @Test
    fun clearLogsButtonWorks() {
        viewModel.systemLogs.add(SystemLog("12:00:01", LogLevel.INFO, "Log to clear"))

        composeTestRule.setContent {
            LogsScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Log to clear").assertIsDisplayed()
        
        // Click clear button (find by content description)
        composeTestRule.onNodeWithContentDescription("Clear Logs").performClick()
        
        assertTrue(viewModel.systemLogs.isEmpty())
        composeTestRule.onNodeWithText("No diagnostic events recorded").assertIsDisplayed()
    }
}
