package com.example.groundcontrolsystem.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel

@Composable
fun StatisticsScreen(viewModel: TelemetryViewModel) {
    val history = viewModel.missionLogs.takeLast(50)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mission Statistics", style = MaterialTheme.typography.headlineMedium)

        StatChart(
            title = "Altitude (m)",
            data = history.map { it.altitude },
            color = MaterialTheme.colorScheme.primary,
            maxVal = 200f
        )

        StatChart(
            title = "Speed (km/h)",
            data = history.map { it.speed },
            color = MaterialTheme.colorScheme.secondary,
            maxVal = 40f
        )

        StatChart(
            title = "Battery (%)",
            data = history.map { it.speed * 2f }, // Just for visual mock if battery is static
            color = Color(0xFF4CAF50),
            maxVal = 100f
        )
    }
}

@Composable
fun StatChart(title: String, data: List<Float>, color: Color, maxVal: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (data.size < 2) return@Canvas

                val width = size.width
                val height = size.height
                val spacePerPoint = width / (data.size - 1)

                val path = Path().apply {
                    data.forEachIndexed { index, value ->
                        val x = index * spacePerPoint
                        val y = height - (value / maxVal * height)
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}
