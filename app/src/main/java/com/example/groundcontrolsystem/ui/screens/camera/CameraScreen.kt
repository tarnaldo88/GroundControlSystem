package com.example.groundcontrolsystem.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CameraScreen(viewModel: TelemetryViewModel) {
    val context = LocalContext.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
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
    
    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    val previewView = remember { PreviewView(context) }

    DisposableEffect(camera) {
        val observer = androidx.lifecycle.Observer<ZoomState> { state ->
            zoomRatio = state.zoomRatio
        }
        camera?.cameraInfo?.zoomState?.observe(lifecycleOwner, observer)
        onDispose {
            camera?.cameraInfo?.zoomState?.removeObserver(observer)
        }
    }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
            cameraProviderFuture.addListener({
                continuation.resume(cameraProviderFuture.get())
            }, ContextCompat.getMainExecutor(context))
        }

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(modifier = modifier.clipToBounds()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // --- NEW GAUGES ---

        // 1. Altitude Ladder (Right Side)
        AltitudeLadder(
            altitude = viewModel.altitude,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(80.dp)
                .fillMaxHeight(0.6f)
                .padding(end = 16.dp)
        )

        // 2. Compass / Heading (Top Center)
        CompassGauge(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .size(120.dp)
        )

        // --- EXISTING HUD ---

        // HUD Overlay - Telemetry Data (Top Left)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HudText(label = "SPD", value = "${"%.1f".format(viewModel.speed)} km/h")
            HudText(label = "ALT", value = "${"%.1f".format(viewModel.altitude)} m")
            Spacer(modifier = Modifier.height(8.dp))
            HudText(label = "LAT", value = "%.5f".format(viewModel.latitude))
            HudText(label = "LON", value = "%.5f".format(viewModel.longitude))
        }

        // Zoom Level Indicator (Top Right)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Zoom: ${"%.1f".format(zoomRatio)}x",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }

        // Controls at the bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val currentZoom = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: zoomRatio
                        val minZoom = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
                        val newZoom = (currentZoom - 0.5f).coerceAtLeast(minZoom)
                        camera?.cameraControl?.setZoomRatio(newZoom)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                FloatingActionButton(
                    onClick = { /* Capture Logic */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Take Picture", modifier = Modifier.size(36.dp))
                }

                FilledTonalIconButton(
                    onClick = {
                        val currentZoom = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: zoomRatio
                        val maxZoom = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 5f
                        val newZoom = (currentZoom + 0.5f).coerceAtMost(maxZoom)
                        camera?.cameraControl?.setZoomRatio(newZoom)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                FilledTonalButton(
                    onClick = { /* IR Toggle */ },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(text = "IR")
                }
            }
        }
    }
}

@Composable
fun AltitudeLadder(altitude: Float, modifier: Modifier = Modifier) {
    val animatedAlt by animateFloatAsState(targetValue = altitude, label = "altitude")
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerH = height / 2

        // Background
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.3f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )

        // Draw scale
        val scaleSpacing = 20f
        val startAlt = (animatedAlt.toInt() / 10) * 10 - 50
        
        for (i in 0..100 step 10) {
            val currentLabelAlt = startAlt + i
            if (currentLabelAlt < 0) continue
            
            val offset = (currentLabelAlt - animatedAlt) * (height / 100f)
            val yPos = centerH - offset

            if (yPos in 0f..height) {
                // Main tick
                drawLine(
                    color = Color.White,
                    start = Offset(0f, yPos),
                    end = Offset(width * 0.3f, yPos),
                    strokeWidth = 2f
                )
                
                // Text
                drawContext.canvas.nativeCanvas.drawText(
                    currentLabelAlt.toString(),
                    width * 0.4f,
                    yPos + 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 30f
                        isFakeBoldText = true
                    }
                )
            }
        }

        // Current Alt Indicator Arrow
        val path = Path().apply {
            moveTo(0f, centerH - 15f)
            lineTo(20f, centerH)
            lineTo(0f, centerH + 15f)
            close()
        }
        drawPath(path, Color.Cyan)
        drawLine(Color.Cyan, Offset(0f, centerH), Offset(width, centerH), strokeWidth = 3f)
    }
}

@Composable
fun CompassGauge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Ring
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )

        // Compass Ticks
        for (angle in 0 until 360 step 30) {
            rotate(angle.toFloat()) {
                drawLine(
                    color = Color.White,
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y - radius + 15f),
                    strokeWidth = 2f
                )
                
                val label = when (angle) {
                    0 -> "N"
                    90 -> "E"
                    180 -> "S"
                    270 -> "W"
                    else -> ""
                }
                
                if (label.isNotEmpty()) {
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        center.x - 10f,
                        center.y - radius + 40f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 35f
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }

        // Center Indicator
        drawCircle(Color.Cyan, 5f, center)
        drawLine(Color.Cyan, center, Offset(center.x, center.y - radius * 0.8f), strokeWidth = 4f)
    }
}

@Composable
fun HudText(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}
