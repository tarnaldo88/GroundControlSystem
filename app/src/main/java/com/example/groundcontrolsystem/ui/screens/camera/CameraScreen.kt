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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen() {
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
        CameraContent(modifier = Modifier.fillMaxSize())
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission required")
        }
    }
}

@Composable
fun CameraContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    
    // Mock Telemetry Data
    var speed by remember { mutableFloatStateOf(0f) }
    var altitude by remember { mutableFloatStateOf(0f) }
    var latitude by remember { mutableDoubleStateOf(1.3521) }
    var longitude by remember { mutableDoubleStateOf(103.8198) }

    // Simulation of Telemetry
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            speed = (15f + (Math.random().toFloat() * 5f))
            altitude = (120f + (Math.random().toFloat() * 10f))
            latitude += (Math.random() - 0.5) * 0.0001
            longitude += (Math.random() - 0.5) * 0.0001
        }
    }

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

        // HUD Overlay - Telemetry Data (Top Left)
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HudText(label = "SPD", value = "${"%.1f".format(speed)} km/h")
            HudText(label = "ALT", value = "${"%.1f".format(altitude)} m")
            Spacer(modifier = Modifier.height(8.dp))
            HudText(label = "LAT", value = "%.5f".format(latitude))
            HudText(label = "LON", value = "%.5f".format(longitude))
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
