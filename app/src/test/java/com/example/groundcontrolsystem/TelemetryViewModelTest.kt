package com.example.groundcontrolsystem

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.groundcontrolsystem.ui.viewmodel.LogLevel
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import com.example.groundcontrolsystem.ui.viewmodel.Waypoint
import com.example.groundcontrolsystem.ui.viewmodel.WaypointAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: TelemetryViewModel
    private val mockApplication = mock(Application::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TelemetryViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertFalse(viewModel.isConnected)
        assertEquals(1f, viewModel.batteryLevel)
        assertEquals(0f, viewModel.speed)
        assertEquals(0f, viewModel.altitude)
        assertFalse(viewModel.isMissionActive)
    }

    @Test
    fun testToggleConnection() {
        assertFalse(viewModel.isConnected)
        viewModel.toggleConnection()
        assertTrue(viewModel.isConnected)
        
        // Check if connection log was added
        val lastLog = viewModel.systemLogs.firstOrNull()
        assertNotNull(lastLog)
        assertEquals(LogLevel.INFO, lastLog?.level)
        assertTrue(lastLog?.message?.contains("GCS Connected") == true)

        viewModel.toggleConnection()
        assertFalse(viewModel.isConnected)
    }

    @Test
    fun testStartMission() {
        viewModel.toggleConnection() // Must be connected to start mission
        
        val waypoints = listOf(
            Waypoint(1, GeoPoint(1.35, 103.8), targetAltitude = 50f, targetSpeed = 10f),
            Waypoint(2, GeoPoint(1.36, 103.9), action = WaypointAction.HOVER, actionDuration = 5)
        )
        
        viewModel.startMission(waypoints)
        
        assertTrue(viewModel.isMissionActive)
        assertEquals(2, viewModel.activeWaypoints.size)
        assertEquals(0, viewModel.currentWaypointIndex)
    }

    @Test
    fun testStopMission() {
        viewModel.toggleConnection()
        viewModel.startMission(listOf(Waypoint(1, GeoPoint(0.0, 0.0))))
        
        assertTrue(viewModel.isMissionActive)
        viewModel.stopMission()
        
        assertFalse(viewModel.isMissionActive)
        assertEquals(-1, viewModel.currentWaypointIndex)
        assertTrue(viewModel.activeWaypoints.isEmpty())
    }

    @Test
    fun testToggleRecording() {
        assertFalse(viewModel.isRecording)
        viewModel.toggleRecording()
        assertTrue(viewModel.isRecording)
        viewModel.toggleRecording()
        assertFalse(viewModel.isRecording)
    }
}
