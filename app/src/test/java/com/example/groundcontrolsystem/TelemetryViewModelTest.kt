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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
        assertTrue(viewModel.isConnected) // drone_1 is added in init
        assertEquals(1f, viewModel.batteryLevel)
        assertEquals(0f, viewModel.speed)
        assertEquals(0f, viewModel.altitude)
        assertFalse(viewModel.isMissionActive)
        assertEquals(2, viewModel.noFlyZones.size)
    }

    @Test
    fun testMultiDroneSupport() {
        val droneId = "drone_2"
        val droneName = "Secondary Drone"
        
        viewModel.addDrone(droneId, droneName)
        
        assertTrue(viewModel.drones.containsKey(droneId))
        assertEquals(droneName, viewModel.drones[droneId]?.name)
        
        // Switch to drone_2
        viewModel.switchActiveDrone(droneId)
        assertEquals(droneId, viewModel.activeDroneId)
        
        // Verify logs reflect the switch
        val latestLog = viewModel.systemLogs.first()
        assertTrue(latestLog.message.contains("Switched control to: $droneName"))
    }

    @Test
    fun testStartMission() {
        val waypoints = listOf(
            Waypoint(1, GeoPoint(1.35, 103.8), targetAltitude = 50f, targetSpeed = 10f),
            Waypoint(2, GeoPoint(1.36, 103.9), action = WaypointAction.HOVER, actionDuration = 5)
        )
        
        viewModel.startMission(waypoints)
        
        assertTrue(viewModel.isMissionActive)
        assertEquals(2, viewModel.activeWaypoints.size)
        assertEquals(0, viewModel.currentWaypointIndex)
        
        val latestLog = viewModel.systemLogs.first()
        assertTrue(latestLog.message.contains("Mission started"))
    }

    @Test
    fun testStopMission() {
        viewModel.startMission(listOf(Waypoint(1, GeoPoint(0.0, 0.0))))
        
        assertTrue(viewModel.isMissionActive)
        viewModel.stopMission()
        
        assertFalse(viewModel.isMissionActive)
        assertEquals(-1, viewModel.currentWaypointIndex)
        
        val latestLog = viewModel.systemLogs.first()
        assertTrue(latestLog.message.contains("Mission stopped"))
    }

    @Test
    fun testToggleRecording() {
        assertFalse(viewModel.isRecording)
        viewModel.toggleRecording()
        assertTrue(viewModel.isRecording)
        viewModel.toggleRecording()
        assertFalse(viewModel.isRecording)
    }

    @Test
    fun testNfzProximity() = runTest {
        // Move active drone near Airport Alpha (1.3644, 103.9915)
        val state = viewModel.drones[viewModel.activeDroneId]!!
        state.latitude = 1.3640
        state.longitude = 103.9910
        
        // Advance time to trigger the periodic check in init block
        advanceTimeBy(1100)
        
        assertTrue("Should be near NFZ", viewModel.isNearNfz)
    }

    @Test
    fun testBatteryWarning() = runTest {
        val state = viewModel.drones[viewModel.activeDroneId]!!
        state.batteryLevel = 0.15f // Below 20%
        
        advanceTimeBy(1100)
        
        val hasBatteryLog = viewModel.systemLogs.any { 
            it.level == LogLevel.WARNING && it.message.contains("Low battery") 
        }
        assertTrue("Should have a low battery warning log", hasBatteryLog)
    }

    @Test
    fun testHighWindWarning() = runTest {
        val state = viewModel.drones[viewModel.activeDroneId]!!
        state.windSpeed = 20f // Above 15m/s threshold
        
        advanceTimeBy(1100)
        
        val hasWindLog = viewModel.systemLogs.any { 
            it.level == LogLevel.WARNING && it.message.contains("High Wind Warning") 
        }
        assertTrue("Should have a high wind warning log", hasWindLog)
    }

    @Test
    fun testMissionProgression() = runTest {
        val waypoints = listOf(
            Waypoint(1, GeoPoint(1.3522, 103.8200), targetAltitude = 50f, targetSpeed = 10f)
        )
        viewModel.startMission(waypoints)
        
        val state = viewModel.drones[viewModel.activeDroneId]!!
        // Mock current position to be very close to waypoint
        state.latitude = 1.35219
        state.longitude = 103.81999
        
        advanceTimeBy(1100)
        
        // It should progress to next state (RTH since it's the last waypoint)
        assertTrue("RTH should be active after reaching last waypoint", state.isRthActive)
    }
}
