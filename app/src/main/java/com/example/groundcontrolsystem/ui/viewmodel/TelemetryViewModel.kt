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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

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
    
    // Mission logs stored as a list
    val missionLogs = mutableStateListOf<MissionLog>()
    private var loggingJob: Job? = null

    init {
        viewModelScope.launch {
            while (true) {
                if (isConnected) {
                    // Battery depletion (increased rate for demo: 2.5 min to 0 to reach 20% faster)
                    batteryLevel = max(0f, batteryLevel - (1f / 150f))
                    // Signal fluctuation
                    signalStrength = (0.7f + (Math.random().toFloat() * 0.3f)).coerceIn(0f, 1f)
                    
                    // Smart Battery Logic
                    if (batteryLevel < 0.2f && isMissionActive && !isRthActive) {
                        isRthActive = true
                    }

                    if (isMissionActive) {
                        if (isRthActive) {
                            // RTH dynamics: return to home coordinates (1.3521, 103.8198)
                            speed = (speed + (35f - speed) * 0.1f).coerceIn(0f, 40f) // Flying back fast
                            altitude = (altitude + (50f - altitude) * 0.05f).coerceIn(0f, 200f) // Descending slowly
                            
                            // Move towards home
                            latitude += (1.3521 - latitude) * 0.05
                            longitude += (103.8198 - longitude) * 0.05
                            
                            // Stop mission if reached home
                            if (Math.abs(latitude - 1.3521) < 0.0001 && Math.abs(longitude - 103.8198) < 0.0001) {
                                stopMission()
                            }
                        } else {
                            // Normal Mission flight dynamics
                            speed = (speed + (25f - speed) * 0.1f).coerceIn(0f, 30f)
                            altitude = (altitude + (150f - altitude) * 0.1f).coerceIn(0f, 200f)
                            
                            // Drift position
                            latitude += (Math.random() - 0.4) * 0.0002
                            longitude += (Math.random() - 0.4) * 0.0002
                        }
                    } else {
                        // Idle state / Returning to zero
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

    fun toggleConnection() {
        isConnected = !isConnected
        if (!isConnected) {
            stopMission()
        }
    }

    fun startMission() {
        if (isConnected && !isMissionActive) {
            isMissionActive = true
            isRthActive = false
            missionLogs.clear()
            startLogging()
        }
    }

    private fun startLogging() {
        loggingJob?.cancel()
        loggingJob = viewModelScope.launch {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            while (isMissionActive) {
                val log = MissionLog(
                    timestamp = sdf.format(Date()),
                    speed = speed,
                    altitude = altitude,
                    latitude = latitude,
                    longitude = longitude
                )
                missionLogs.add(log)
                
                // End mission automatically if logs get too many, or just keep logging
                if (missionLogs.size >= 100) {
                    stopMission()
                }
                
                delay(5000) // Log every 5 seconds
            }
        }
    }

    fun stopMission() {
        isMissionActive = false
        isRthActive = false
        loggingJob?.cancel()
    }

    // Helper to get logs as JSON string
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
