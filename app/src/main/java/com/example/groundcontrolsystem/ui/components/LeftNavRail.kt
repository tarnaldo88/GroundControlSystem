package com.example.groundcontrolsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.groundcontrolsystem.ui.navigation.Routes

/**
 * INSTRUCTIONS & HOW-TO:
 * 
 * 1. STATE MANAGEMENT:
 *    We use `remember { mutableStateOf(false) }` to track if the dialog is open.
 *    `showResourcesDialog` is a boolean flag that controls the visibility of the AlertDialog.
 * 
 * 2. TRIGGERING THE POP-UP:
 *    We added a standard `NavigationRailItem` that doesn't navigate, but instead 
 *    sets `showResourcesDialog = true` when clicked.
 * 
 * 3. THE POP-UP (AlertDialog):
 *    - `onDismissRequest`: Handles closing when the user clicks outside or presses back.
 *    - `title` & `text`: Basic descriptive content.
 *    - `confirmButton`: Navigates to the internal WebResources route.
 * 
 * 4. INTERNAL WEB VIEW:
 *    Instead of opening an external browser, we navigate to a dedicated screen 
 *    (`WebResourcesScreen`) that hosts a WebView, keeping the user inside the GCS app.
 */

private data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun LeftNavRail(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var showResourcesDialog by remember { mutableStateOf(false) }

    val items = listOf(
        NavItem(Routes.Dashboard.route, "Dashboard", Icons.Default.Home),
        NavItem(Routes.Camera.route, "Camera View", Icons.AutoMirrored.Outlined.KeyboardArrowRight),
        NavItem(Routes.Gps.route, "GPS", Icons.Default.Place),
        NavItem(Routes.MissionPlan.route, "Mission Plan", Icons.Default.Code),
        NavItem(Routes.Statistics.route, "Stats", Icons.Default.BarChart),
        NavItem(Routes.Replay.route, "Replay", Icons.Default.History),
        NavItem(Routes.Logs.route, "Logs", Icons.Default.Warning),
        NavItem(Routes.Settings.route, "Settings", Icons.Default.Settings)
    )

    NavigationRail(
        modifier = modifier,
        header = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("GCS", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationRailItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }

        Spacer(Modifier.weight(1f))

        // New Resources Button
        NavigationRailItem(
            selected = false,
            onClick = { showResourcesDialog = true },
            icon = { Icon(Icons.Default.Info, contentDescription = "Resources") },
            label = { Text("Links") },
            alwaysShowLabel = true
        )
    }

    // Resource Pop-up (AlertDialog)
    if (showResourcesDialog) {
        AlertDialog(
            onDismissRequest = { showResourcesDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(24.dp)) },
            title = { Text("Flight Resources") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Access official UAV portals and flight safety documentation.")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Note: This will open the portal inside the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate(Routes.WebResources.route)
                        showResourcesDialog = false
                    }
                ) {
                    Text("Open FAA Portal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResourcesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
