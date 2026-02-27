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
    Items("items"),
    Settings("settings")
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

}

@Composable
private fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GroundControlSystemTheme {
        Greeting("Android")
    }
}