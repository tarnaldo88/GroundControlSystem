package com.example.groundcontrolsystem.ui.screens.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.groundcontrolsystem.ui.viewmodel.LogLevel
import com.example.groundcontrolsystem.ui.viewmodel.SystemLog
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    viewModel: TelemetryViewModel,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<LogLevel?>(null) }
    val logs = viewModel.systemLogs
    
    val filteredLogs = remember(logs, selectedFilter) {
        if (selectedFilter == null) logs.toList() else logs.filter { it.level == selectedFilter }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("System Diagnostics", style = MaterialTheme.typography.headlineSmall)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Filter Chips
                LogLevel.values().forEach { level ->
                    FilterChip(
                        selected = selectedFilter == level,
                        onClick = { selectedFilter = if (selectedFilter == level) null else level },
                        label = { Text(level.name, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                IconButton(onClick = { viewModel.systemLogs.clear() }) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Clear Logs", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No diagnostic events recorded", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredLogs) { _, entry ->
                        SystemLogRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemLogRow(entry: SystemLog) {
    val color = when (entry.level) {
        LogLevel.ERROR -> MaterialTheme.colorScheme.error
        LogLevel.WARNING -> Color(0xFFFFA000) // Amber
        LogLevel.INFO -> MaterialTheme.colorScheme.primary
        LogLevel.DEBUG -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = entry.level.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = color
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = entry.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                )
            )
        }
    }
}
