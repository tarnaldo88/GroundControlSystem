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
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel

@Composable
fun DashboardScreen(viewModel: TelemetryViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Ground Control System",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
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
                value = "${(viewModel.batteryLevel * 100).toInt()}%",
                progress = viewModel.batteryLevel,
                icon = Icons.Default.BatteryFull,
                color = if (viewModel.batteryLevel > 0.2f) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            TelemetryCard(
                label = "Signal",
                value = if (viewModel.isConnected) "${(viewModel.signalStrength * 100).toInt()}%" else "OFF",
                progress = viewModel.signalStrength,
                icon = Icons.Default.SignalCellularAlt,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        // Actions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("System Actions", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = if (viewModel.isConnected) "Disconnect" else "Connect",
                    icon = if (viewModel.isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                    color = if (viewModel.isConnected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.toggleConnection() },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Calibrate",
                    icon = Icons.Default.Build,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TelemetryCard(label: String, value: String, progress: Float, icon: ImageVector, color: Color, modifier: Modifier) {
    val animatedProgress by animateFloatAsState(targetValue = progress)
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit = {}, modifier: Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier.height(100.dp)) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = color)
            Text(text = title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun StatusRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label)
        Text(text = value, color = color)
    }
}
