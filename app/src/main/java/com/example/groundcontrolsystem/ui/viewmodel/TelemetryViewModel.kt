package com.example.groundcontrolsystem.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

enum class LogLevel {
    INFO, WARNING, ERROR, DEBUG
}

data class SystemLog(
    val timestamp: String,
    val level: LogLevel,
    val message: String
)

data class MissionLog(
    val timestamp: String,
    val speed: Float,
    val altitude: Float,
    val latitude: Double,
    val longitude: Double
)

class TelemetryViewModel : ViewModel() {
    var batteryLevel by mutableStateOf(1f)
    var isConnected by mutableStateOf(false)
    var signalStrength by mutableStateOf(0f)
    
    var speed by mutableStateOf(0f)
    var altitude by mutableStateOf(0f)
    var latitude by mutableStateOf(1.3521)
    var longitude by mutableStateOf(103.8198)
    
    var isMissionActive by mutableStateOf(false)
    var isRthActive by mutableStateOf(false)
    
    var currentWaypointIndex by mutableStateOf(-1)
    var activeWaypoints = mutableStateListOf<GeoPoint>()
    
    val missionLogs = mutableStateListOf<MissionLog>()
    val systemLogs = mutableStateListOf<SystemLog>()
    
    private var loggingJob: Job? = null
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        addLog(LogLevel.INFO, "System initialized and ready")
        
        viewModelScope.launch {
            while (true) {
                if (isConnected) {
                    batteryLevel = max(0f, batteryLevel - (1f / 150f))
                    signalStrength = (0.7f + (Math.random().toFloat() * 0.3f)).coerceIn(0f, 1f)
                    
                    if (batteryLevel < 0.2f && isMissionActive && !isRthActive) {
                        isRthActive = true
                        addLog(LogLevel.WARNING, "Low battery detected! Emergency RTH initiated")
                    }

                    if (isMissionActive) {
                        if (isRthActive) {
                            speed = (speed + (35f - speed) * 0.1f).coerceIn(0f, 40f)
                            altitude = (altitude + (50f - altitude) * 0.05f).coerceIn(0f, 200f)
                            latitude += (1.3521 - latitude) * 0.05
                            longitude += (103.8198 - longitude) * 0.05
                            
                            if (abs(latitude - 1.3521) < 0.0001 && abs(longitude - 103.8198) < 0.0001) {
                                addLog(LogLevel.INFO, "RTH Complete. Drone landed at home.")
                                stopMission()
                            }
                        } else {
                            speed = (speed + (25f - speed) * 0.1f).coerceIn(0f, 30f)
                            altitude = (altitude + (150f - altitude) * 0.1f).coerceIn(0f, 200f)
                            
                            if (activeWaypoints.isNotEmpty() && currentWaypointIndex != -1) {
                                val target = activeWaypoints[currentWaypointIndex]
                                val latDiff = target.latitude - latitude
                                val lonDiff = target.longitude - longitude
                                latitude += latDiff * 0.05
                                longitude += lonDiff * 0.05
                                
                                if (abs(latDiff) < 0.0005 && abs(lonDiff) < 0.0005) {
                                    addLog(LogLevel.DEBUG, "Reached Waypoint ${currentWaypointIndex + 1}")
                                    if (currentWaypointIndex < activeWaypoints.size - 1) {
                                        currentWaypointIndex++
                                    } else {
                                        speed = max(0f, speed - 2f)
                                    }
                                }
                            } else {
                                latitude += (Math.random() - 0.4) * 0.0002
                                longitude += (Math.random() - 0.4) * 0.0002
                            }
                        }
                    } else {
                        speed = max(0f, speed - 1f)
                        altitude = max(0f, altitude - 2f)
                    }
                } else {
                    signalStrength = 0f
                    speed = 0f
                }
                delay(1000)
            }
        }
    }

    private fun addLog(level: LogLevel, message: String) {
        systemLogs.add(0, SystemLog(sdf.format(Date()), level, message))
    }

    fun toggleConnection() {
        isConnected = !isConnected
        if (isConnected) {
            addLog(LogLevel.INFO, "GCS Connected to Drone")
        } else {
            addLog(LogLevel.ERROR, "Link Lost. Connection terminated.")
            stopMission()
        }
    }

    fun startMission(waypoints: List<GeoPoint> = emptyList()) {
        if (isConnected && !isMissionActive) {
            activeWaypoints.clear()
            activeWaypoints.addAll(waypoints)
            currentWaypointIndex = if (waypoints.isNotEmpty()) 0 else -1
            
            isMissionActive = true
            isRthActive = false
            missionLogs.clear()
            
            addLog(LogLevel.INFO, "Mission Started with ${waypoints.size} waypoints")
            startLogging()
        }
    }

    private fun startLogging() {
        loggingJob?.cancel()
        loggingJob = viewModelScope.launch {
            while (isMissionActive) {
                val log = MissionLog(
                    timestamp = sdf.format(Date()),
                    speed = speed,
                    altitude = altitude,
                    latitude = latitude,
                    longitude = longitude
                )
                missionLogs.add(log)
                
                // Also add a summary to system logs for visibility
                addLog(LogLevel.DEBUG, "Telemetry Sync: Alt ${"%.1f".format(altitude)}m, Spd ${"%.1f".format(speed)}km/h")
                
                if (missionLogs.size >= 100) stopMission()
                delay(5000)
            }
        }
    }

    fun stopMission() {
        if (isMissionActive) {
            addLog(LogLevel.INFO, "Mission Aborted/Completed")
        }
        isMissionActive = false
        isRthActive = false
        currentWaypointIndex = -1
        activeWaypoints.clear()
        loggingJob?.cancel()
    }

    fun getMissionLogsJson(): String {
        val sb = StringBuilder()
        sb.append("[\n")
        missionLogs.forEachIndexed { index, log ->
            sb.append("  {\n")
            sb.append("    \"timestamp\": \"${log.timestamp}\",\n")
            sb.append("    \"speed\": ${"%.2f".format(log.speed)},\n")
            sb.append("    \"altitude\": ${"%.2f".format(log.altitude)},\n")
            sb.append("    \"latitude\": ${"%.6f".format(log.latitude)},\n")
            sb.append("    \"longitude\": ${"%.6f".format(log.longitude)}\n")
            sb.append("  }")
            if (index < missionLogs.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }
}
