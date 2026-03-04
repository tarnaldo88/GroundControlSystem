package com.example.groundcontrolsystem.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class TelemetryViewModel : ViewModel() {
    var batteryLevel by mutableStateOf(1f)
    var isConnected by mutableStateOf(false)
    var signalStrength by mutableStateOf(0f)
    
    var speed by mutableStateOf(0f)
    var altitude by mutableStateOf(0f)
    var latitude by mutableStateOf(1.3521)
    var longitude by mutableStateOf(103.8198)
    
    var isMissionActive by mutableStateOf(false)

    init {
        viewModelScope.launch {
            while (true) {
                if (isConnected) {
                    // Battery depletion
                    batteryLevel = max(0f, batteryLevel - (1f / 300f))
                    // Signal fluctuation
                    signalStrength = (0.7f + (Math.random().toFloat() * 0.3f)).coerceIn(0f, 1f)
                    
                    if (isMissionActive) {
                        // Mission flight dynamics (1 minute = 60 seconds)
                        // Target speed: 25 km/h, Target altitude: 150m
                        speed = (speed + (25f - speed) * 0.1f).coerceIn(0f, 30f)
                        altitude = (altitude + (150f - altitude) * 0.1f).coerceIn(0f, 200f)
                        
                        // Drift position
                        latitude += (Math.random() - 0.4) * 0.0002
                        longitude += (Math.random() - 0.4) * 0.0002
                    } else {
                        // Idle state
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
            isMissionActive = false
        }
    }

    fun startMission() {
        if (isConnected) {
            isMissionActive = true
        }
    }
}
