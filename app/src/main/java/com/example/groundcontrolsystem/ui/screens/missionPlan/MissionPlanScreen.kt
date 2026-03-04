package com.example.groundcontrolsystem.ui.screens.missionplan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.BufferedReader
import java.io.InputStreamReader

data class Waypoint(
    val id: Int,
    val location: GeoPoint,
    val description: String = "Waypoint"
)

@Composable
fun MissionPlanScreen(viewModel: TelemetryViewModel) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    var waypoints by remember { mutableStateOf(listOf<Waypoint>()) }
    var missionObjectives by remember { mutableStateOf("") }
    var showObjectivesDialog by remember { mutableStateOf(false) }

    // File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val content = reader.readText()
                        missionObjectives = content
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )
    
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(1.35, 103.87))
        }
    }

    val polyline = remember {
        Polyline().apply {
            outlinePaint.color = android.graphics.Color.RED
            outlinePaint.strokeWidth = 5f
        }
    }

    LaunchedEffect(mapView) {
        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                waypoints = waypoints + Waypoint(waypoints.size + 1, p)
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean = false
        })
        mapView.overlays.add(0, eventsOverlay)
    }

    LaunchedEffect(waypoints) {
        mapView.overlays.removeAll { it is Marker || it is Polyline }
        val points = waypoints.map { it.location }
        polyline.setPoints(points)
        mapView.overlays.add(polyline)

        waypoints.forEach { wp ->
            val marker = Marker(mapView)
            marker.position = wp.location
            marker.title = "Waypoint ${wp.id}"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    if (showObjectivesDialog) {
        var tempObjectives by remember { mutableStateOf(missionObjectives) }
        AlertDialog(
            onDismissRequest = { showObjectivesDialog = false },
            title = { Text("Edit Mission Objectives") },
            text = {
                OutlinedTextField(
                    value = tempObjectives,
                    onValueChange = { tempObjectives = it },
                    label = { Text("Describe the mission") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    missionObjectives = tempObjectives
                    showObjectivesDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showObjectivesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.weight(2f).fillMaxHeight().clipToBounds(),
            tonalElevation = 2.dp
        ) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                SmallFloatingActionButton(
                    onClick = { waypoints = emptyList() },
                    modifier = Modifier.align(Alignment.TopEnd),
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All")
                }
            }
        }

        VerticalDivider()

        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mission Plan", style = MaterialTheme.typography.titleLarge)
            
            Text("Waypoints", style = MaterialTheme.typography.titleSmall)
            
            if (waypoints.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Tap on map to add waypoints")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(waypoints) { wp ->
                        WaypointItem(wp) { waypoints = waypoints.filter { it.id != wp.id } }
                    }
                }
            }

            Button(
                onClick = { viewModel.startMission() },
                modifier = Modifier.fillMaxWidth(),
                enabled = (waypoints.isNotEmpty() || missionObjectives.isNotEmpty()) && viewModel.isConnected && !viewModel.isMissionActive
            ) {
                if (viewModel.isMissionActive) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Mission In Progress")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Mission")
                }
            }
            
            if (!viewModel.isConnected) {
                Text(
                    "System disconnected. Connect in Dashboard to start.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun WaypointItem(waypoint: Waypoint, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("WP ${waypoint.id}", style = MaterialTheme.typography.labelLarge)
                Text("Lat: ${"%.4f".format(waypoint.location.latitude)}\nLon: ${"%.4f".format(waypoint.location.longitude)}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
