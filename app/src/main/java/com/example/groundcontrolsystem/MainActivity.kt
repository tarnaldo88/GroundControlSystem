package com.example.groundcontrolsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.groundcontrolsystem.ui.theme.GroundControlSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroundControlSystemTheme {
                TabletShell()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopCommandBar(
    modifier: Modifier = Modifier,
    statusText: String
) {
    val title = "Ground Control System"

    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(statusText, style = MaterialTheme.typography.labelMedium)
            }
        },
        actions = {
            TextButton(onClick = { /*TODO*/ }) { Text("Log View") }
            TextButton(onClick = { /*TODO*/ }) { Text("Options") }
            Spacer(Modifier.width(8.dp))
        }
    )
}



private data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun DashboardScreen() {
    Card(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Dashboard Page")
        }
    }
}

@Composable
private fun CameraScreen() {
    Card(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera Page")
        }
    }
}

@Composable
private fun SettingsScreen() {
    Card(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Settings Page")
        }
    }
}

@Composable
private fun GpsScreen() {
    Card(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("GPS Page")
        }
    }
}

@Composable
private fun LogsScreen() {
    Card(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Logs Page")
        }
    }
}
