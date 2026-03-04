package com.example.groundcontrolsystem.ui.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun DashboardScreen() {
    // Mock Telemetry State
    var batteryLevel by remember { mutableStateOf(1f) } // 0.0 to 1.0
    var isConnected by remember { mutableStateOf(false) }
    var signalStrength by remember { mutableStateOf(0f) }

    // Mock Battery Depletion (5 minutes = 300 seconds)
    // We'll update every second: 1.0 / 300 = 0.00333 per second
    LaunchedEffect(isConnected) {
        if (isConnected) {
            while (batteryLevel > 0) {
                delay(1000)
                batteryLevel = max(0f, batteryLevel - (1f / 300f))
                // Mock fluctuating signal
                signalStrength = (0.7f + (Math.random().toFloat() * 0.3f)).coerceIn(0f, 1f)
            }
        } else {
            signalStrength = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title and Description Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ground Control System",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Welcome to the command center. This application is designed to provide real-time telemetry, visual monitoring via CameraX, and precise location tracking through OpenStreetMap.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Telemetry Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TelemetryCard(
                label = "Battery",
                value = "${(batteryLevel * 100).toInt()}%",
                progress = batteryLevel,
                icon = when {
                    batteryLevel > 0.7f -> Icons.Default.BatteryFull
                    batteryLevel > 0.3f -> Icons.Default.BatteryChargingFull
                    else -> Icons.Default.BatteryAlert
                },
                color = when {
                    batteryLevel > 0.5f -> Color(0xFF4CAF50)
                    batteryLevel > 0.2f -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                },
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                label = "Signal",
                value = if (isConnected) "${(signalStrength * 100).toInt()}%" else "OFF",
                progress = signalStrength,
                icon = if (signalStrength > 0.5f) Icons.Default.SignalCellularAlt else Icons.Default.SignalCellularConnectedNoInternet0Bar,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Actions Section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "System Actions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    title = if (isConnected) "Disconnect" else "Connect",
                    icon = if (isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                    color = if (isConnected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    onClick = { isConnected = !isConnected },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Calibrate",
                    icon = Icons.Default.Build,
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Status Summary Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "System Health", style = MaterialTheme.typography.titleMedium)
                HorizontalDivider()
                StatusRow(
                    label = "Connection", 
                    value = if (isConnected) "Connected" else "Disconnected", 
                    color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
                StatusRow(
                    label = "Uptime", 
                    value = "00:12:45", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusRow(
                    label = "Storage", 
                    value = "85% Free", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TelemetryCard(
    label: String,
    value: String,
    progress: Float,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    val animatedColor by animateColorAsState(targetValue = color, label = "color")

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = animatedColor, modifier = Modifier.size(20.dp))
                Text(text = label, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = animatedColor,
                trackColor = animatedColor.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun StatusRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
