package com.example.groundcontrolsystem.ui.viewmodel

import android.app.Application
import android.graphics.RectF
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

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

data class NoFlyZone(
    val id: String,
    val name: String,
    val center: GeoPoint,
    val radiusMeter: Double
)

data class DroneState(
    val id: String,
    val name: String,
    var batteryLevel: Float = 1f,
    var signalStrength: Float = 0f,
    var speed: Float = 0f,
    var altitude: Float = 0f,
    var latitude: Double = 1.3521,
    var longitude: Double = 103.8198,
    var isMissionActive: Boolean = false,
    var isRthActive: Boolean = false,
    var currentWaypointIndex: Int = -1,
    val activeWaypoints: MutableList<Waypoint> = mutableListOf(),
    var windSpeed: Float = 0f,
    var windDirection: Int = 0
)

// Retrofit Service for Weather
interface WeatherService {
    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

data class WeatherResponse(
    val wind: WindData,
    val name: String
)

data class WindData(
    val speed: Float,
    val deg: Int
)

class TelemetryViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    // Multi-Drone Support
    val drones = mutableStateMapOf<String, DroneState>()
    var activeDroneId by mutableStateOf("drone_1")

    // Active Drone Derived Properties
    val activeDroneState: DroneState? get() = drones[activeDroneId]
    
    val batteryLevel: Float get() = activeDroneState?.batteryLevel ?: 0f
    val isConnected: Boolean get() = activeDroneId in drones
    val signalStrength: Float get() = activeDroneState?.signalStrength ?: 0f
    val speed: Float get() = activeDroneState?.speed ?: 0f
    val altitude: Float get() = activeDroneState?.altitude ?: 0f
    val latitude: Double get() = activeDroneState?.latitude ?: 1.3521
    val longitude: Double get() = activeDroneState?.longitude ?: 103.8198
    val isMissionActive: Boolean get() = activeDroneState?.isMissionActive ?: false
    val isRthActive: Boolean get() = activeDroneState?.isRthActive ?: false
    val currentWaypointIndex: Int get() = activeDroneState?.currentWaypointIndex ?: -1
    val activeWaypoints: List<Waypoint> get() = activeDroneState?.activeWaypoints ?: emptyList()
    val windSpeed: Float get() = activeDroneState?.windSpeed ?: 0f
    val windDirection: Int get() = activeDroneState?.windDirection ?: 0

    // Shared State
    var currentTileSource: ITileSource by mutableStateOf(TileSourceFactory.MAPNIK)
    val missionLogs = mutableStateListOf<MissionLog>()
    val systemLogs = mutableStateListOf<SystemLog>()
    val telemetryHistory = mutableStateListOf<MissionLog>()
    val noFlyZones = mutableStateListOf<NoFlyZone>()
    var isNearNfz by mutableStateOf(false)
    var isCaching by mutableStateOf(false)
    var cacheProgress by mutableStateOf(0f)
    var isRecording by mutableStateOf(false)
    var trackedObjectBox by mutableStateOf<RectF?>(null)

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var lastBatteryWarning = 0f
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // Weather API (OpenWeatherMap)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val weatherService = retrofit.create(WeatherService::class.java)
    private val WEATHER_API_KEY = "YOUR_API_KEY_HERE" // Replace with actual key

    init {
        tts = TextToSpeech(application, this)
        addLog(LogLevel.INFO, "System initialized and ready")
        
        drones["drone_1"] = DroneState("drone_1", "Primary Drone")
        
        noFlyZones.add(NoFlyZone("1", "Airport Alpha", GeoPoint(1.3644, 103.9915), 5000.0))
        noFlyZones.add(NoFlyZone("2", "Government Plaza", GeoPoint(1.3521, 103.8198 + 0.05), 2000.0))

        viewModelScope.launch {
            while (true) {
                drones.forEach { (id, state) ->
                    simulateStateUpdate(state)
                }
                
                activeDroneState?.let {
                    val currentLog = MissionLog(sdf.format(Date()), it.speed, it.altitude, it.latitude, it.longitude)
                    telemetryHistory.add(currentLog)
                    if (telemetryHistory.size > 50) telemetryHistory.removeAt(0)
                    
                    checkAlerts(it)
                    checkNfzProximity(it)
                }
                
                delay(1000)
            }
        }

        // Periodic Weather Updates (every 30 seconds)
        viewModelScope.launch {
            while (true) {
                updateWeather()
                delay(30000)
            }
        }
    }

    private suspend fun updateWeather() {
        activeDroneState?.let { state ->
            try {
                // If API key is missing, simulate some wind for visual feedback
                if (WEATHER_API_KEY == "YOUR_API_KEY_HERE") {
                    state.windSpeed = (2f + Math.random().toFloat() * 10f)
                    state.windDirection = (0..359).random()
                } else {
                    val response = weatherService.getWeather(state.latitude, state.longitude, WEATHER_API_KEY)
                    state.windSpeed = response.wind.speed
                    state.windDirection = response.wind.deg
                }
            } catch (e: Exception) {
                // Fallback simulation on error
                state.windSpeed = 5.5f
                state.windDirection = 45
            }
        }
    }

