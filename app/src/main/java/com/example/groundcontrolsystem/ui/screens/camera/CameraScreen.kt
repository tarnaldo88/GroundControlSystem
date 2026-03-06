package com.example.groundcontrolsystem.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
    
    var zoomRatio by remember { mutableFloatStateOf(1f) }
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

        // Thermal/Night Vision Overlay simulation
        if (viewModel.systemLogs.any { it.message.contains("IR", ignoreCase = true) }) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(color = Color.Green.copy(alpha = 0.15f))
            }
        }

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

        // Gauges
        AltitudeLadder(altitude = viewModel.altitude, modifier = Modifier.align(Alignment.CenterEnd).width(80.dp).fillMaxHeight(0.6f).padding(end = 16.dp))
        CompassGauge(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).size(120.dp))

        // HUD
        Column(
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HudText(label = "SPD", value = "${"%.1f".format(viewModel.speed)} km/h")
            HudText(label = "ALT", value = "${"%.1f".format(viewModel.altitude)} m")
            if (viewModel.isRecording) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color.Red, shape = RoundedCornerShape(50), modifier = Modifier.size(8.dp)) {}
                    Spacer(Modifier.width(4.dp))
                    Text("REC", color = Color.Red, style = MaterialTheme.typography.labelSmall)
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

                FilledTonalButton(onClick = { /* Toggle IR via ViewModel log for now */ }) {
                    Text("IR")
                }
            }
        }
    }
}

@Composable
fun AltitudeLadder(altitude: Float, modifier: Modifier = Modifier) {
    val animatedAlt by animateFloatAsState(targetValue = altitude)
    Canvas(modifier = modifier) {
        drawRoundRect(color = Color.Black.copy(alpha = 0.3f), size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
        val centerH = size.height / 2
        for (i in -50..50 step 10) {
            val labelAlt = (animatedAlt.toInt() / 10 * 10) + i
            if (labelAlt < 0) continue
            val yPos = centerH - (labelAlt - animatedAlt) * (size.height / 100f)
            if (yPos in 0f..size.height) {
                drawLine(Color.White, Offset(0f, yPos), Offset(size.width * 0.3f, yPos), strokeWidth = 2f)
                drawContext.canvas.nativeCanvas.drawText(labelAlt.toString(), size.width * 0.4f, yPos + 10f, android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 30f })
            }
        }
        drawLine(Color.Cyan, Offset(0f, centerH), Offset(size.width, centerH), strokeWidth = 3f)
    }
}

@Composable
fun CompassGauge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = radius, center = center, style = Stroke(width = 2f))
        for (angle in 0 until 360 step 30) {
            rotate(angle.toFloat()) {
                drawLine(Color.White, Offset(center.x, center.y - radius), Offset(center.x, center.y - radius + 15f), strokeWidth = 2f)
            }
        }
        drawLine(Color.Cyan, center, Offset(center.x, center.y - radius * 0.8f), strokeWidth = 4f)
    }
}

@Composable
fun HudText(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "$label:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = Color.White)
    }
}
