package com.example.groundcontrolsystem.ui.screens.dashboard

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel

@Composable
fun DashboardScreen(viewModel: TelemetryViewModel) {
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    title = "Post-Flight Report",
                    icon = Icons.Default.Assessment,
                    onClick = { showReportDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = viewModel.missionLogs.isNotEmpty()
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = "Calibrate",
                    icon = Icons.Default.Build,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Clear Logs",
                    icon = Icons.Default.DeleteForever,
                    onClick = { viewModel.missionLogs.clear() },
                    modifier = Modifier.weight(1f),
                    enabled = viewModel.missionLogs.isNotEmpty()
                )
            }
        }
    }

    if (showReportDialog) {
        val jsonLogs = viewModel.getMissionLogsJson()
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Post-Flight Mission Report") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total entries: ${viewModel.missionLogs.size}", style = MaterialTheme.typography.labelMedium)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
                            Text(
                                text = jsonLogs,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, jsonLogs)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Mission Logs")
                    context.startActivity(shareIntent)
                }) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Close")
                }
            }
        )
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
fun ActionCard(
    title: String, 
    icon: ImageVector, 
    color: Color = MaterialTheme.colorScheme.primary, 
    onClick: () -> Unit = {}, 
    modifier: Modifier,
    enabled: Boolean = true
) {
    ElevatedCard(
        onClick = if (enabled) onClick else ({}), 
        modifier = modifier.height(100.dp),
        enabled = enabled
    ) {
        Column(
            modifier = Modifier.fillMaxSize().alpha(if (enabled) 1f else 0.5f), 
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (enabled) color else MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// Extension to use alpha on Modifier
fun Modifier.alpha(alpha: Float): Modifier = this.then(
    androidx.compose.ui.draw.alpha(alpha)
)
