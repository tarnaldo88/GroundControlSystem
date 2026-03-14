package com.example.groundcontrolsystem.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(viewModel: TelemetryViewModel) {
    val history = viewModel.telemetryHistory.toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Live Mission Analytics", style = MaterialTheme.typography.headlineMedium)
            if (viewModel.isMissionActive) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "LIVE DATA",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        EnhancedStatChart(
            title = "Altitude Trend (M)",
            data = history.map { it.altitude },
            color = Color(0xFF2196F3),
            maxVal = 200f,
            unit = "m"
        )

        EnhancedStatChart(
            title = "Airspeed (KM/H)",
            data = history.map { it.speed },
            color = Color(0xFFFFC107),
            maxVal = 60f,
            unit = "km/h"
        )

        EnhancedStatChart(
            title = "Battery Drain (%)",
            data = history.map { viewModel.batteryLevel * 100f }, // Real battery from VM
            color = Color(0xFF4CAF50),
            maxVal = 100f,
            unit = "%"
        )
    }
}

@Composable
fun EnhancedStatChart(
    title: String,
    data: List<Float>,
    color: Color,
    maxVal: Float,
    unit: String
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                selectedIndex?.let { idx ->
                    if (idx < data.size) {
                        Text(
                            text = "VALUE: ${data[idx].roundToInt()}$unit",
                            style = MaterialTheme.typography.labelLarge,
                            color = color
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(data) {
                            detectTapGestures { offset ->
                                val spacePerPoint = size.width / (if (data.size > 1) data.size - 1 else 1)
                                selectedIndex = (offset.x / spacePerPoint)
                                    .roundToInt()
                                    .coerceIn(0, data.size - 1)
                            }
                        }
                ) {
                    if (data.size < 2) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "Awaiting telemetry stream...",
                            size.width / 2 - 100f, size.height / 2,
                            android.graphics.Paint().apply {
                                setColor(android.graphics.Color.GRAY)
                                textSize = 30f
                            }
                        )
                        return@Canvas
                    }

                    val width = size.width
                    val height = size.height
                    val spacePerPoint = width / (data.size - 1)

                    // Draw Grid Lines
                    val gridPaint = android.graphics.Paint().apply {
                        setColor(android.graphics.Color.LTGRAY)
                        alpha = 50
                    }
                    for (i in 0..4) {
                        val y = height - (i * height / 4)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Create Path
                    val path = Path().apply {
                        data.forEachIndexed { index, value ->
                            val x = index * spacePerPoint
                            val y = height - (value / maxVal * height).coerceIn(0f, height)
                            if (index == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }

                    // Draw Area Fill
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )

                    // Draw Line
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.cornerPathEffect(8f)
                        )
                    )

                    // Draw Selection Indicator
                    selectedIndex?.let { idx ->
                        if (idx < data.size) {
                            val x = idx * spacePerPoint
                            val y = height - (data[idx] / maxVal * height).coerceIn(0f, height)
                            
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            )
                            
                            drawCircle(
                                color = color,
                                radius = 6.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }
    }
}
