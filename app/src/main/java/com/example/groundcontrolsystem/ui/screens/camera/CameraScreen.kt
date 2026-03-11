package com.example.groundcontrolsystem.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

@Composable
fun CameraScreen(viewModel: TelemetryViewModel) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        CameraContent(viewModel = viewModel, modifier = Modifier.fillMaxSize())
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission required")
        }
    }
}

@Composable
fun CameraContent(viewModel: TelemetryViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val previewView = remember { PreviewView(context) }

    // Object Selection State
    var selectionStart by remember { mutableStateOf<Offset?>(null) }
    var selectionEnd by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
            cameraProviderFuture.addListener({ continuation.resume(cameraProviderFuture.get()) }, ContextCompat.getMainExecutor(context))
        }
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) })
        } catch (e: Exception) { e.printStackTrace() }
    }

    Box(modifier = modifier
        .clipToBounds()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { selectionStart = it; selectionEnd = it },
                onDrag = { _, dragAmount -> selectionEnd = (selectionEnd ?: Offset.Zero) + dragAmount },
                onDragEnd = {
                    selectionStart?.let { start ->
                        selectionEnd?.let { end ->
                            viewModel.trackedObjectBox = RectF(
                                minOf(start.x, end.x), minOf(start.y, end.y),
                                maxOf(start.x, end.x), maxOf(start.y, end.y)
                            )
                        }
                    }
                    selectionStart = null
                    selectionEnd = null
                }
            )
        }
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Advanced HUD Overlay
        HudOverlay(viewModel = viewModel)

        // Object Tracking UI
        viewModel.trackedObjectBox?.let { box ->
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Cyan,
                    topLeft = Offset(box.left, box.top),
                    size = Size(box.width(), box.height()),
                    style = Stroke(width = 4f)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "TRACKING LOCK",
                    box.left, box.top - 10f,
                    android.graphics.Paint().apply { color = android.graphics.Color.CYAN; textSize = 40f; isFakeBoldText = true }
                )
            }
        }

        // Selection Drag UI
        selectionStart?.let { start ->
            selectionEnd?.let { end ->
                Canvas(Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.5f),
                        topLeft = Offset(minOf(start.x, end.x), minOf(start.y, end.y)),
                        size = Size(abs(start.x - end.x), abs(start.y - end.y)),
                        style = Stroke(width = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    )
                }
            }
        }

        // Controls
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.trackedObjectBox = null }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Tracking")
                }

                FloatingActionButton(
                    onClick = { viewModel.toggleRecording() },
                    containerColor = if (viewModel.isRecording) Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(if (viewModel.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord, contentDescription = "Record", modifier = Modifier.size(36.dp))
                }

                FilledTonalButton(onClick = { /* IR Toggle logic */ }) {
                    Text("IR")
                }
            }
        }
    }
}

@Composable
fun HudOverlay(viewModel: TelemetryViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Artificial Horizon / Attitude Indicator Center
        AttitudeIndicator(modifier = Modifier.align(Alignment.Center).size(300.dp))

        // Left Side: Speed Tape
        Box(modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)) {
            VerticalTape(
                value = viewModel.speed,
                label = "SPD",
                unit = "KM/H",
                color = Color.Cyan,
                modifier = Modifier.width(80.dp).height(400.dp)
            )
        }

        // Right Side: Altitude Tape
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)) {
            VerticalTape(
                value = viewModel.altitude,
                label = "ALT",
                unit = "M",
                color = Color.Yellow,
                modifier = Modifier.width(80.dp).height(400.dp)
            )
        }

        // Top: Compass / Heading
        Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp)) {
            HeadingIndicator(modifier = Modifier.width(400.dp).height(60.dp))
        }

        // Bottom Stats
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 120.dp, start = 24.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HudStatItem(label = "BAT", value = "${(viewModel.batteryLevel * 100).toInt()}%", color = if(viewModel.batteryLevel > 0.2f) Color.Green else Color.Red)
            HudStatItem(label = "SIG", value = "${(viewModel.signalStrength * 100).toInt()}%", color = Color.Cyan)
            if (viewModel.isRecording) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color.Red, RoundedCornerShape(50)))
                    Spacer(Modifier.width(4.dp))
                    Text("REC", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun AttitudeIndicator(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // Static Aircraft Reference
        val aircraftWidth = 100f
        drawLine(Color.White, Offset(centerX - aircraftWidth, centerY), Offset(centerX - 20f, centerY), strokeWidth = 4f)
        drawLine(Color.White, Offset(centerX + 20f, centerY), Offset(centerX + aircraftWidth, centerY), strokeWidth = 4f)
        drawCircle(Color.White, radius = 5f, center = Offset(centerX, centerY))
        
        // Pitch/Roll markings would be animated here in a real impl
        drawContext.canvas.nativeCanvas.drawText("0°", centerX + 110f, centerY + 10f, android.graphics.Paint().apply { 
            color = android.graphics.Color.WHITE
            textSize = 30f
        })
    }
}

@Composable
fun VerticalTape(value: Float, label: String, unit: String, color: Color, modifier: Modifier) {
    val animatedValue by animateFloatAsState(targetValue = value)
    
    Canvas(modifier = modifier) {
        drawRect(Color.Black.copy(alpha = 0.4f))
        val stepHeight = size.height / 10
        val center = size.height / 2
        
        // Scale markings
        for (i in -5..5) {
            val markValue = (animatedValue.toInt() / 10 * 10) + (i * 10)
            if (markValue < 0) continue
            
            val yPos = center - (markValue - animatedValue) * (stepHeight / 10f)
            if (yPos in 0f..size.height) {
                drawLine(Color.White.copy(alpha = 0.7f), Offset(0f, yPos), Offset(20f, yPos), strokeWidth = 2f)
                drawContext.canvas.nativeCanvas.drawText(
                    markValue.toString(), 25f, yPos + 10f,
                    android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 24f }
                )
            }
        }
        
        // Center pointer
        val path = Path().apply {
            moveTo(size.width, center)
            lineTo(size.width - 30f, center - 20f)
            lineTo(size.width - 30f, center + 20f)
            close()
        }
        drawPath(path, color)
        
        // Label/Value
        drawContext.canvas.nativeCanvas.drawText(
            "$label: ${value.toInt()}$unit", 10f, 30f,
            android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 28f; isFakeBoldText = true }
        )
    }
}

@Composable
fun HeadingIndicator(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color.Black.copy(alpha = 0.4f))
        val centerX = size.width / 2
        
        // Pointer
        val pointer = Path().apply {
            moveTo(centerX, size.height)
            lineTo(centerX - 10f, size.height - 15f)
            lineTo(centerX + 10f, size.height - 15f)
            close()
        }
        drawPath(pointer, Color.Cyan)
        
        // Heading text (Mocked at 000 North)
        drawContext.canvas.nativeCanvas.drawText(
            "000", centerX - 25f, 35f,
            android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 32f; isFakeBoldText = true }
        )
    }
}

@Composable
fun HudStatItem(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}
