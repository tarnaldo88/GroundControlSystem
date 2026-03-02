package com.example.groundcontrolsystem.ui.screens.missionplan

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
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

data class Waypoint(
    val id: Int,
    val location: GeoPoint,
    val description: String = "Waypoint"
)

@Composable
fun MissionPlanScreen() {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    var waypoints by remember { mutableStateOf(listOf<Waypoint>()) }
    var missionObjectives by remember { mutableStateOf("") }
    var showObjectivesDialog by remember { mutableStateOf(false) }
    
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

    // Handle map clicks to add waypoints
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

    // Update map markers and lines when waypoints change
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
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("e.g. Survey the north perimeter and check for leaks.") }
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
        // Map Section (Left side)
        Surface(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .clipToBounds(),
            tonalElevation = 2.dp
        ) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            
            // Map Overlay Controls
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

        // Objectives/Waypoints List (Right side)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mission Plan", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { showObjectivesDialog = true }) {
                    Icon(Icons.Default.EditNote, contentDescription = "Edit Objectives")
                }
            }

            if (missionObjectives.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Objectives:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = missionObjectives,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Text("Waypoints", style = MaterialTheme.typography.titleSmall)
            
            if (waypoints.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Tap on map to add waypoints", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(waypoints) { wp ->
                        WaypointItem(wp) {
                            waypoints = waypoints.filter { it.id != wp.id }
                        }
                    }
                }
            }

            Button(
                onClick = { /* TODO: Launch Mission */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = waypoints.isNotEmpty() || missionObjectives.isNotEmpty()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start Mission")
            }
        }
    }
}

@Composable
fun WaypointItem(waypoint: Waypoint, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("WP ${waypoint.id}", style = MaterialTheme.typography.labelLarge)
                Text(
                    "Lat: ${"%.4f".format(waypoint.location.latitude)}\nLon: ${"%.4f".format(waypoint.location.longitude)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
