package com.example.groundcontrolsystem.ui.screens.replay

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.groundcontrolsystem.ui.viewmodel.MissionLog
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlinx.coroutines.delay

@Composable
fun ReplayScreen(viewModel: TelemetryViewModel) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    val logs = viewModel.missionLogs.toList()
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

    val currentFrame = if (logs.isNotEmpty()) logs[currentFrameIndex] else null
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(17.0)
        }
    }

    val pathOverlay = remember {
        Polyline().apply {
            outlinePaint.color = android.graphics.Color.CYAN
            outlinePaint.strokeWidth = 5f
        }
    }

    val droneMarker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = context.getDrawable(android.R.drawable.ic_menu_directions)
            icon.setTint(primaryColor)
        }
    }

    LaunchedEffect(logs) {
        if (logs.isNotEmpty()) {
            pathOverlay.setPoints(logs.map { GeoPoint(it.latitude, it.longitude) })
            mapView.overlays.add(pathOverlay)
            mapView.overlays.add(droneMarker)
            mapView.controller.setCenter(GeoPoint(logs[0].latitude, logs[0].longitude))
        }
    }

    LaunchedEffect(currentFrameIndex) {
        currentFrame?.let {
            val pos = GeoPoint(it.latitude, it.longitude)
            droneMarker.position = pos
            mapView.controller.animateTo(pos)
            mapView.invalidate()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying && currentFrameIndex < logs.size - 1) {
            delay((1000 / playbackSpeed).toLong())
            currentFrameIndex++
            if (currentFrameIndex >= logs.size - 1) isPlaying = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            
            // Replay HUD
            currentFrame?.let {
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("REPLAY DATA", style = MaterialTheme.typography.labelSmall, color = Color.Cyan)
                        Text("Time: ${it.timestamp}", color = Color.White)
                        Text("Alt: ${"%.1f".format(it.altitude)}m", color = Color.White)
                        Text("Spd: ${"%.1f".format(it.speed)}km/h", color = Color.White)
                    }
                }
            }
        }

        Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Slider(
                    value = currentFrameIndex.toFloat(),
                    onValueChange = { currentFrameIndex = it.toInt() },
                    valueRange = 0f..maxOf(0f, (logs.size - 1).toFloat()),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isPlaying = !isPlaying }, enabled = logs.isNotEmpty()) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        }
                        IconButton(onClick = { currentFrameIndex = 0; isPlaying = false }, enabled = logs.isNotEmpty()) {
                            Icon(Icons.Default.Replay, contentDescription = null)
                        }
                        Text("Speed: ${playbackSpeed}x", modifier = Modifier.padding(start = 8.dp))
                        Slider(value = playbackSpeed, onValueChange = { playbackSpeed = it }, valueRange = 1f..5f, modifier = Modifier.width(100.dp))
                    }

                    Row {
                        Button(onClick = { exportToCSV(context, logs) }, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(Icons.Default.Description, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("CSV")
                        }
                        Button(onClick = { exportToKML(context, logs) }) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("KML")
                        }
                    }
                }
            }
        }
    }
}

private fun exportToCSV(context: Context, logs: List<MissionLog>) {
    val csv = StringBuilder("Timestamp,Latitude,Longitude,Altitude,Speed\n")
    logs.forEach { csv.append("${it.timestamp},${it.latitude},${it.longitude},${it.altitude},${it.speed}\n") }
    shareFile(context, csv.toString(), "mission_log.csv")
}

private fun exportToKML(context: Context, logs: List<MissionLog>) {
    val kml = StringBuilder()
    kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    kml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n<name>Flight Path</name>\n<Placemark>\n<LineString>\n<coordinates>\n")
    logs.forEach { kml.append("${it.longitude},${it.latitude},${it.altitude}\n") }
    kml.append("</coordinates>\n</LineString>\n</Placemark>\n</Document>\n</kml>")
    shareFile(context, kml.toString(), "mission_log.kml")
}

private fun shareFile(context: Context, content: String, fileName: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export $fileName"))
}
