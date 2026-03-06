package com.example.groundcontrolsystem.ui.viewmodel

import android.app.Application
import android.graphics.RectF
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

enum class LogLevel {
    INFO, WARNING, ERROR, DEBUG
}

enum class WaypointAction {
    NAVIGATE, HOVER, TAKE_PHOTO, LAND
}

data class Waypoint(
    val id: Int,
    val location: GeoPoint,
    val targetAltitude: Float = 100f,
    val targetSpeed: Float = 20f,
    val action: WaypointAction = WaypointAction.NAVIGATE,
    val actionDuration: Int = 0 // seconds
)

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

class TelemetryViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
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
    var activeWaypoints = mutableStateListOf<Waypoint>()
    
    // Map Tile Source Shared State
    var currentTileSource: ITileSource by mutableStateOf(TileSourceFactory.MAPNIK)

    val missionLogs = mutableStateListOf<MissionLog>()
    val systemLogs = mutableStateListOf<SystemLog>()
    
    val telemetryHistory = mutableStateListOf<MissionLog>()

    // Camera & Vision Features
    var isRecording by mutableStateOf(false)
    var trackedObjectBox by mutableStateOf<RectF?>(null)

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var lastBatteryWarning = 0f

    private var loggingJob: Job? = null
    private var udpJob: Job? = null
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        tts = TextToSpeech(application, this)
        addLog(LogLevel.INFO, "System initialized and ready")
        
        // Continuous Telemetry & TTS Monitoring
        viewModelScope.launch {
            while (true) {
                if (isConnected) {
                    batteryLevel = max(0f, batteryLevel - (1f / 800f))
                    signalStrength = (0.7f + (Math.random().toFloat() * 0.3f)).coerceIn(0f, 1f)
                    
                    checkAlerts()

                    if (isMissionActive) {
                        simulateFlight()
                    } else if (!isUdpActive()) {
                        speed = max(0f, speed - 1f)
                        altitude = max(0f, altitude - 2f)
                    }
                } else {
                    signalStrength = 0f
                    speed = 0f
                }
                
                val currentLog = MissionLog(sdf.format(Date()), speed, altitude, latitude, longitude)
                telemetryHistory.add(currentLog)
                if (telemetryHistory.size > 50) telemetryHistory.removeAt(0)
                
                delay(1000)
            }
        }
    }

    private fun isUdpActive(): Boolean = udpJob?.isActive == true

    private fun checkAlerts() {
        if (batteryLevel < 0.2f && lastBatteryWarning != 0.2f) {
            speak("Warning: Battery low. 20 percent remaining.")
            addLog(LogLevel.WARNING, "Low battery alert: 20%")
            lastBatteryWarning = 0.2f
        } else if (batteryLevel < 0.1f && lastBatteryWarning != 0.1f) {
            speak("Critical Warning: Battery at 10 percent. Landing recommended.")
            addLog(LogLevel.ERROR, "Critical battery alert: 10%")
            lastBatteryWarning = 0.1f
        }

        if (batteryLevel < 0.2f && isMissionActive && !isRthActive) {
            isRthActive = true
            speak("Low battery detected. Emergency return to home initiated.")
            addLog(LogLevel.WARNING, "Emergency RTH initiated due to low battery")
        }
    }

    private suspend fun simulateFlight() {
        if (isRthActive) {
            speed = (speed + (35f - speed) * 0.1f).coerceIn(0f, 40f)
            altitude = (altitude + (50f - altitude) * 0.05f).coerceIn(0f, 200f)
            latitude += (1.3521 - latitude) * 0.05
            longitude += (103.8198 - longitude) * 0.05
            
            if (abs(latitude - 1.3521) < 0.0001 && abs(longitude - 103.8198) < 0.0001) {
                speak("Return to home complete. Drone landed.")
                addLog(LogLevel.INFO, "RTH Complete. Drone landed at home.")
                stopMission()
            }
        } else {
            if (activeWaypoints.isNotEmpty() && currentWaypointIndex != -1) {
                val targetWp = activeWaypoints[currentWaypointIndex]
                val target = targetWp.location
                
                speed = (speed + (targetWp.targetSpeed - speed) * 0.1f).coerceIn(0f, 40f)
                altitude = (altitude + (targetWp.targetAltitude - altitude) * 0.1f).coerceIn(0f, 500f)
                
                val latDiff = target.latitude - latitude
                val lonDiff = target.longitude - longitude
                latitude += latDiff * 0.05
                longitude += lonDiff * 0.05
                
                if (abs(latDiff) < 0.0005 && abs(lonDiff) < 0.0005) {
                    handleWaypointArrival(targetWp)
                }
            }
        }
    }

    private suspend fun handleWaypointArrival(wp: Waypoint) {
        speak("Reached waypoint ${wp.id}")
        when (wp.action) {
            WaypointAction.HOVER -> {
                addLog(LogLevel.INFO, "Hovering at Waypoint ${wp.id} for ${wp.actionDuration}s")
                speed = 0f
                delay(wp.actionDuration * 1000L)
            }
            WaypointAction.TAKE_PHOTO -> {
                addLog(LogLevel.INFO, "Taking photo at Waypoint ${wp.id}")
                speak("Taking photograph")
                delay(2000)
            }
            WaypointAction.LAND -> {
                addLog(LogLevel.INFO, "Landing at Waypoint ${wp.id}")
                speak("Mission target reached. Landing.")
                stopMission()
                return
            }
            else -> {
                addLog(LogLevel.DEBUG, "Reached Waypoint ${wp.id}")
            }
        }

        if (currentWaypointIndex < activeWaypoints.size - 1) {
            currentWaypointIndex++
        } else {
            speak("Mission path completed. Returning to home.")
            addLog(LogLevel.INFO, "Mission Path Completed")
            isRthActive = true
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsReady = true
        }
    }

    private fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    fun setTileSource(source: ITileSource) {
        currentTileSource = source
    }

    private fun addLog(level: LogLevel, message: String) {
        systemLogs.add(0, SystemLog(sdf.format(Date()), level, message))
    }

    fun toggleConnection() {
        if (!isConnected) {
            startUdpListener()
            isConnected = true
            speak("Ground control station connected.")
            addLog(LogLevel.INFO, "GCS Connected. UDP Listener started on port 14550.")
        } else {
            stopUdpListener()
            isConnected = false
            speak("Connection lost.")
            addLog(LogLevel.ERROR, "GCS Disconnected.")
            stopMission()
        }
    }

    private fun startUdpListener() {
        udpJob?.cancel()
        udpJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val socket = DatagramSocket(14550)
                val buffer = ByteArray(1024)
                while (isConnected) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val data = String(packet.data, 0, packet.length)
                    parseUdpData(data)
                }
                socket.close()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addLog(LogLevel.ERROR, "UDP Error: ${e.message}")
                }
            }
        }
    }

    private fun stopUdpListener() {
        udpJob?.cancel()
        udpJob = null
    }

    private fun parseUdpData(data: String) {
        // Simple mock MAVLink-like parser: "lat:1.35;lon:103.8;alt:100;spd:15"
        try {
            viewModelScope.launch(Dispatchers.Main) {
                val parts = data.split(";")
                parts.forEach { part ->
                    val pair = part.split(":")
                    if (pair.size == 2) {
                        when (pair[0]) {
                            "lat" -> latitude = pair[1].toDouble()
                            "lon" -> longitude = pair[1].toDouble()
                            "alt" -> altitude = pair[1].toFloat()
                            "spd" -> speed = pair[1].toFloat()
                            "bat" -> batteryLevel = pair[1].toFloat() / 100f
                        }
                    }
                }
            }
        } catch (e: Exception) {}
    }

    fun startMission(waypoints: List<Waypoint>) {
        if (isConnected && !isMissionActive) {
            speak("Starting mission.")
            activeWaypoints.clear()
            activeWaypoints.addAll(waypoints)
            currentWaypointIndex = if (waypoints.isNotEmpty()) 0 else -1
            
            isMissionActive = true
            isRthActive = false
            missionLogs.clear()
            
            addLog(LogLevel.INFO, "Mission Started with ${waypoints.size} waypoints")
        }
    }

    fun stopMission() {
        if (isMissionActive) {
            speak("Mission aborted.")
            addLog(LogLevel.INFO, "Mission Aborted/Completed")
        }
        isMissionActive = false
        isRthActive = false
        currentWaypointIndex = -1
        activeWaypoints.clear()
    }

    fun toggleRecording() {
        isRecording = !isRecording
        if (isRecording) {
            speak("Recording started.")
            addLog(LogLevel.INFO, "Video recording started")
        } else {
            speak("Recording saved.")
            addLog(LogLevel.INFO, "Video recording saved")
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        stopUdpListener()
        super.onCleared()
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
