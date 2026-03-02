package com.example.groundcontrolsystem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.groundcontrolsystem.ui.navigation.Routes
import com.example.groundcontrolsystem.ui.screens.camera.CameraScreen
import com.example.groundcontrolsystem.ui.screens.dashboard.DashboardScreen
import com.example.groundcontrolsystem.ui.screens.gps.GpsScreen
import com.example.groundcontrolsystem.ui.screens.logs.LogsScreen
import com.example.groundcontrolsystem.ui.screens.logs.LogEntry
import com.example.groundcontrolsystem.ui.screens.logs.LogLevel
import com.example.groundcontrolsystem.ui.screens.settings.SettingsScreen
import com.example.groundcontrolsystem.ui.screens.missionplan.MissionPlanScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    isNightVision: Boolean,
    onNightVisionToggle: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard.route
    ) {
        val dummyLogs = listOf(
            LogEntry("10:00:01", LogLevel.INFO, "System started"),
            LogEntry("10:00:05", LogLevel.DEBUG, "GPS module initialized"),
            LogEntry("10:01:20", LogLevel.WARNING, "Low signal strength detected"),
            LogEntry("10:02:15", LogLevel.ERROR, "Camera connection failed"),
            LogEntry("10:03:00", LogLevel.INFO, "Retrying camera connection..."),
            LogEntry("10:03:05", LogLevel.INFO, "Camera connected successfully"),
            LogEntry("10:05:00", LogLevel.DEBUG, "Battery at 85%"),
            LogEntry("10:10:00", LogLevel.ERROR, "Obstacle avoidance sensor timeout"),
        )

        composable(Routes.Dashboard.route) { DashboardScreen() }
        composable(Routes.Camera.route) { CameraScreen() }
        composable(Routes.Settings.route) { 
            SettingsScreen(
                isNightVision = isNightVision,
                onNightVisionToggle = onNightVisionToggle
            ) 
        }
        composable(Routes.Gps.route) { GpsScreen() }
        composable(Routes.Logs.route) { LogsScreen(dummyLogs) }
        composable(Routes.MissionPlan.route) { MissionPlanScreen() }
    }
}