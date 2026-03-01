package com.example.groundcontrolsystem.ui.screens.logs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class LogLevel {
    INFO, WARNING, ERROR, DEBUG
}

data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val message: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    logs: List<LogEntry>,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
) {
    var selectedFilter by remember { mutableStateOf<LogLevel?>(null) }
    
    val filteredLogs = remember(logs, selectedFilter) {
        if (selectedFilter == null) logs else logs.filter { it.level == selectedFilter }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("System Logs", style = MaterialTheme.typography.headlineSmall)
            
            Row {
                // Filter Chips
                LogLevel.values().forEach { level ->
                    FilterChip(
                        selected = selectedFilter == level,
                        onClick = { selectedFilter = if (selectedFilter == level) null else level },
                        label = { Text(level.name) },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                if (onClear != null) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.FilterList, contentDescription = "Clear")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredLogs.isEmpty()) {
            EmptyLogsState()
        } else {
            Card(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(filteredLogs) { _, entry ->
                        LogRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    val color = when (entry.level) {
        LogLevel.ERROR -> MaterialTheme.colorScheme.error
        LogLevel.WARNING -> Color(0xFFFFA000) // Amber
        LogLevel.INFO -> MaterialTheme.colorScheme.primary
        LogLevel.DEBUG -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "[${entry.level.name}]",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = entry.message,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun EmptyLogsState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No logs match the current filter", style = MaterialTheme.typography.bodyMedium)
    }
}
