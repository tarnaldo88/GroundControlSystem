package com.example.groundcontrolsystem.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var autoConnect by remember { mutableStateOf(false) }
    var metricUnits by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(true) }
    var cameraResolution by remember { mutableStateOf("1080p") }
    
    // Drone Specific States
    var maxAltitude by remember { mutableFloatStateOf(120f) }
    var returnToHomeAltitude by remember { mutableFloatStateOf(50f) }
    var obstacleAvoidance by remember { mutableStateOf(true) }
    var droneModel by remember { mutableStateOf("Quadcopter X-1") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SettingHeader("Drone Configuration") }
            item {
                ActionSetting(
                    icon = Icons.Default.Flight,
                    title = "Drone Model",
                    subtitle = droneModel,
                    onClick = { /* TODO: Selection dialog */ }
                )
            }
            item {
                ToggleSetting(
                    icon = Icons.Default.Security,
                    title = "Obstacle Avoidance",
                    subtitle = "Use ultrasonic and vision sensors to prevent collisions",
                    checked = obstacleAvoidance,
                    onCheckedChange = { obstacleAvoidance = it }
                )
            }
            item {
                ActionSetting(
                    icon = Icons.Default.VerticalAlignTop,
                    title = "Max Altitude",
                    subtitle = "${maxAltitude.toInt()}m (Current Limit)",
                    onClick = { /* TODO: Slider dialog */ }
                )
            }
            item {
                ActionSetting(
                    icon = Icons.Default.Home,
                    title = "RTH Altitude",
                    subtitle = "Return to home altitude: ${returnToHomeAltitude.toInt()}m",
                    onClick = { /* TODO: Slider dialog */ }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { SettingHeader("General") }
            item {
                ToggleSetting(
                    icon = Icons.Default.Link,
                    title = "Auto-connect",
                    subtitle = "Automatically connect to the drone on startup",
                    checked = autoConnect,
                    onCheckedChange = { autoConnect = it }
                )
            }
            item {
                ToggleSetting(
                    icon = Icons.Default.Straighten,
                    title = "Metric Units",
                    subtitle = "Use meters and km/h instead of feet and mph",
                    checked = metricUnits,
                    onCheckedChange = { metricUnits = it }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { SettingHeader("Display") }
            item {
                ToggleSetting(
                    icon = Icons.Default.Brightness4,
                    title = "Night Mode",
                    subtitle = "Toggle night/day theme",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { SettingHeader("Camera") }
            item {
                ActionSetting(
                    icon = Icons.Default.Videocam,
                    title = "Resolution",
                    subtitle = "Current: $cameraResolution",
                    onClick = { /* TODO: Show dialog */ }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { SettingHeader("About") }
            item {
                ActionSetting(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0 (Stable)",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ToggleSetting(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun ActionSetting(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
