package com.example.groundcontrolsystem.ui.screens.gps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun GpsScreen(viewModel: TelemetryViewModel) {
    val context = LocalContext.current

    var followDrone by remember { mutableStateOf(true) }
    var showLayerMenu by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
            isVerticalMapRepetitionEnabled = false
            isHorizontalMapRepetitionEnabled = false
            controller.setZoom(17.0)
            controller.setCenter(GeoPoint(viewModel.latitude, viewModel.longitude))
        }
    }

    // Sync tile source
    LaunchedEffect(viewModel.currentTileSource) {
        mapView.setTileSource(viewModel.currentTileSource)
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
        if (!mapView.overlays.contains(droneMarker)) {
            mapView.overlays.add(droneMarker)
        }
    }

    LaunchedEffect(viewModel.latitude, viewModel.longitude) {
        val dronePos = GeoPoint(viewModel.latitude, viewModel.longitude)
        droneMarker.position = dronePos
        if (followDrone) {
            mapView.controller.animateTo(dronePos)
        }
        mapView.invalidate()
    }

    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Layer Toggle
            Box {
                FloatingActionButton(
                    onClick = { showLayerMenu = true },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Map Layers")
                }
                
                DropdownMenu(
                    expanded = showLayerMenu,
                    onDismissRequest = { showLayerMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Standard (Mapnik)") },
                        onClick = { 
                            viewModel.setTileSource(TileSourceFactory.MAPNIK)
                            showLayerMenu = false 
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Topographic (OpenTopo)") },
                        onClick = { 
                            viewModel.setTileSource(TileSourceFactory.OpenTopo)
                            showLayerMenu = false 
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Public Transport") },
                        onClick = { 
                            viewModel.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT)
                            showLayerMenu = false 
                        }
                    )
                }
            }

            // Follow Drone Toggle
            FloatingActionButton(
                onClick = { followDrone = !followDrone },
                containerColor = if (followDrone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (followDrone) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Follow Drone")
            }
        }

        // Mini HUD
        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("DRONE POSITION", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text("${"%.6f".format(viewModel.latitude)}, ${"%.6f".format(viewModel.longitude)}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text(
                    text = "LAYER: ${viewModel.currentTileSource.name()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }
}
