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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.groundcontrolsystem.ui.viewmodel.TelemetryViewModel
import com.example.groundcontrolsystem.ui.viewmodel.Waypoint
import com.example.groundcontrolsystem.ui.viewmodel.WaypointAction
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MissionPlanScreen(viewModel: TelemetryViewModel) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    var waypoints by remember { mutableStateOf(listOf<Waypoint>()) }
    var showLayerMenu by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var editingWaypoint by remember { mutableStateOf<Waypoint?>(null) }
    
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(viewModel.latitude, viewModel.longitude))
        }
    }

    LaunchedEffect(viewModel.currentTileSource) {
        mapView.setTileSource(viewModel.currentTileSource)
    }

    val polyline = remember {
        Polyline().apply {
            outlinePaint.color = android.graphics.Color.RED
            outlinePaint.strokeWidth = 5f
        }
    }

    val droneMarker = remember {
        Marker(mapView).apply {
            title = "Drone"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = context.getDrawable(android.R.drawable.ic_menu_directions)
            icon.setTint(primaryColor)
        }
    }

    LaunchedEffect(mapView) {
        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                val newId = if (waypoints.isEmpty()) 1 else waypoints.maxOf { it.id } + 1
                waypoints = waypoints + Waypoint(id = newId, location = p)
                return true
            }
            override fun longPressHelper(p: GeoPoint): Boolean = false
        })
        mapView.overlays.add(0, eventsOverlay)
    }

    LaunchedEffect(waypoints) {
        mapView.overlays.removeAll { it is Marker && it != droneMarker || it is Polyline }
        val points = waypoints.map { it.location }
        polyline.setPoints(points)
        mapView.overlays.add(polyline)

        waypoints.forEach { wp ->
            val marker = Marker(mapView)
            marker.position = wp.location
            marker.title = "WP ${wp.id}: ${wp.action}"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }
        if (!mapView.overlays.contains(droneMarker)) mapView.overlays.add(droneMarker)
        mapView.invalidate()
    }

    LaunchedEffect(viewModel.latitude, viewModel.longitude) {
        val newPos = GeoPoint(viewModel.latitude, viewModel.longitude)
        droneMarker.position = newPos
        if (viewModel.isMissionActive) {
            mapView.controller.animateTo(newPos)
        }
        mapView.invalidate()
    }

    // Waypoint Editor Dialog
    editingWaypoint?.let { wp ->
        var alt by remember { mutableStateOf(wp.targetAltitude.toString()) }
        var spd by remember { mutableStateOf(wp.targetSpeed.toString()) }
        var action by remember { mutableStateOf(wp.action) }
        var duration by remember { mutableStateOf(wp.actionDuration.toString()) }

        AlertDialog(
            onDismissRequest = { editingWaypoint = null },
            title = { Text("Edit Waypoint ${wp.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = alt, onValueChange = { alt = it }, label = { Text("Altitude (m)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = spd, onValueChange = { spd = it }, label = { Text("Speed (km/h)") }, modifier = Modifier.fillMaxWidth())
                    
                    Text("Action", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WaypointAction.values().forEach { a ->
                            FilterChip(
                                selected = action == a,
                                onClick = { action = a },
                                label = { Text(a.name, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    
                    if (action == WaypointAction.HOVER) {
                        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Hover Duration (s)") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updatedWp = wp.copy(
                        targetAltitude = alt.toFloatOrNull() ?: wp.targetAltitude,
                        targetSpeed = spd.toFloatOrNull() ?: wp.targetSpeed,
                        action = action,
                        actionDuration = duration.toIntOrNull() ?: wp.actionDuration
                    )
                    waypoints = waypoints.map { if (it.id == wp.id) updatedWp else it }
                    editingWaypoint = null
                }) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { editingWaypoint = null }) { Text("Cancel") } }
        )
    }

    if (showChecklistDialog) {
        var batteryChecked by remember { mutableStateOf(viewModel.batteryLevel > 0.5f) }
        var gpsChecked by remember { mutableStateOf(viewModel.isConnected && viewModel.signalStrength > 0.5f) }
        var nfZChecked by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChecklistDialog = false },
            title = { Text("Pre-Flight Checklist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChecklistItem("Battery > 50%", batteryChecked, { batteryChecked = it }, viewModel.batteryLevel > 0.5f)
                    ChecklistItem("GPS Signal Lock", gpsChecked, { gpsChecked = it }, viewModel.isConnected && viewModel.signalStrength > 0.5f)
                    ChecklistItem("No-Fly Zone Checked", nfZChecked, { nfZChecked = it })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showChecklistDialog = false
                        viewModel.startMission(waypoints)
                    },
                    enabled = batteryChecked && gpsChecked && nfZChecked
                ) { Text("Launch Mission") }
            }
        )
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.weight(2f).fillMaxHeight().clipToBounds(), tonalElevation = 2.dp) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                SmallFloatingActionButton(onClick = { showLayerMenu = true }, containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(Icons.Default.Layers, contentDescription = null)
                }
                DropdownMenu(expanded = showLayerMenu, onDismissRequest = { showLayerMenu = false }) {
                    DropdownMenuItem(text = { Text("Standard") }, onClick = { viewModel.setTileSource(TileSourceFactory.MAPNIK); showLayerMenu = false })
                    DropdownMenuItem(text = { Text("Satellite (Topo)") }, onClick = { viewModel.setTileSource(TileSourceFactory.OpenTopo); showLayerMenu = false })
                }
                SmallFloatingActionButton(onClick = { waypoints = emptyList() }, modifier = Modifier.align(Alignment.TopEnd), containerColor = MaterialTheme.colorScheme.errorContainer) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                }
            }
        }

        VerticalDivider()

        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Mission Planner", style = MaterialTheme.typography.titleLarge)
            
            if (waypoints.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Tap map to add waypoints", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(waypoints) { wp ->
                        val isActive = viewModel.currentWaypointIndex == waypoints.indexOf(wp)
                        WaypointItem(
                            wp = wp, 
                            isActive = isActive, 
                            onDelete = { waypoints = waypoints.filter { it.id != wp.id } },
                            onEdit = { editingWaypoint = wp }
                        )
                    }
                }
            }

            Button(
                onClick = { showChecklistDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = waypoints.isNotEmpty() && viewModel.isConnected && !viewModel.isMissionActive
            ) {
                if (viewModel.isMissionActive) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Execute Mission")
                }
            }
        }
    }
}

@Composable
fun WaypointItem(wp: Waypoint, isActive: Boolean, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("WP ${wp.id}", style = MaterialTheme.typography.labelLarge)
                    if (isActive) {
                        Surface(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.extraSmall, modifier = Modifier.padding(start = 8.dp)) {
                            Text("ACTIVE", modifier = Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
                Text("${wp.action} @ ${wp.targetAltitude}m", style = MaterialTheme.typography.bodySmall)
                Text("${"%.4f".format(wp.location.latitude)}, ${"%.4f".format(wp.location.longitude)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ChecklistItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
    }
}
