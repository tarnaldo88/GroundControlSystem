package com.example.groundcontrolsystem.ui.screens.logs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun LogsScreen(
    logs: List<String>,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
    onLogLongPress: ((String) -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Logs", style = MaterialTheme.typography.titleLarge)

            if(onClear != null) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
        }

        if(logs.isEmpty()) {
            EmptyLogsState()
        } else {
            LogsList(
                logs = logs,
                onLogLongPress = onLogLongPress
            )
        }
    }
}

@Composable
private fun EmptyLogsState() {
    Card(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        ) {
            Text(
                "No logs",
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun LogsList(
    logs: List<String>,
    onLogLongPress: ((String) -> Unit)?,
) {
    Card(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = logs,
                key = { index, _ -> index }
            ) { _, line ->
                LogRow(
                    line = line,
                    onLongPress = { onLogLongPress?.invoke(line) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LogRow(
    line: String,
    onLongPress: (() -> Unit)? = null,
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { onLongPress?.invoke() }
            )
    ) {
        Text(
            text = line,
            modifier= Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

