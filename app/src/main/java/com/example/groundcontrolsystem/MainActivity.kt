package com.example.groundcontrolsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment
import androidx.savedstate.savedState
import com.example.groundcontrolsystem.ui.theme.GroundControlSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                setContent {

                }
            }

        }
    }
}

private enum class AppRoute(val route: String) {
    Dashboard("dashboard"),
    Camera("camera"),
    Settings("settings"),
    Gps("gps"),
    Logs("logs")
}

@Composable
private fun TabletShell() {
    val navController = rememberNavController()

    Row(modifier = Modifier.fillMaxSize()) {
        LeftNavRail(
            navController = navController,
            modifier = Modifier.fillMaxHeight().width(96.dp)
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopCommandBar(
                modifier = Modifier.fillMaxWidth(),
                title = "Ground Control System",
                statusText = "Connected"
            )

            VerticalDivider(modifier = Modifier.fillMaxWidth().height(1.dp))

            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                AppNavHost(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopCommandBar(
    modifier: Modifier = Modifier,
    title: String,
    statusText: String
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column() {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(statusText, style = MaterialTheme.typography.labelMedium)
            }
        },
        actions = {
            TextButton(onClick = { /*TODO*/ }) {Text("Log View") }
            TextButton(onClick = { /*TODO*/ }) {Text("Options") }
            Spacer(Modifier.width(8.dp))
        }
    )
}

@Composable
private fun LeftNavRail(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(AppRoute.Dashboard.route, "Dashboard", Icons.Default.Home),
        NavItem(AppRoute.Camera.route, "Camera View", Icons.AutoMirrored.Outlined.KeyboardArrowRight),
        NavItem(AppRoute.Gps.route, "GPS", Icons.Default.Place),
        NavItem(AppRoute.Logs.route, "Logs", Icons.Default.Warning),
        NavItem(AppRoute.Settings.route, "Settings", Icons.Default.Settings)
    )
    NavigationRail(
        modifier = modifier,
        header = {
            //part to put logo
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center
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
                        popUpTo(navController.graph.findStartDestination().id) {saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }

                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Dashboard.route
    ) {
        composable(AppRoute.Dashboard.route) { DashboardScreen() }
        composable(AppRoute.Camera.route) { CameraScreen() }
        composable(AppRoute.Settings.route) { SettingsScreen() }
        composable(AppRoute.Gps.route) { GpsScreen() }
        composable(AppRoute.Logs.route) { LogsScreen() }
    }

}