    private fun simulateStateUpdate(state: DroneState) {
        if (state.isMissionActive) {
            simulateFlight(state)
        } else {
            state.speed = max(0f, state.speed - 1f)
            state.altitude = max(0f, state.altitude - 2f)
        }
        state.batteryLevel = max(0f, state.batteryLevel - (1f / 1200f))
        state.signalStrength = (0.7f + (Math.random().toFloat() * 0.3f)).coerceIn(0f, 1f)
    }

    private fun checkAlerts(state: DroneState) {
        if (state.batteryLevel < 0.2f && lastBatteryWarning != 0.2f) {
            speak("Warning: ${state.name} Battery low.")
            addLog(LogLevel.WARNING, "${state.name} Low battery alert: 20%")
            lastBatteryWarning = 0.2f
        }
        if (state.windSpeed > 15f) { // High wind threshold
            speak("Caution: High wind speeds detected for ${state.name}.")
            addLog(LogLevel.WARNING, "High Wind Warning: ${"%.1f".format(state.windSpeed)} m/s")
        }
    }

    private fun checkNfzProximity(state: DroneState) {
        var nearAny = false
        noFlyZones.forEach { nfz ->
            val distance = calculateDistance(state.latitude, state.longitude, nfz.center.latitude, nfz.center.longitude)
            if (distance < nfz.radiusMeter + 500.0) {
                nearAny = true
                if (!isNearNfz) {
                    speak("Warning: ${state.name} entering restricted airspace.")
                }
            }
        }
        isNearNfz = nearAny
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun simulateFlight(state: DroneState) {
        if (state.isRthActive) {
            state.speed = (state.speed + (35f - state.speed) * 0.1f).coerceIn(0f, 40f)
            state.altitude = (state.altitude + (50f - state.altitude) * 0.05f).coerceIn(0f, 200f)
            state.latitude += (1.3521 - state.latitude) * 0.02
            state.longitude += (103.8198 - state.longitude) * 0.02
            
            if (abs(state.latitude - 1.3521) < 0.0001 && abs(state.longitude - 103.8198) < 0.0001) {
                speak("${state.name} landed.")
                state.isMissionActive = false
                state.isRthActive = false
            }
        } else if (state.activeWaypoints.isNotEmpty() && state.currentWaypointIndex != -1) {
            val targetWp = state.activeWaypoints[state.currentWaypointIndex]
            val target = targetWp.location
            
            state.speed = (state.speed + (targetWp.targetSpeed - state.speed) * 0.1f).coerceIn(0f, 40f)
            state.altitude = (state.altitude + (targetWp.targetAltitude - state.altitude) * 0.1f).coerceIn(0f, 500f)
            
            val latDiff = target.latitude - state.latitude
            val lonDiff = target.longitude - state.longitude
            state.latitude += latDiff * 0.05
            state.longitude += lonDiff * 0.05
            
            if (abs(latDiff) < 0.0005 && abs(lonDiff) < 0.0005) {
                if (state.currentWaypointIndex < state.activeWaypoints.size - 1) {
                    state.currentWaypointIndex++
                } else {
                    state.isRthActive = true
                }
            }
        }
    }

    fun addDrone(id: String, name: String) {
        drones[id] = DroneState(id, name)
        addLog(LogLevel.INFO, "New drone registered: $name")
    }

    fun switchActiveDrone(id: String) {
        if (id in drones) {
            activeDroneId = id
            addLog(LogLevel.INFO, "Switched control to: ${drones[id]?.name}")
        }
    }

    fun toggleConnection() {
        if (activeDroneId in drones) {
            addLog(LogLevel.INFO, "Active connection toggled for $activeDroneId")
        }
    }

    fun startMission(waypoints: List<Waypoint>) {
        activeDroneState?.let { state ->
            state.activeWaypoints.clear()
            state.activeWaypoints.addAll(waypoints)
            state.currentWaypointIndex = 0
            state.isMissionActive = true
            state.isRthActive = false
            addLog(LogLevel.INFO, "Mission started for ${state.name}")
        }
    }

    fun stopMission() {
        activeDroneState?.let { state ->
            state.isMissionActive = false
            state.currentWaypointIndex = -1
            addLog(LogLevel.INFO, "Mission stopped for ${state.name}")
        }
    }

    fun toggleRecording() {
        isRecording = !isRecording
    }

    fun setTileSource(source: ITileSource) {
        currentTileSource = source
    }

    private fun addLog(level: LogLevel, message: String) {
        systemLogs.add(0, SystemLog(sdf.format(Date()), level, message))
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

    fun downloadAreaTiles(mapView: MapView, zoomMin: Int, zoomMax: Int) {
        val cacheManager = CacheManager(mapView)
        viewModelScope.launch(Dispatchers.IO) {
            isCaching = true
            cacheManager.downloadAreaAsync(mapView.context, mapView.boundingBox, zoomMin, zoomMax, object : CacheManager.CacheManagerCallback {
                override fun onTaskComplete() { isCaching = false }
                override fun onTaskFailed(errors: Int) { isCaching = false }
                override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) { cacheProgress = progress / 100f }
                override fun downloadStarted() {}
                override fun setPossibleTilesInArea(total: Int) {}
            })
        }
    }

    fun getMissionLogsJson(): String = "[]"

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
